package pl.pielat.heuristic;

import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import pl.pielat.algorithm.TabuRandomizer;

import java.util.Random;

public abstract class BaseHeuristicProvider<T> extends TabuRandomizer<T>
{
    protected VehicleRoutingProblem vrp;

    public BaseHeuristicProvider(VehicleRoutingProblem vrp, Random random)
    {
        super(random);
        this.vrp = vrp;
    }
}
