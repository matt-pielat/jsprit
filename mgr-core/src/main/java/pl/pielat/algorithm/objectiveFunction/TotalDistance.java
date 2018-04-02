package pl.pielat.algorithm.objectiveFunction;

import com.graphhopper.jsprit.core.problem.cost.TransportDistance;
import com.graphhopper.jsprit.core.problem.solution.SolutionCostCalculator;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;

public class TotalDistance implements SolutionCostCalculator{
    private final TransportDistance distanceFunction;

    public TotalDistance(TransportDistance distanceFunction)
    {
        this.distanceFunction = distanceFunction;
    }

    @Override
    public double getCosts(VehicleRoutingProblemSolution solution)
    {
        if (!solution.getUnassignedJobs().isEmpty())
            return Double.POSITIVE_INFINITY;

        double costs = 0.;
        for (VehicleRoute route : solution.getRoutes()) {
            TourActivity prevAct = route.getStart();
            for (TourActivity act : route.getActivities()) {
                costs += distanceFunction.getDistance(prevAct.getLocation(), act.getLocation(), prevAct.getEndTime(), route.getVehicle());
                prevAct = act;
            }
            costs += distanceFunction.getDistance(prevAct.getLocation(), route.getEnd().getLocation(), prevAct.getEndTime(), route.getVehicle());
        }

        return costs;
    }
}
