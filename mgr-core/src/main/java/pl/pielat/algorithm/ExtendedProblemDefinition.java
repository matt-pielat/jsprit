package pl.pielat.algorithm;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Service;

import java.util.Collection;

public class ExtendedProblemDefinition
{
    public final String id;
    public final VehicleRoutingProblem vrp;
    public final boolean timeWindows;
    public final boolean transportAsymmetry;
    public final double maxCost;

    public ExtendedProblemDefinition(String id, VehicleRoutingProblem vrp,
                                     boolean timeWindows, boolean transportAsymmetry)
    {
        this.id = id;
        this.vrp = vrp;
        this.timeWindows = timeWindows;
        this.transportAsymmetry = transportAsymmetry;
        this.maxCost = getMaxCost();
    }

    private double getMaxCost()
    {
        Location depot = vrp.getVehicles().iterator().next().getStartLocation();
        Collection<Job> jobs = vrp.getJobs().values();

        double maxCost = 0;

        for (Job j : jobs) {
            Service service = (Service)j;

            double cost = vrp.getTransportCosts().getTransportCost(
                depot, service.getLocation(), 0, null, null);
            if (cost > maxCost)
                maxCost = cost;
        }

        if (!transportAsymmetry)
            return maxCost;

        for (Job j : jobs) {
            Service service = (Service)j;

            double cost = vrp.getTransportCosts().getTransportCost(
                service.getLocation(), depot, 0, null, null);
            if (cost > maxCost)
                maxCost = cost;
        }

        return maxCost;
    }
}
