package pl.pielat.algorithm;


import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.cost.TransportDistance;
import com.graphhopper.jsprit.core.problem.job.Delivery;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import pl.pielat.heuristic.Job;
import pl.pielat.heuristic.Place;
import pl.pielat.heuristic.Route;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EntityConverter
{
    private final List<Job> jobs;
    private final List<Delivery> deliveries;

    private final VehicleRoutingProblem vrp;
    private final Vehicle vehicle;

    public EntityConverter(VehicleRoutingProblem vrp)
    {
        this.vrp = vrp;
        vehicle = vrp.getVehicles().iterator().next();

        int jobCount = vrp.getJobs().size();
        jobs = new ArrayList<>(jobCount);
        deliveries = new ArrayList<>(jobCount);
    }

    public ProblemInfo getProblemInfo(boolean transportAsymmetry, boolean timeWindows)
    {
        final TransportDistance distanceFunction = vrp.getTransportCosts();
        ProblemInfo.TransportCostFunction costFunction = new ProblemInfo.TransportCostFunction() {
            @Override
            public double getCost(Place from, Place to)
            {
                return distanceFunction.getDistance(from.location, to.location, 0, null);
            }
        };

        int vehicleCapacity = vehicle.getType().getCapacityDimensions().get(0);

        Location depotLocation = vehicle.getStartLocation();
        TimeWindow schedulingHorizon = new TimeWindow(vehicle.getEarliestDeparture(), vehicle.getLatestArrival());
        Place depot = new Place(depotLocation, schedulingHorizon);

        return new ProblemInfo(costFunction, transportAsymmetry, timeWindows, vehicleCapacity, depot, getJobs());
    }

    public VehicleRoutingProblemSolution getSolution(Collection<Route> routes, double costToSet)
    {
        Collection<VehicleRoute> jspritRoutes = new ArrayList<>(routes.size());

        for (Route r : routes)
            jspritRoutes.add(getVehicleRoute(r));

        return new VehicleRoutingProblemSolution(jspritRoutes, costToSet);
    }

    private ArrayList<Job> getJobs()
    {
        int i = 0;
        for (com.graphhopper.jsprit.core.problem.job.Job jspritJob : vrp.getJobs().values())
        {
            Delivery delivery = (Delivery)jspritJob;
            deliveries.add(delivery);

            Job job = new Job(delivery, i);
            jobs.add(job);

            i++;
        }
        return new ArrayList<>(jobs);
    }

    private VehicleRoute getVehicleRoute(Route route)
    {
        VehicleRoute.Builder builder = VehicleRoute.Builder.newInstance(vehicle);

        for (int i = 0; i < route.length(); i++)
        {
            Job job = route.getFromStart(i);
            Delivery delivery = deliveries.get(job.id);

            builder.addDelivery(delivery);
        }

        return builder.build();
    }
}
