package pl.pielat.benchmark;

import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import org.apache.commons.io.FilenameUtils;
import pl.pielat.algorithm.ExtendedProblemDefinition;
import pl.pielat.algorithm.GarridoRiff;
import pl.pielat.benchmark.algorithmCreation.AlgorithmFactory;
import pl.pielat.benchmark.algorithmCreation.GarridoRiffAlgorithmFactory;
import pl.pielat.benchmark.algorithmCreation.JspritAlgorithmFactory;
import pl.pielat.benchmark.runnerEngine.BenchmarkRunner;
import pl.pielat.benchmark.runnerEngine.BenchmarkRunnerArgs;
import pl.pielat.benchmark.solutionProcessing.IterationProcessor;
import pl.pielat.benchmark.solutionProcessing.ProcessingArgs;
import pl.pielat.benchmark.solutionProcessing.RunProcessor;
import pl.pielat.util.logging.ConcreteLogger;
import pl.pielat.util.logging.Logger;
import pl.pielat.util.logging.Multilogger;
import pl.pielat.util.problemParsing.SolomonFileReader;
import pl.pielat.util.problemParsing.Tsplib95FileReader;
import pl.pielat.util.problemParsing.VrpFileParser;
import pl.pielat.util.solutionSerialization.AugeratFormatSolutionSerializer;
import pl.pielat.util.solutionSerialization.VrpSolutionSerializer;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Program
{
    public static void main(String[] rawArgs)
    {
        Program program;
        try
        {
            ProgramArgs args = ProgramArgs.parse(rawArgs);
            program = new Program(args);
            program.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return;
        }
    }

    private AlgorithmFactory[] algorithmFactories;
    private VrpFileParser problemParser;
    private VrpSolutionSerializer solutionSerializer;

    private File problemDirectory;
    private File[] outputDirectories;
    private File logDirectory;
    private boolean serializeIterations;

    private int runsPerProblem;

    private ExtendedProblemDefinition problemInstances[];
    private File inputFiles[];

    Program(ProgramArgs args)
    {
        serializeIterations = args.serializeIterations;

        List<AlgorithmFactory> factoryList = new ArrayList<>(2);
        List<File> outputDirectoryList = new ArrayList<>(2);

        if (args.garridoRiffOutputDirectory != null)
        {
            int populationSize = args.populationSize > 0 ? args.populationSize : GarridoRiff.DEFAULT_POPULATION_SIZE;
            int offspringSize = args.offspringSize > 0 ? args.offspringSize : GarridoRiff.DEFAULT_OFFSPRING_SIZE;
            int chromosomeSize = args.chromosomeSize > 0 ? args.chromosomeSize : GarridoRiff.DEFAULT_CHROMOSOME_SIZE;

            GarridoRiffAlgorithmFactory factory = new GarridoRiffAlgorithmFactory(
                populationSize, offspringSize, chromosomeSize);
            factoryList.add(factory);
            outputDirectoryList.add(args.garridoRiffOutputDirectory);
        }
        if (args.jspritOutputDirectory != null)
        {
            factoryList.add(new JspritAlgorithmFactory());
            outputDirectoryList.add(args.jspritOutputDirectory);
        }

        for (AlgorithmFactory factory : factoryList)
        {
            if (args.timePerRunInMs > 0)
                factory.setTimeThreshold(args.timePerRunInMs);
            if (args.iterationsPerRunInMs > 0)
                factory.setMaxIterations(args.iterationsPerRunInMs);
        }
        algorithmFactories = factoryList.toArray(new AlgorithmFactory[0]);

        problemParser = args.timeWindows
            ? new SolomonFileReader()
            : new Tsplib95FileReader();
        solutionSerializer = new AugeratFormatSolutionSerializer();

        problemDirectory = args.problemDirectory;
        outputDirectories = outputDirectoryList.toArray(new File[0]);
        logDirectory = args.logDirectory;

        runsPerProblem = args.runsPerProblem;
    }

    void start() throws IOException
    {
        logDirectory.mkdirs();
        Logger logger = createLogger(logDirectory);

        for (File outputDirectory : outputDirectories)
            outputDirectory.mkdirs();

        parseProblemInstances(logger);

        BenchmarkRunnerArgs benchmarkArgs = new BenchmarkRunnerArgs();
        benchmarkArgs.problemInstances = problemInstances;
        benchmarkArgs.algorithmFactories = algorithmFactories;
        benchmarkArgs.logger = logger;
        benchmarkArgs.runsPerProblem = runsPerProblem;

        BenchmarkRunner runner = new BenchmarkRunner(benchmarkArgs);
        runner.setRunProcessor(createRunProcessor(logger));
        runner.setIterationProcessor(createIterationProcessor(logger));
        runner.run();
    }

    private void parseProblemInstances(Logger logger) throws FileNotFoundException, IllegalArgumentException
    {
        logger.log("Starting to parse problem instances...");

        if (!problemDirectory.exists())
            throw new FileNotFoundException();

        if (!problemDirectory.isDirectory())
            throw new IllegalArgumentException();

        File[] files = problemDirectory.listFiles();
        assert files != null;

        List<ExtendedProblemDefinition> problemList = new ArrayList<>(files.length);
        List<File> successfullyParsedFiles = new ArrayList<>(files.length);

        for (File file : files)
        {
            if (file.isDirectory())
            {
                logger.log("Skipping - is directory.");
                continue;
            }

            ExtendedProblemDefinition instance;
            try
            {
                VehicleRoutingProblem vrp = problemParser.parse(file.getAbsolutePath());
                boolean timeWindows = problemParser.timeWindowsDetected();
                boolean transportAsymmetry = problemParser.transportAsymmetryDetected();
                String id = FilenameUtils.getBaseName(file.getName());
                instance = new ExtendedProblemDefinition(id, vrp, timeWindows, transportAsymmetry);
            }
            catch (FileNotFoundException e) //Should not happen
            {
                logger.log("Skipping - problem file not found.");
                logger.log(e);
                continue;
            }
            catch (Exception e)
            {
                logger.log("Skipping - problem parse failure. (%s)", file.getAbsolutePath());
                logger.log(e);
                continue;
            }

            problemList.add(instance);
            successfullyParsedFiles.add(file);
        }

        problemInstances = problemList.toArray(new ExtendedProblemDefinition[0]);
        inputFiles = successfullyParsedFiles.toArray(new File[0]);

        logger.log("Successfully parsed %d files.", problemInstances.length);
    }

    private Logger createLogger(File directory) throws IOException
    {
        String date = new SimpleDateFormat("yyMMdd-HHmmss").format(Calendar.getInstance().getTime());

        String fileName = String.format("log_%s", date);
        String suffix = "";
        String extension = "log";

        File logFile;

        Logger stdoutLogger = new ConcreteLogger(new PrintWriter(new OutputStreamWriter(System.out)));
        Logger fileLogger;

        for (int i = 2; ; i++)
        {
            if (i == 100)
            {
                stdoutLogger.log("Failed to create file logger.");
                return stdoutLogger;
            }

            String suffixedFileName = String.format("%s%s.%s", fileName, suffix, extension);
            Path filePath = Paths.get(directory.getAbsolutePath(), suffixedFileName);
            logFile = new File(filePath.toUri());

            if (logFile.createNewFile())
            {
                fileLogger = new ConcreteLogger(new PrintWriter(logFile));
                return new Multilogger(fileLogger, stdoutLogger);
            }

            suffix = String.format("_%d", i);
        }
    }

    private RunProcessor createRunProcessor(final Logger logger)
    {
        final VrpSolutionSerializer serializer = new AugeratFormatSolutionSerializer();

        if (serializeIterations)
        {
            return new RunProcessor() {
                @Override
                public void processRun(ProcessingArgs args)
                { }
            };
        }

        return new RunProcessor() {
            @Override
            public void processRun(ProcessingArgs args)
            {
                ExtendedProblemDefinition problemInstance = problemInstances[args.problemIndex];

                String filename = String.format("%s_r%d", problemInstance.id, args.runIndex);
                File outputFile = new File(outputDirectories[args.algorithmIndex], filename);

                try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(outputFile, false))))
                {
                    serializer.serialize(args.bestSolution, writer);
                }
                catch (IOException e)
                {
                    logger.log("Failed to serialize run solution to file %s.", outputFile.getAbsolutePath());
                    logger.log(e);
                }
            }
        };
    }

    private IterationProcessor createIterationProcessor(final Logger logger)
    {
        final VrpSolutionSerializer serializer = new AugeratFormatSolutionSerializer();

        if (!serializeIterations)
        {
            return new IterationProcessor() {
                @Override
                public void processIteration(ProcessingArgs args, int iterationIdx)
                { }
            };
        }

        return new IterationProcessor() {
            @Override
            public void processIteration(ProcessingArgs args, int iterationIdx)
            {
                if (!serializeIterations)
                    return;

                ExtendedProblemDefinition problemInstance = problemInstances[args.problemIndex];

                String filename = String.format("%s_r%d_i%d", problemInstance.id, args.runIndex, iterationIdx);
                File outputFile = new File(outputDirectories[args.algorithmIndex], filename);

                try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(outputFile, false))))
                {
                    serializer.serialize(args.bestSolution, writer);
                }
                catch (IOException e)
                {
                    logger.log("Failed to serialize iteration solution to file %s.", outputFile.getAbsolutePath());
                    logger.log(e);
                }
            }
        };
    }
}
