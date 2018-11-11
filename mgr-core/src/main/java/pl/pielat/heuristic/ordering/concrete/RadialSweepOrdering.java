package pl.pielat.heuristic.ordering.concrete;

import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Delivery;
import com.graphhopper.jsprit.core.util.Coordinate;
import pl.pielat.heuristic.ordering.OrderingHeuristic;
import pl.pielat.util.RadialJobComparator;

import java.util.Collections;
import java.util.List;

public class RadialSweepOrdering extends OrderingHeuristic
{
    private boolean _clockwise;
    private boolean _pivotIsFarthestJob;

    private double _depotX;
    private double _depotY;

    public RadialSweepOrdering(VehicleRoutingProblem vrp, boolean pivotIsFarthestJob, boolean clockwise)
    {
        super(vrp);
        _pivotIsFarthestJob = pivotIsFarthestJob;
        _clockwise = clockwise;

        Coordinate depotCoord = getDepotLocation().getCoordinate();
        _depotX = depotCoord.getX();
        _depotY = depotCoord.getY();
    }

    @Override
    public void orderUnassignedJobs(List<Delivery> jobs)
    {
        if (jobs.size() == 0)
            return;

        Coordinate pivot = determinePivot(jobs, _pivotIsFarthestJob);
        RadialJobComparator comparator = new RadialJobComparator(pivot);
//        Collections.sort(jobs, comparator);
        if (_clockwise)
            Collections.reverse(jobs);
    }

    private Coordinate determinePivot(List<Delivery> jobs, boolean farthest)
    {
        Coordinate bestCoord = jobs.get(0).getLocation().getCoordinate();
        double bestDist = getDistanceFromDepotSquared(bestCoord);

        for (int i = 1; i < jobs.size(); i++)
        {
            Coordinate coord = jobs.get(i).getLocation().getCoordinate();
            double dist = getDistanceFromDepotSquared(coord);
            if ((farthest && dist > bestDist) || (!farthest && dist < bestDist))
            {
                bestCoord = coord;
                bestDist = dist;
            }
        }
        return bestCoord;
    }

    private double getDistanceFromDepotSquared(Coordinate jobCoord)
    {
        return (_depotX - jobCoord.getX()) * (_depotX - jobCoord.getX()) +
            (_depotY - jobCoord.getY()) * (_depotY - jobCoord.getY());
    }
}
