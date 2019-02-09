package pl.pielat.algorithm;

import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.SolutionCostCalculator;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import pl.pielat.heuristic.Route;

import java.util.ArrayList;

public class ObjectiveFunction implements SolutionCostCalculator
{
    private VehicleRoutingProblem vrp;
    private double fixedRoutePenalty;
    private double unassignedJobPenalty;

    public ObjectiveFunction(ExtendedProblemDefinition vrp, boolean penalizeRouteCount)
    {
        this.vrp = vrp.vrp;
        this.unassignedJobPenalty = vrp.maxCost * 3;
        this.fixedRoutePenalty = penalizeRouteCount ? vrp.maxCost * 2 : 0;
    }

    @Override
    public double getCosts(VehicleRoutingProblemSolution solution)
    {
        double costs = 0;
        for (VehicleRoute route : solution.getRoutes())
        {
            TourActivity prevAct = route.getStart();
            for (TourActivity act : route.getActivities())
            {
                costs += vrp.getTransportCosts().getTransportCost(prevAct.getLocation(), act.getLocation(), prevAct.getEndTime(), route.getDriver(), route.getVehicle());
                prevAct = act;
            }
            costs += vrp.getTransportCosts().getTransportCost(prevAct.getLocation(), route.getEnd().getLocation(), prevAct.getEndTime(), route.getDriver(), route.getVehicle());
            costs += fixedRoutePenalty;
        }
        for (Job j : solution.getUnassignedJobs())
            costs += unassignedJobPenalty;
        return costs;
    }

    public double getCosts(ArrayList<Route> solution)
    {
        double costs = 0;
        for (Route route : solution)
        {
            costs += route.getCost();
            costs += fixedRoutePenalty;
        }
        return costs;
    }
}
