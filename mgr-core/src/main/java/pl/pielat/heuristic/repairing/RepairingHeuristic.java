package pl.pielat.heuristic.repairing;

import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import pl.pielat.algorithm.MgrRoute;
import pl.pielat.heuristic.BaseHeuristic;

import java.util.List;

public abstract class RepairingHeuristic extends BaseHeuristic
{
    public RepairingHeuristic(VehicleRoutingProblem vrp)
    {
        super(vrp);
    }

    public abstract void improveRoutes(List<MgrRoute> routes);
}
