package pl.pielat.benchmark;


import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class RunnerTests
{
    @Test
    public void augeratSetA()
    {
        ProgramArgs args = new ProgramArgs();
        File rootDirectory = new File("D:\\VRP Benchmarks\\Set A (Augerat, 1995)");
        File solutionsDirectory = new File(rootDirectory, "Solutions");
        args.garridoRiffOutputDirectory = new File(solutionsDirectory, "Pielat");
        args.jspritOutputDirectory = new File(solutionsDirectory, "jsprit");
        args.logDirectory = new File(rootDirectory, "Logs");
        args.problemDirectory = new File(rootDirectory, "Problems");
        args.runsPerProblem = 10;
        args.timeWindows = false;
        args.timeThresholdInMs = 100;

        Program program = new Program(args);
        try
        {
            program.start();
        }
        catch (IOException e)
        {
            Assert.fail();
        }
    }
}
