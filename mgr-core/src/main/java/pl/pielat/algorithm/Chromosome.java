package pl.pielat.algorithm;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Delivery;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.SolutionCostCalculator;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
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

    public VehicleRoutingProblemSolution evaluate(VehicleRoutingProblem vrp, SolutionCostCalculator costCalculator)
    {
        Location depotLocation = vrp.getVehicles().iterator().next().getStartLocation();

        List<Delivery> jobs = new ArrayList<>(vrp.getJobs().size());
        for (Job j : vrp.getJobs().values())
            jobs.add((Delivery)j);

        List<MgrRoute> routes = new ArrayList<>();

        for (Gene gene : this)
        {
            OrderingHeuristic oh = gene.orderingHeuristic;
            oh.orderUnassignedJobs(jobs);

            ConstructiveHeuristic ch = gene.constructiveHeuristic;
            List<Delivery> jobsToInsert = new ArrayList<>(gene.customersToInsert);
            for (int i = 0; i < gene.customersToInsert; i++)
                jobsToInsert.add(jobs.remove(0));
            ch.insertJobs(routes, jobsToInsert);

            //RepairingHeuristic lih = gene.getLocalImprovementHeuristic(); TODO

            RepairingHeuristic ih = gene.improvementHeuristic;
            ih.improveRoutes(routes);
        }

        Vehicle vehicle = vrp.getVehicles().iterator().next();
        List<VehicleRoute> vehicleRoutes = new ArrayList<>(routes.size());
        for (MgrRoute r : routes)
            vehicleRoutes.add(r.toVehicleRoute(vehicle));

        VehicleRoutingProblemSolution result = new VehicleRoutingProblemSolution(vehicleRoutes, 0);
        result.setCost(costCalculator.getCosts(result)); //TODO cost calculated here?
        return result;
    }

}
