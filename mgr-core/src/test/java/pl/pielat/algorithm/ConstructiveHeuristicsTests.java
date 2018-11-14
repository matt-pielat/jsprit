package pl.pielat.algorithm;

import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import org.junit.Assert;
import org.junit.Test;
import pl.pielat.heuristic.Job;
import pl.pielat.heuristic.Route;
import pl.pielat.heuristic.constructive.ConstructiveHeuristic;
import pl.pielat.heuristic.constructive.concrete.ClarkeWrightHeuristic;
import pl.pielat.heuristic.constructive.concrete.KilbyAlgorithm;
import pl.pielat.heuristic.constructive.concrete.MoleJamesonHeuristic;
import pl.pielat.heuristic.constructive.concrete.SweepAlgorithm;
import pl.pielat.util.simpleBuilder.SimpleVrpBuilder;

import java.util.ArrayList;

public class ConstructiveHeuristicsTests extends SimpleTestsBase
{
    private ProblemInfo problemInfo1;

    public ConstructiveHeuristicsTests()
    {
        VehicleRoutingProblem vrp = new SimpleVrpBuilder()
            .setDepotLocation(0, 0)
            .setVehicleCapacity(500)
            .addJob(100, 0).setDemand(100).build()
            .addJob(0, 100).setDemand(100).build()
            .addJob(-100, 0).setDemand(100).build()
            .addJob(0, -100).setDemand(100).build()
            .addJob(50, 0).setDemand(100).build()
            .addJob(0, 50).setDemand(100).build()
            .addJob(50, 0).setDemand(100).build()
            .addJob(0, 50).setDemand(100).build()
            .addJob(200, 200).setDemand(100).build()
            .addJob(-200, -200).setDemand(100).build()
            .addJob(200, -200).setDemand(100).build()
            .addJob(-200, 200).setDemand(100).build()
            .build();

        EntityConverter converter = new EntityConverter(vrp);
        problemInfo1 = converter.getProblemInfo(false, false);
    }

    @Test
    public void clarkeWrightHeuristicTest1()
    {
        checkSolutionConsistency(new ClarkeWrightHeuristic(problemInfo1), problemInfo1);
    }

    @Test
    public void kilbyAlgorithmTest1()
    {
        checkSolutionConsistency(new KilbyAlgorithm(problemInfo1), problemInfo1);
    }

    @Test
    public void moleJamesonHeuristicTest1()
    {
        checkSolutionConsistency(new MoleJamesonHeuristic(problemInfo1), problemInfo1);
    }

    @Test
    public void sweepAlgorithmTest1()
    {
        checkSolutionConsistency(new SweepAlgorithm(problemInfo1), problemInfo1);
    }

    private void checkSolutionConsistency(ConstructiveHeuristic heuristic, ProblemInfo problemInfo)
    {
        ArrayList<Route> routes = new ArrayList<>();
        ArrayList<Job> jobsToInsert = new ArrayList<>(problemInfo.jobs);

        int counter = 0;
        int expectedCount = jobsToInsert.size();
        boolean[] markedJobs = new boolean[expectedCount];

        heuristic.insertJobs(routes, jobsToInsert);

        for (Route r : routes)
        {
            Assert.assertTrue(r.length() > 0);
            for (int i = 0; i < r.length(); i++)
            {
                Job j = r.getFromStart(i);
                Assert.assertFalse(markedJobs[j.id]);
                markedJobs[j.id] = true;
                counter++;
            }
            Assert.assertTrue(r.getDemand() <= problemInfo.vehicleCapacity);
        }
        Assert.assertEquals(counter, expectedCount);
    }
}
