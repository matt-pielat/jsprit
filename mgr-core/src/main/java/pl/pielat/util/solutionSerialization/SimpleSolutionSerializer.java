package pl.pielat.util.solutionSerialization;

import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;

import java.io.PrintWriter;
import java.util.Collection;

public class SimpleSolutionSerializer implements VrpSolutionSerializer
{
    @Override
    public void serialize(VehicleRoutingProblemSolution solution, long millisecondsElapsed, PrintWriter writer)
    {
        Collection<VehicleRoute> routes = solution.getRoutes();
        double cost = solution.getCost();

        writer.format("Route count: %d", routes.size());
        writer.println();
        writer.format("Cost: %.2f", cost);
    }
}
