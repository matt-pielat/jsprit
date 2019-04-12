package pl.pielat.heuristic.ordering.concrete;

import com.graphhopper.jsprit.core.util.Coordinate;
import pl.pielat.algorithm.ProblemInfo;
import pl.pielat.heuristic.Job;
import pl.pielat.heuristic.ordering.OrderingHeuristic;
import pl.pielat.util.RadialPlaceSweepComparator;

import java.util.ArrayList;
import java.util.Collections;

public class OrderingByRadialSweep extends OrderingHeuristic
{
    @Override
    public String getId()
    {
        return "Ordering by radial sweep"
            + (order == Order.DESCENDING ? " (ccw)" : " (cw)");
    }

    public enum SweepStart
    {
        CLOSEST_JOB,
        FARTHEST_JOB
    }

    public enum RadialOrder
    {
        CLOCKWISE,
        COUNTERCLOCKWISE
    }

    private double startTheta;
    private double depotX;
    private double depotY;

    private RadialPlaceSweepComparator comparator;
    private SweepStart sweepStart;

    public OrderingByRadialSweep(ProblemInfo info, RadialOrder order, SweepStart sweepStart)
    {
        super(info, order == RadialOrder.CLOCKWISE ? Order.ASCENDING : Order.DESCENDING);

        if (noCoordinates)
            throw new IllegalArgumentException("This heuristic cannot be used when jobs or depots have no coordinates.");

        Coordinate depotCoordinate = depot.location.getCoordinate();
        depotX = depotCoordinate.getX();
        depotY = depotCoordinate.getY();
        comparator = new RadialPlaceSweepComparator(depotCoordinate);
    }

    @Override
    protected void orderJobsAscending(ArrayList<Job> jobs)
    {
        double bestX = Double.NaN;
        double bestY = Double.NaN;
        double bestDistanceSquared;

        if (sweepStart == SweepStart.CLOSEST_JOB)
            bestDistanceSquared = Double.POSITIVE_INFINITY;
        else
            bestDistanceSquared = Double.NEGATIVE_INFINITY;

        for (Job j : jobs)
        {
            double x = j.location.getCoordinate().getX();
            double y = j.location.getCoordinate().getY();
            double distanceSquared = getDistanceToDepotSquared(x, y);
            if ((bestDistanceSquared > distanceSquared && sweepStart == SweepStart.CLOSEST_JOB) ||
                (bestDistanceSquared < distanceSquared && sweepStart == SweepStart.FARTHEST_JOB))
            {
                bestDistanceSquared = distanceSquared;
                bestX = x;
                bestY = y;
            }
        }

        startTheta = comparator.getTheta(bestX, bestY);
        Collections.sort(jobs, comparator);
    }

    private double getDistanceToDepotSquared(double x, double y)
    {
        return (x - depotX) * (x - depotX) + (y - depotY) * (y - depotY);
    }
}
