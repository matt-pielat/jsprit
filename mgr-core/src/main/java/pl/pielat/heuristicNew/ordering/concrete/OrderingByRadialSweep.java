package pl.pielat.heuristicNew.ordering.concrete;

import com.graphhopper.jsprit.core.util.Coordinate;
import pl.pielat.heuristicNew.Job;
import pl.pielat.heuristicNew.Place;
import pl.pielat.heuristicNew.ordering.OrderingHeuristic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class OrderingByRadialSweep extends OrderingHeuristic
{
    public enum SweepStart
    {
        CLOSEST_JOB,
        FARTHEST_JOB
    }

    public enum TieBreak
    {
        CLOSER_FIRST,
        FARTHER_FIRST
    }

    private class AngleComparator implements Comparator<Place>
    {
        @Override
        public int compare(Place o1, Place o2)
        {
            Coordinate c1 = o1.location.getCoordinate();
            Coordinate c2 = o2.location.getCoordinate();

            double r1 = getTheta(c1.getX(), c1.getY());
            double r2 = getTheta(c2.getX(), c2.getY());

            if (r1 < startTheta)
                r1 += 2 * Math.PI;
            if (r2 < startTheta)
                r2 += 2 * Math.PI;

            return Double.compare(r1, r2);
        }
    }

    private double startTheta;
    private double depotX;
    private double depotY;

    private AngleComparator comparator = new AngleComparator();
    private SweepStart sweepStart;

    protected OrderingByRadialSweep(ProblemInfo info, SweepStart sweepStart)
    {
        super(info);

        Coordinate depotCoordinate = depot.location.getCoordinate();
        depotX = depotCoordinate.getX();
        depotY = depotCoordinate.getY();
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

        startTheta = getTheta(bestX, bestY);
        Collections.sort(jobs, comparator);
    }

    private double getTheta(double x, double y)
    {
        return Math.atan2(y - depotY, x - depotX);
    }

    private double getDistanceToDepotSquared(double x, double y)
    {
        return (x - depotX) * (x - depotX) + (y - depotY) * (y - depotY);
    }
}
