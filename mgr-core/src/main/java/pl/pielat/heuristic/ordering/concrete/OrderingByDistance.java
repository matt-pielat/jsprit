package pl.pielat.heuristic.ordering.concrete;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Delivery;
import pl.pielat.heuristic.ordering.OrderingHeuristic;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class OrderingByDistance extends OrderingHeuristic
{
    private class DemandComparator implements Comparator<Delivery>
    {
        @Override
        public int compare(Delivery o1, Delivery o2)
        {
            if (_distances.get(o1) > _distances.get(o2))
                return 1;
            else if (_distances.get(o1) < _distances.get(o2))
                return -1;
            return 0;
        }
    }

    private boolean _increasing;
    private boolean _fromDepot;

    private HashMap<Delivery, Double> _distances;
    private DemandComparator _comparator = new DemandComparator();

    public OrderingByDistance(VehicleRoutingProblem vrp, boolean increasing, boolean fromDepot)
    {
        super(vrp);
        _increasing = increasing;
        _fromDepot = fromDepot;
    }

    @Override
    public void orderUnassignedJobs(List<Delivery> jobs)
    {
        _distances = new HashMap<>(jobs.size());

        Location from = getDepotLocation();
        Location to = getDepotLocation();
        for (Delivery key : jobs)
        {
            if (_fromDepot)
                to = key.getLocation();
            else
                from = key.getLocation();

            double distance = getDistance(from, to);
            _distances.put(key, distance);
        }

        Collections.sort(jobs, _comparator);

        if (!_increasing)
            Collections.reverse(jobs);
    }
}
