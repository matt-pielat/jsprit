package pl.pielat.heuristic.ordering.concrete;

import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Delivery;
import pl.pielat.heuristic.ordering.OrderingHeuristic;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class OrderingByDemand extends OrderingHeuristic
{
    private class DemandComparator implements Comparator<Delivery>
    {
        @Override
        public int compare(Delivery o1, Delivery o2)
        {
            return getDemand(o1) - getDemand(o2);
        }
    }

    private boolean _increasing;
    private DemandComparator _comparator = new DemandComparator();

    public OrderingByDemand(VehicleRoutingProblem vrp, boolean increasing)
    {
        super(vrp);
        _increasing = increasing;
    }

    @Override
    public void orderUnassignedJobs(List<Delivery> jobs)
    {
        Collections.sort(jobs, _comparator);
        if (!_increasing)
            Collections.reverse(jobs);
    }
}
