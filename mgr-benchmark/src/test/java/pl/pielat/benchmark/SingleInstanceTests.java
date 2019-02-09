package pl.pielat.benchmark;

import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.algorithm.selector.SelectBest;
import com.graphhopper.jsprit.core.algorithm.selector.SolutionSelector;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import org.junit.Assert;
import org.junit.Test;
import pl.pielat.algorithm.GarridoRiff;
import pl.pielat.benchmark.solutionProcessing.XmlSolutionSerializer;
import pl.pielat.util.problemParsing.SolomonFileReader;
import pl.pielat.util.problemParsing.VrpFileParser;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class SingleInstanceTests
{
    @Test
    public void solomonR103()
    {
        VrpFileParser parser = new SolomonFileReader();
        VehicleRoutingProblem vrp;
        try
        {
            vrp = parser.parse("./data/Solomon/Problems/R103.txt");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Assert.fail();
            return;
        }

//        VehicleRoutingAlgorithm vra = new GarridoRiff().createAlgorithm(vrp, false, true);
        VehicleRoutingAlgorithm vra = Jsprit.createAlgorithm(vrp);
        vra.setMaxIterations(500);

        SolutionSelector solutionSelector = new SelectBest();
        VehicleRoutingProblemSolution bestSolution = solutionSelector.selectSolution(vra.searchSolutions());

        XmlSolutionSerializer serializer = new XmlSolutionSerializer();

        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter("D:\\solomon_R103.xml", false))))
        {
            serializer.serialize(bestSolution , -1, writer);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            Assert.fail();
            return;
        }

        Assert.assertTrue(bestSolution != null);
    }
}
