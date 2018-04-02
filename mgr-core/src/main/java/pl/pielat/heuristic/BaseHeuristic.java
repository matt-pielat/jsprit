package pl.pielat.heuristic;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Delivery;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.util.CrowFlyCosts;
import com.graphhopper.jsprit.core.util.EuclideanDistanceCalculator;
import com.graphhopper.jsprit.core.util.VehicleRoutingTransportCostsMatrix;
import pl.pielat.algorithm.MgrRoute;

public abstract class BaseHeuristic
{
    protected static final double EPSILON = 1e-5;

    private Location depotLocation;
    private int vehicleCapacity;
    private VehicleRoutingProblem vrp;
    private boolean symmetricDistances;

    public BaseHeuristic(VehicleRoutingProblem vrp)
    {
        vehicleCapacity = vrp.getTypes().iterator().next().getCapacityDimensions().get(0);
        depotLocation = vrp.getVehicles().iterator().next().getStartLocation();
        this.vrp = vrp;

        if (vrp.getTransportCosts() instanceof VehicleRoutingTransportCostsMatrix)
            symmetricDistances = false;
        else if (vrp.getTransportCosts() instanceof CrowFlyCosts)
            symmetricDistances = true;
        else
        {
            System.out.println("Unknown distance function");
            symmetricDistances = false;
        }
    }

    protected Location getDepotLocation()
    {
        return depotLocation;
    }

    protected int getVehicleCapacity()
    {
        return vehicleCapacity;
    }

    protected int getDemand(Delivery job)
    {
        return job.getSize().get(0);
    }

    protected int getDemand(MgrRoute route)
    {
        int sum = 0;
        for (Delivery job : route)
            sum += getDemand(job);
        return sum;
    }

    protected double getDistance(Location from, Location to)
    {
        return vrp.getTransportCosts().getDistance(from, to, -1, null);
    }

    protected double getDistance(MgrRoute route)
    {
        double result = 0;
        Location prevLocation = getDepotLocation();
        for (Delivery d : route)
        {
            Location location = d.getLocation();
            result += getDistance(prevLocation, location);
            prevLocation = location;
        }
        result += getDistance(prevLocation, getDepotLocation());
        return result;
    }

    protected boolean distanceIsSymmetric()
    {
        return symmetricDistances;
    }

    protected double reverseIfDistanceIsSmaller(MgrRoute route)
    {
        double distance = getDistance(route);
        if (!distanceIsSymmetric())
        {
            route.reverse();
            double revDistance = getDistance(route);

            if (revDistance < distance)
                return revDistance;

            route.reverse(); //Reverse back
        }
        return distance;
    }
}
