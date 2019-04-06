package pl.pielat.program;

import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.selector.SelectBest;
import com.graphhopper.jsprit.core.algorithm.selector.SolutionSelector;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import pl.pielat.algorithm.*;
import pl.pielat.algorithm.factory.AlgorithmFactory;
import pl.pielat.algorithm.factory.GarridoRiffFactory;
import pl.pielat.algorithm.factory.JspritFactory;
import pl.pielat.util.Diagnostics;
import pl.pielat.util.logging.*;
import pl.pielat.util.metadata.AlgorithmRunMetadataGatherer;
import pl.pielat.util.metadata.EhDvrpStatistics;
import pl.pielat.util.metadata.EhDvrpStatisticsGatherer;
import pl.pielat.util.problemParsing.FileFormatType;
import pl.pielat.util.problemParsing.SolomonFileReader;
import pl.pielat.util.problemParsing.Tsplib95FileReader;
import pl.pielat.util.problemParsing.VrpFileParser;
import pl.pielat.util.solutionSerialization.VrpSolutionSerializer;
import pl.pielat.util.solutionSerialization.XmlSolutionSerializer;

import java.io.*;
import java.util.Collection;

public class BenchmarkRunner
{
    public static void main(String[] rawArgs)
    {
        BenchmarkRunnerArgs args = BenchmarkRunnerArgs.parse(rawArgs);
        BenchmarkRunner program = new BenchmarkRunner(args);
        program.run();
    }

    private Logger logger;
    private Logger diagnosticLogger;

    private AlgorithmFactory algorithmFactory;
    private VrpSolutionSerializer solutionSerializer;
    private EhDvrpStatisticsGatherer statisticsGatherer;

    private ExtendedProblemDefinition epd;
    private File solutionFile;

    private boolean errorEncountered = false;

    private long minIntermediateCostDelay = -1;

    public BenchmarkRunner(BenchmarkRunnerArgs args)
    {
        try
        {
            logger = createLogger(args.logFile, true);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return;
        }

        try
        {
            if (args.diagnosticLogFile != null)
            {
                logger.log("Creating diagnostic log file \"%s\".", args.diagnosticLogFile.getAbsolutePath());
                Diagnostics.Logger = createLogger(args.diagnosticLogFile, false);
                Diagnostics.Enabled = true;
                Diagnostics.Logger.log("Initialized.");
            }
        }
        catch (IOException e)
        {
            logger.log("Failed to create diagnostic logger.");
            logger.log(e);
            errorEncountered = true;
            return;
        }

        logger.log("Processing problem file \"%s\".", args.problemFile.getAbsolutePath());

        VrpFileParser parser;
        try
        {
            parser = getParserForFileType(args.problemFileFormat);
        }
        catch (Exception e)
        {
            logger.log(e);
            errorEncountered = true;
            return;
        }

        String fileName = args.problemFile.getName();

        try
        {
            VehicleRoutingProblem vrp = parser.parse(args.problemFile.getAbsolutePath());
            boolean timeWindows = parser.timeWindowsDetected();
            boolean transportAsymmetry = parser.transportAsymmetryDetected();
            logger.log("Problem instance file parsed.");

            epd = new ExtendedProblemDefinition(fileName, vrp, timeWindows, transportAsymmetry);
        }
        catch (Exception e)
        {
            logger.log("Failed to parse problem instance file.");
            logger.log(e);
            errorEncountered = true;
            return;
        }

        switch (args.algorithmType)
        {
            case GarridoRiff:
                GarridoRiffFactory gr = new GarridoRiffFactory();

                if (args.chromosomeSize > 0)
                    gr.setChromosomeSize(args.chromosomeSize);
                if (args.populationSize > 0)
                    gr.setChromosomeSize(args.populationSize);
                if (args.offspringSize > 0)
                    gr.setChromosomeSize(args.offspringSize);

                statisticsGatherer = new EhDvrpStatisticsGatherer();
                gr.setStatisticsGatherer(statisticsGatherer);

                algorithmFactory = gr;
                break;
            case Jsprit:
                algorithmFactory = new JspritFactory();
                break;
            default:
                logger.log("Unknown algorithm type");
                errorEncountered = true;
                return;
        }
        if (args.timeLimitInMs > 0)
            algorithmFactory.setTimeLimit(args.timeLimitInMs);
        if (args.iterationLimit > 0)
            algorithmFactory.setTimeLimit(args.iterationLimit);
        if (args.minIntermediateCostDelay >= 0)
            minIntermediateCostDelay = args.minIntermediateCostDelay;

        solutionFile = args.solutionFile;
        solutionSerializer = new XmlSolutionSerializer();
    }

    public void run()
    {
        if (errorEncountered)
        {
            logger.log("BenchmarkRunner failed to run due to some error.");
            return;
        }

        VehicleRoutingAlgorithm vra = algorithmFactory.build(epd);
        AlgorithmRunMetadataGatherer metadataGatherer = new AlgorithmRunMetadataGatherer(
            statisticsGatherer,
            minIntermediateCostDelay);
        vra.addListener(metadataGatherer);

        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

        if (solutions.isEmpty())
        {
            logger.log("No solutions found.");
            return;
        }
        VehicleRoutingProblemSolution bestSolution = getBestSolution(solutions);

        createOrOverwriteSolutionFile(solutionFile);
        try (PrintWriter writer = new PrintWriter(new FileOutputStream(solutionFile, false)))
        {
            solutionSerializer.serialize(bestSolution, metadataGatherer.getMetadata(), writer);
        }
        catch (FileNotFoundException e)
        {
            logger.log("Could not find solution file.");
            logger.log(e);
        }

        logger.log("Successfully serialized at \"%s\".", solutionFile.getAbsolutePath());
    }

    private Logger createLogger(File logFile, boolean logToStdout) throws IOException
    {
        Logger stdoutLogger = null;
        Logger fileLogger = null;

        if (logToStdout)
        {
            stdoutLogger = new ConcreteLogger(
                new PrintWriter(
                    new OutputStreamWriter(System.out)));
        }

        if (logFile != null)
        {
            logFile.getParentFile().mkdirs();
            logFile.createNewFile();

            fileLogger = new ConcreteLogger(
                new PrintWriter(
                    new FileOutputStream(logFile, true)));
        }

        if (stdoutLogger == null && fileLogger == null)
            return new DummyLogger();
        if (stdoutLogger == null)
            return fileLogger;
        if (fileLogger == null)
            return stdoutLogger;
        return new Multilogger(fileLogger, stdoutLogger);
    }

    private VrpFileParser getParserForFileType(FileFormatType formatType)
    {
        switch (formatType)
        {
            case Solomon:
                return new SolomonFileReader();
            case Tsplib95:
                return new Tsplib95FileReader();
        }
        throw new RuntimeException("Invalid file format type.");
    }

    private VehicleRoutingProblemSolution getBestSolution(Collection<VehicleRoutingProblemSolution> solutions)
    {
        ObjectiveFunction costFunction = new ObjectiveFunction(epd, false);
        SolutionSelector solutionSelector = new SelectBest();

        VehicleRoutingProblemSolution bestSolution = solutionSelector.selectSolution(solutions);
        bestSolution.setCost(costFunction.getCosts(bestSolution));

        return bestSolution;
    }

    private void createOrOverwriteSolutionFile(File file)
    {
        try
        {
            if (file.exists())
                file.delete();
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        catch (IOException e)
        {
            logger.log("Could not create solution file.");
            logger.log(e);
            errorEncountered = true;
            return;
        }
    }
}
