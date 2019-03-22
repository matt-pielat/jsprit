package pl.pielat.program;

import org.junit.Assert;
import org.junit.Test;
import pl.pielat.algorithm.AlgorithmType;
import pl.pielat.util.problemParsing.FileFormatType;

import java.io.File;
import java.io.IOException;

public class SingleInstanceTests
{
    @Test
    public void benchmarkRunnerTest()
    {
        BenchmarkRunnerArgs args = new BenchmarkRunnerArgs();

        try
        {
            args.timeLimitInMs = 30000;
            args.solutionFile = File.createTempFile("solutionFile", ".tmp");
//            args.problemFile = new File();
            args.problemFileFormat = FileFormatType.Tsplib95;
            args.algorithmType = AlgorithmType.GarridoRiff;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            Assert.fail();
        }

        BenchmarkRunner runner = new BenchmarkRunner(args);
        runner.run();

        Assert.assertTrue(true);
    }
}
