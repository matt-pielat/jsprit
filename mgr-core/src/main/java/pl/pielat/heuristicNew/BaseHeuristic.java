package pl.pielat.heuristicNew;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.cost.TransportDistance;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class BaseHeuristic
{
    public class ProblemInfo
    {
        public final boolean transportAsymmetry;
        public final boolean timeWindows;
        public final TransportCostFunction costFunction;
        public final int vehicleCapacity;
        public final Place depot;

        public ProblemInfo(VehicleRoutingProblem vrp, boolean transportAsymmetry, boolean timeWindows)
        {
            this.transportAsymmetry = transportAsymmetry;
            this.timeWindows = timeWindows;

            final TransportDistance distanceFunction = vrp.getTransportCosts();
            Vehicle vehicle = vrp.getVehicles().iterator().next();
            Location depotLocation = vehicle.getStartLocation();
            TimeWindow schedulingHorizon = new TimeWindow(vehicle.getEarliestDeparture(), vehicle.getLatestArrival());

            costFunction = new TransportCostFunction() {
                @Override
                public double getCost(Place from, Place to)
                {
                    return distanceFunction.getDistance(from.location, to.location, 0, null);
                }
            };
            vehicleCapacity = vehicle.getType().getCapacityDimensions().get(0);
            depot = new Place(depotLocation, schedulingHorizon);
        }
    }

    public interface TransportCostFunction
    {
        double getCost(Place from, Place to);
    }

    protected static final double EPSILON = 1e-5;
    private static final int INITIAL_ROUTE_CAPACITY = 10;

    private final ProblemInfo problemInfo;

    protected final boolean transportAsymmetry;
    protected final boolean timeWindows;
    protected final int vehicleCapacity;
    protected final Place depot;
    private final TransportCostFunction costFunction;

    protected BaseHeuristic(ProblemInfo info)
    {
        if (info.transportAsymmetry && info.timeWindows)
        {
            throw new IllegalArgumentException(
                "Transport asymmetry and time windows cannot be enabled at the same time.");
        }

        problemInfo = info;

        transportAsymmetry = info.transportAsymmetry;
        timeWindows = info.timeWindows;
        vehicleCapacity = info.vehicleCapacity;
        depot = info.depot;
        costFunction = info.costFunction;
    }

    protected double getCost(Place from, Place to)
    {
        return costFunction.getCost(from, to);
    }

    protected Route createRoute(int initialCapacity)
    {
        return new Route(problemInfo, initialCapacity);
    }

    protected Route createRoute(Job job)
    {
        Route route = new Route(problemInfo, INITIAL_ROUTE_CAPACITY);
        route.add(job);
        return route;
    }

    protected Route createRoute(List<Job> jobs)
    {
        int initialCapacity = jobs.size() > INITIAL_ROUTE_CAPACITY ? jobs.size() : INITIAL_ROUTE_CAPACITY;
        Route route = new Route(problemInfo, initialCapacity);
        route.addAll(jobs);
        return route;
    }
}
