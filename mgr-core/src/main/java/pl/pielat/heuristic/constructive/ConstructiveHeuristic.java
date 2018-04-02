package pl.pielat.heuristic.constructive;

import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Delivery;
import pl.pielat.algorithm.MgrRoute;
import pl.pielat.heuristic.BaseHeuristic;

import java.util.List;

public abstract class ConstructiveHeuristic extends BaseHeuristic
{
    public ConstructiveHeuristic(VehicleRoutingProblem vrp)
    {
        super(vrp);
    }

    public abstract void insertJobs(List<MgrRoute> routes, List<Delivery> jobsToInsert);
}
