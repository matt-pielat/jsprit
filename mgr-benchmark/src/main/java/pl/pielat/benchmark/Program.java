package pl.pielat.benchmark;

import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import pl.pielat.algorithm.ExtendedProblemDefinition;
import pl.pielat.benchmark.algorithmCreation.AlgorithmFactory;
import pl.pielat.benchmark.algorithmCreation.GarridoRiffAlgorithmFactory;
import pl.pielat.benchmark.algorithmCreation.JspritAlgorithmFactory;
import pl.pielat.benchmark.runnerEngine.BenchmarkRunner;
import pl.pielat.benchmark.runnerEngine.BenchmarkRunnerArgs;
import pl.pielat.benchmark.solutionProcessing.BenchmarkSolutionProcessor;
import pl.pielat.benchmark.solutionProcessing.CsvResultsSerializer;
import pl.pielat.util.logging.ConcreteLogger;
import pl.pielat.util.logging.Logger;
import pl.pielat.util.logging.Multilogger;
import pl.pielat.util.problemParsing.SolomonFileReader;
import pl.pielat.util.problemParsing.Tsplib95FileReader;
import pl.pielat.util.problemParsing.VrpFileParser;
import pl.pielat.util.problemParsing.VrpParseException;
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

    private int runsPerProblem;

    private ExtendedProblemDefinition problemInstances[];
    private File inputFiles[];

    Program(ProgramArgs args)
    {
        List<AlgorithmFactory> factoryList = new ArrayList<>(2);
        List<File> outputDirectoryList = new ArrayList<>(2);

        if (args.garridoRiffOutputDirectory != null)
        {
            factoryList.add(new GarridoRiffAlgorithmFactory());
            outputDirectoryList.add(args.garridoRiffOutputDirectory);
        }
        if (args.jspritOutputDirectory != null)
        {
            factoryList.add(new JspritAlgorithmFactory());
            outputDirectoryList.add(args.jspritOutputDirectory);
        }

        for (AlgorithmFactory factory : factoryList)
            factory.setTimeThreshold(args.timeThresholdInMs);
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
        benchmarkArgs.solutionProcessor = createSolutionProcessor(logger);

        new BenchmarkRunner(benchmarkArgs).run();
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
                instance = new ExtendedProblemDefinition(file.getName(), vrp, timeWindows, transportAsymmetry);
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

    private BenchmarkSolutionProcessor createSolutionProcessor(Logger logger)
    {
        String[] resultsFilePaths = new String[algorithmFactories.length];
        for (int i = 0; i < algorithmFactories.length; i++)
        {
            String algorithmId = algorithmFactories[i].getSerializableAlgorithmId();

            File resultsFile = new File(outputDirectories[i], "results.csv");
            resultsFilePaths[i] = resultsFile.getAbsolutePath();
        }

        String[] problemIds = new String[problemInstances.length];
        for (int i = 0; i < problemInstances.length; i++)
        {
            problemIds[i] = problemInstances[i].id;
        }

        return new CsvResultsSerializer(resultsFilePaths, problemIds, logger);
    }
}
