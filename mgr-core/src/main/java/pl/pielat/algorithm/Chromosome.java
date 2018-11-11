package pl.pielat.algorithm;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Delivery;
import com.graphhopper.jsprit.core.problem.solution.SolutionCostCalculator;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import pl.pielat.heuristic.Job;
import pl.pielat.heuristic.Route;
import pl.pielat.heuristic.constructive.ConstructiveHeuristic;
import pl.pielat.heuristic.ordering.OrderingHeuristic;
import pl.pielat.heuristic.repairing.RepairingHeuristic;

import java.util.ArrayList;
import java.util.List;

public class Chromosome extends ArrayList<Gene>
{
    public Chromosome()
    {
    }

    public Chromosome(Chromosome other)
    {
        super(other.size());
        for (Gene otherGene : other)
            add(new Gene(otherGene));
    }

    public int getCustomersToInsertCount()
    {
        int result = 0;
        for (Gene g : this)
            result += g.customersToInsert;
        return result;
    }

    public void removeRange(int from, int to)
    {
        for (int i = to - 1; i >= from; i--)
            remove(i);
    }

    public ArrayList<Route> calculateSolution(ProblemInfo problemInfo)
    {
        ArrayList<Job> unassignedJobs = new ArrayList<>(problemInfo.jobs);
        ArrayList<Route> routes = new ArrayList<>();

        for (Gene gene: this)
        {
            OrderingHeuristic oh = gene.orderingHeuristic;
            oh.orderJobs(unassignedJobs);

            ConstructiveHeuristic ch = gene.constructiveHeuristic;
            List<Job> jobsToInsert = unassignedJobs.subList(0, gene.customersToInsert);
            ch.insertJobs(routes, new ArrayList<>(jobsToInsert));
            jobsToInsert.clear();

            RepairingHeuristic ih = gene.improvementHeuristic;
            ih.improveRoutes(routes);
        }

        return routes;
    }



}
