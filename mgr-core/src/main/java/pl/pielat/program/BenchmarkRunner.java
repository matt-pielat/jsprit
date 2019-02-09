package pl.pielat.program;

import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.selector.SelectBest;
import com.graphhopper.jsprit.core.algorithm.selector.SolutionSelector;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import pl.pielat.algorithm.ExtendedProblemDefinition;
import pl.pielat.algorithm.ObjectiveFunction;
import pl.pielat.algorithm.factory.AlgorithmFactory;
import pl.pielat.algorithm.factory.GarridoRiffFactory;
import pl.pielat.algorithm.factory.JspritFactory;
import pl.pielat.util.logging.ConcreteLogger;
import pl.pielat.util.logging.Logger;
import pl.pielat.util.logging.Multilogger;
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

    private ExtendedProblemDefinition epd;
    private AlgorithmFactory algorithmFactory;

    private VrpSolutionSerializer solutionSerializer;
    private File solutionFile;

    private boolean errorEncountered = false;

    public BenchmarkRunner(BenchmarkRunnerArgs args)
    {
        try
        {
            logger = createLogger(args.logFile);
        }
        catch (IOException e)
        {
            e.printStackTrace();
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

        solutionFile = args.solutionFile;
        try
        {
            if (solutionFile.exists())
                solutionFile.delete();
            solutionFile.createNewFile();
        }
        catch (IOException e)
        {
            logger.log("Could not create solution file.");
            logger.log(e);
            errorEncountered = true;
            return;
        }

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

        long startTime = System.nanoTime();
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
        long endTime = System.nanoTime();

        if (solutions.isEmpty())
        {
            logger.log("No solutions found.");
            return;
        }
        VehicleRoutingProblemSolution bestSolution = getBestSolution(solutions);

        try (PrintWriter writer = new PrintWriter(new FileOutputStream(solutionFile, false)))
        {
            solutionSerializer.serialize(bestSolution, endTime - startTime, writer);
        }
        catch (FileNotFoundException e)
        {
            logger.log("Could not find solution file.");
            logger.log(e);
        }

        logger.log("Terminated successfully.");
    }

    private Logger createLogger(File logFile) throws IOException
    {
        Logger stdoutLogger = new ConcreteLogger(
            new PrintWriter(
                new OutputStreamWriter(System.out)));

        if (logFile != null)
        {
            logFile.createNewFile();

            Logger fileLogger = new ConcreteLogger(
                new PrintWriter(
                    new FileOutputStream(logFile, true)));

            return new Multilogger(stdoutLogger, fileLogger);
        }

        return stdoutLogger;
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
}
