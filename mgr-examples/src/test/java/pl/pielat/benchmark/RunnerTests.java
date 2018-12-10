package pl.pielat.benchmark;


import org.junit.Assert;
import org.junit.Test;
import pl.pielat.algorithm.ExtendedProblemDefinition;
import pl.pielat.util.logging.DummyLogger;
import pl.pielat.util.problemParsing.Tsplib95FileReader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class RunnerTests
{
    @Test
    public void jspritCvrpBenchmarkRun()
    {
        AlgorithmFactory factory = new JspritAlgorithmFactory();
        factory.setTimeThreshold(100);

        try
        {
            Path problemDirectory = Files.createTempDirectory("problem");
            Path solutionDirectory = Files.createTempDirectory("solution");

            BenchmarkRunnerArgs args = new BenchmarkRunnerArgs();
            args.algorithmFactories = new AlgorithmFactory[] {factory};
            args.logger = new DummyLogger();
            args.problemInstances = new ExtendedProblemDefinition[0];
            args.runsPerProblem = 10;
            args.solutionProcessor = new DummySolutionProcessor();

            BenchmarkRunner runner = new BenchmarkRunner(args);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }
}
