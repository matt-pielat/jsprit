package pl.pielat.toFix;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class FromGeneratorTests extends FileBasedTest
{
    private String vrpDirectory = "D:\\VRP Benchmarks\\From generator\\Problems\\";
    private String resultDirectory = "D:\\VRP Benchmarks\\From generator\\Solutions\\";

    @Before
    public void initialize() throws IOException
    {
        File[] vrpFiles = new File(vrpDirectory).listFiles();

        if (vrpFiles == null)
            throw new IOException();
    }

    @Test
    public void runN20a() {
        final String name = "n20a";
        double cost = runTestFromFile(vrpDirectory, name, resultDirectory, 1000);
        Assert.assertTrue(cost < 13488);
    }

    @Test
    public void runN20b() {
        final String name = "n20b";
        double cost = runTestFromFile(vrpDirectory, name, resultDirectory, 1000);
        Assert.assertTrue(cost < 13488);
    }

    @Test
    public void runN30a() {
        final String name = "n30a";
        double cost = runTestFromFile(vrpDirectory, name, resultDirectory, 1000);
        Assert.assertTrue(cost < 13488);
    }

    @Test
    public void runN40a() {
        final String name = "n40a";
        double cost = runTestFromFile(vrpDirectory, name, resultDirectory, 1000);
        Assert.assertTrue(cost < 13488);
    }
}
