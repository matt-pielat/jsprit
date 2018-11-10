package pl.pielat.util;

import com.graphhopper.jsprit.core.util.Coordinate;
import pl.pielat.heuristicNew.Job;

import java.util.Comparator;

public class RadialJobComparator implements Comparator<Job>
{
    private double pX;
    private double pY;

    public RadialJobComparator(Coordinate pivot)
    {
        pX = pivot.getX();
        pY = pivot.getY();
    }

    @Override
    public int compare(Job a, Job b)
    {
        Coordinate aC = a.location.getCoordinate();
        Coordinate bC = b.location.getCoordinate();

        double aX = aC.getX() - pX;
        double aY = aC.getY() - pY;
        double bX = bC.getX() - pX;
        double bY = bC.getY() - pY;

        int qDiff = getQuarter(aX, aY) - getQuarter(bX, bY);

        if (qDiff == 0)
        {
            double c = aY * bX - bY * aX;
            if (c > 0)
                return 1;
            if (c < 0)
                return -1;

            double dDiff =  (aX * aX + aY * aY) - (bX * bX + bY * bY);
            if (dDiff == 0)
                return 0;
            return dDiff > 0 ? 1 : -1;
        }
        return qDiff;
    }

    private int getQuarter(double x, double y)
    {
        if (x > 0)
        {
            if (y >= 0)
                return 0;
        }
        else
        {
            if (y > 0)
                return 1;
            if (x < 0)
                return 2;
        }
        return 3;
    }

}
