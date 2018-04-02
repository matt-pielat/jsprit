package pl.pielat.util;

import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.util.Coordinate;

import java.util.Comparator;

public class RadialJobComparator implements Comparator<Service>
{
    private double _pX;
    private double _pY;

    public RadialJobComparator(Coordinate pivot)
    {
        _pX = pivot.getX();
        _pY = pivot.getY();
    }

    @Override
    public int compare(Service a, Service b)
    {
        Coordinate aC = a.getLocation().getCoordinate();
        Coordinate bC = b.getLocation().getCoordinate();

        double aX = aC.getX() - _pX;
        double aY = aC.getY() - _pY;
        double bX = bC.getX() - _pX;
        double bY = bC.getY() - _pY;

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
