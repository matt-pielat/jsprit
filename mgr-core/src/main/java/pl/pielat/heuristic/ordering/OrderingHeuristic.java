package pl.pielat.heuristic.ordering;

import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Delivery;
import pl.pielat.heuristic.BaseHeuristic;

import java.util.List;

public abstract class OrderingHeuristic extends BaseHeuristic
{
    public OrderingHeuristic(VehicleRoutingProblem vrp)
    {
        super(vrp);
    }

    public abstract void orderUnassignedJobs(List<Delivery> jobs);
}
