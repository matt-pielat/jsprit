package pl.pielat.program;

import org.junit.Assert;
import org.junit.Test;
import pl.pielat.algorithm.AlgorithmType;
import pl.pielat.util.problemParsing.FileFormatType;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;

public class BenchmarkRunnerTests
{
    private final String simpleProblemFileContent = "NAME : Problem name\n" +
        "COMMENT : Problem comment\n" +
        "TYPE : CVRP\n" +
        "DIMENSION : 3\n" +
        "EDGE_WEIGHT_TYPE : EUC_2D\n" +
        "CAPACITY : 206\n" +
        "NODE_COORD_SECTION\n1 365 689\n2 146 180\n3 792 5\n" +
        "DEMAND_SECTION\n1 0\n2 38\n3 51\n" +
        "DEPOT_SECTION\n1\n-1\n" +
        "EOF";

    @Test
    public void benchmarkRunnerTest()
    {
        BenchmarkRunnerArgs args = new BenchmarkRunnerArgs();

        try
        {
            args.problemFile = File.createTempFile("problemFile", ".tmp");
            args.solutionFile = File.createTempFile("solutionFile", ".tmp");
            args.logFile = File.createTempFile("logFile", ".tmp");
        }
        catch (IOException e)
        {
            Assert.fail();
            e.printStackTrace();
            return;
        }

        args.solutionFile.delete();
        args.logFile.delete();

        try (FileWriter fw = new FileWriter(args.problemFile, false))
        {
            fw.write(simpleProblemFileContent);
        }
        catch (IOException e)
        {
            Assert.fail();
            e.printStackTrace();
            return;
        }

        args.algorithmType = AlgorithmType.GarridoRiff;
        args.problemFileFormat = FileFormatType.Tsplib95;

        args.iterationLimit = 10;
        args.timeLimitInMs = 1000;

        BenchmarkRunner runner = new BenchmarkRunner(args);
        runner.run();

        String solutionContent;
        try
        {
            byte[] encoded = Files.readAllBytes(args.solutionFile.toPath());
            solutionContent = new String(encoded, Charset.defaultCharset());
        }
        catch (IOException e)
        {
            Assert.fail();
            e.printStackTrace();
            return;
        }

        Assert.assertTrue(!solutionContent.isEmpty());

        args.solutionFile.delete();
        args.logFile.delete();
    }
}
