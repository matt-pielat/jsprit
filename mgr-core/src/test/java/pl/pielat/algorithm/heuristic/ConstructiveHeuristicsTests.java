package pl.pielat.algorithm.heuristic;

import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import pl.pielat.algorithm.EntityConverter;
import pl.pielat.algorithm.ProblemInfo;
import pl.pielat.heuristic.Job;
import pl.pielat.heuristic.Route;
import pl.pielat.heuristic.constructive.ConstructiveHeuristic;
import pl.pielat.heuristic.constructive.concrete.ClarkeWrightHeuristic;
import pl.pielat.heuristic.constructive.concrete.KilbyAlgorithm;
import pl.pielat.heuristic.constructive.concrete.MoleJamesonHeuristic;
import pl.pielat.heuristic.constructive.concrete.SweepAlgorithm;
import pl.pielat.util.simpleBuilder.SimpleVrpBuilder;

import java.util.ArrayList;

@RunWith(Parameterized.class)
public class ConstructiveHeuristicsTests extends HeuristicsTestsBase
{
    @Parameterized.Parameters(name = "{0}")
    public static Object[] heuristicNames()
    {
        return new Object[] { "clarkeWright", "kilby", "moleJameson", "sweep" };
    }

    @Parameterized.Parameter
    public String heuristicName;

    private ConstructiveHeuristic buildHeuristic(ProblemInfo problemInfo)
    {
        switch (heuristicName)
        {
            case "clarkeWright":
                return new ClarkeWrightHeuristic(problemInfo);
            case "kilby":
                return new KilbyAlgorithm(problemInfo);
            case "moleJameson":
                return new MoleJamesonHeuristic(problemInfo);
            case "sweep":
                return new SweepAlgorithm(problemInfo);
        }
        throw new RuntimeException();
    }

    private void runTest(ProblemInfo problemInfo)
    {
        Assume.assumeFalse(heuristicName.equals("sweep") && problemInfo.timeWindows);

        ConstructiveHeuristic heuristic = buildHeuristic(problemInfo);
        ArrayList<Job> jobsToInsert = new ArrayList<>(problemInfo.jobs);
        ArrayList<Route> constructedRoutes = new ArrayList<>();

        heuristic.insertJobs(constructedRoutes, jobsToInsert);
        checkSolutionConsistency(constructedRoutes, problemInfo);
    }

    @Test
    public void simple()
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
        ProblemInfo problemInfo = converter.getProblemInfo(false, false);

        runTest(problemInfo);
    }

    @Test
    public void simpleWithFauxTransportAsymmetry()
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
        ProblemInfo problemInfo = converter.getProblemInfo(true, false);

        runTest(problemInfo);
    }
}
