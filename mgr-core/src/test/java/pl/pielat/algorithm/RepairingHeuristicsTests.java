package pl.pielat.algorithm;

import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import pl.pielat.heuristic.Job;
import pl.pielat.heuristic.Route;
import pl.pielat.heuristic.constructive.ConstructiveHeuristic;
import pl.pielat.heuristic.repairing.RepairingHeuristic;
import pl.pielat.heuristic.repairing.concrete.*;
import pl.pielat.util.SimpleConstructiveHeuristic;
import pl.pielat.util.simpleBuilder.SimpleVrpBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Stack;

@RunWith(Parameterized.class)
public class RepairingHeuristicsTests extends HeuristicsTestsBase
{
    @Parameterized.Parameters(name = "{0}")
    public static Object[] heuristicNames()
    {
        return new Object[] { "orOpt", "stringCross", "stringExchange", "stringRelocation", "threeOpt", "twoOpt" };
    }

    @Parameterized.Parameter
    public String heuristicName;

    private RepairingHeuristic buildHeuristic(ProblemInfo problemInfo)
    {
        switch (heuristicName)
        {
            case "orOpt":
                return new OrOpt(problemInfo);
            case "stringCross":
                return new StringCross(problemInfo);
            case "stringExchange":
                return new StringExchange(problemInfo);
            case "stringRelocation":
                return new StringRelocation(problemInfo);
            case "threeOpt":
                return new ThreeOpt(problemInfo);
            case "twoOpt":
                return new TwoOpt(problemInfo);
        }
        throw new RuntimeException();
    }

    private void runTest(ProblemInfo problemInfo)
    {
        RepairingHeuristic heuristic = buildHeuristic(problemInfo);

        ArrayList<Job> jobs = new ArrayList<>(problemInfo.jobs);
        Collections.shuffle(jobs);

        for (int i = jobs.size(); i > 0; i--)
        {
            System.out.printf("Start (max route length = %d)", i);

            ArrayList<Route> routes = new ArrayList<>();
            ConstructiveHeuristic constructiveHeuristic = new SimpleConstructiveHeuristic(problemInfo, i);
            constructiveHeuristic.insertJobs(routes, jobs);

            int countBefore = routes.size();
            double costBefore = Route.calculateCost(routes);

            heuristic.improveRoutes(routes);
            checkSolutionConsistency(routes, problemInfo);

            int countAfter = routes.size();
            double costAfter = Route.calculateCost(routes);

            Assert.assertTrue(countBefore >= countAfter);
            Assert.assertTrue(costBefore >= costAfter);

            System.out.print(" DONE!");

            if (countBefore != countAfter)
                System.out.printf(" route count %d->%d", countBefore, countAfter);
            else
                System.out.printf(" route count %d", countBefore);

            if (costBefore != costAfter)
                System.out.printf("; total cost %.0f->%.0f", costBefore, costAfter);
            else
                System.out.printf("; total cost %.0f", costBefore);

            System.out.println();
        }
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
