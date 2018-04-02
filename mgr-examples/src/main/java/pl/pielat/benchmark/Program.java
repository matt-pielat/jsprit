package pl.pielat.benchmark;

import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import pl.pielat.util.logging.ConcreteLogger;
import pl.pielat.util.logging.Logger;
import pl.pielat.util.problemParsing.SolomonFileReader;
import pl.pielat.util.problemParsing.Tsplib95FileReader;
import pl.pielat.util.problemParsing.VrpFileParser;
import pl.pielat.util.problemParsing.VrpParseException;
import pl.pielat.util.solutionSerialization.AugeratFormatSolutionSerializer;
import pl.pielat.util.solutionSerialization.VrpSolutionSerializer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Program implements BenchmarkSolutionProcessor
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
    private File solutionDirectory;
    private File logDirectory;

    private int runsPerProblem;

    private VehicleRoutingProblem problemInstances[];
    private File inputFiles[];

    private Program(ProgramArgs args)
    {
        List<AlgorithmFactory> factoryList = new ArrayList<>(2);
        if (args.useGarridoRiff)
            factoryList.add(new GarridoRiffAlgorithmFactory());
        if (args.useJsprit)
            factoryList.add(new JspritAlgorithmFactory());
        for (AlgorithmFactory factory : factoryList)
            factory.setTimeThreshold(args.timeThresholdInMs);
        algorithmFactories = factoryList.toArray(new AlgorithmFactory[0]);

        problemParser = args.timeWindows
            ? new SolomonFileReader()
            : new Tsplib95FileReader();
        solutionSerializer = new AugeratFormatSolutionSerializer();

        problemDirectory = args.problemDirectory;
        solutionDirectory = args.solutionDirectory;
        logDirectory = args.logDirectory;

        runsPerProblem = args.runsPerProblem;
    }

    private void start() throws Exception
    {
        logDirectory.mkdirs();
        Logger logger = createLogger(logDirectory);

        solutionDirectory.mkdirs();

        parseProblemInstances(logger);

        BenchmarkRunnerArgs benchmarkArgs = new BenchmarkRunnerArgs();
        benchmarkArgs.problemInstances = problemInstances;
        benchmarkArgs.algorithmFactories = algorithmFactories;
        benchmarkArgs.logger = logger;
        benchmarkArgs.runsPerProblem = runsPerProblem;
        benchmarkArgs.solutionProcessor = this;

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

        List<VehicleRoutingProblem> problemList = new ArrayList<>(files.length);
        List<File> successfullyParsedFiles = new ArrayList<>(files.length);

        for (File file : files)
        {
            if (file.isDirectory())
            {
                logger.log("Skipping - is directory.");
                continue;
            }

            VehicleRoutingProblem vrp;
            try
            {
                vrp = problemParser.parse(file.getAbsolutePath());
            }
            catch (FileNotFoundException e) //Should not happen
            {
                logger.log("Skipping - problem file not found.");
                logger.log(e);
                continue;
            }
            catch (VrpParseException e)
            {
                logger.log("Skipping - problem parse failure.");
                logger.log(e);
                continue;
            }

            problemList.add(vrp);
            successfullyParsedFiles.add(file);
        }

        problemInstances = problemList.toArray(new VehicleRoutingProblem[0]);
        inputFiles = successfullyParsedFiles.toArray(new File[0]);

        logger.log("Successfully parsed %d files.", problemInstances.length);
    }

    private Logger createLogger(File directory) throws Exception
    {
        String date = new SimpleDateFormat("yyMMdd-HHmmss").format(Calendar.getInstance().getTime());

        String fileName = String.format("log_%s", date);
        String suffix = "";
        String extension = "log";

        File logFile;

        for (int i = 2; ; i++)
        {
            if (i == 100)
                throw new Exception("Could not create a log file");

            String suffixedFileName = String.format("%s%s.%s", fileName, suffix, extension);
            Path filePath = Paths.get(directory.getAbsolutePath(), suffixedFileName);
            logFile = new File(filePath.toUri());

            if (logFile.createNewFile())
                break;

            suffix = String.format("_%d", i);
        }

        return new ConcreteLogger(new PrintWriter(logFile));
    }

    @Override
    public void processSingleRun(VehicleRoutingProblemSolution solution, int runIdx, int problemIdx, int algorithmIdx)
    {

    }

    @Override
    public void aggregateAllRuns(VehicleRoutingProblemSolution[] solutions, int problemIdx, int algorithmIndex)
    {

    }
}
