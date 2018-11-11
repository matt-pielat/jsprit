package pl.pielat.util;

import com.graphhopper.jsprit.core.util.Coordinate;
import pl.pielat.heuristic.Place;

import java.util.Comparator;

public class RadialPlaceSweepComparator implements Comparator<Place>
{
    private double pivotX;
    private double pivotY;
    private double startingTheta;

    public RadialPlaceSweepComparator(Coordinate pivot)
    {
        pivotX = pivot.getX();
        pivotY = pivot.getY();
        startingTheta = - 2 * Math.PI;
    }

    public void setStartingTheta(double theta)
    {
        startingTheta = theta;
    }

    @Override
    public int compare(Place o1, Place o2)
    {
        Coordinate c1 = o1.location.getCoordinate();
        Coordinate c2 = o2.location.getCoordinate();

        double r1 = getTheta(c1.getX(), c1.getY());
        double r2 = getTheta(c2.getX(), c2.getY());

        if (r1 < startingTheta)
            r1 += 2 * Math.PI;
        if (r2 < startingTheta)
            r2 += 2 * Math.PI;

        return Double.compare(r1, r2);
    }

    public double getTheta(double x, double y)
    {
        return Math.atan2(y - pivotY, x - pivotX);
    }
}
