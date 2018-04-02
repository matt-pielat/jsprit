package pl.pielat.heuristic.repairing.concrete;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import pl.pielat.algorithm.MgrRoute;
import pl.pielat.heuristic.repairing.RepairingHeuristic;

import java.util.List;

//TODO adjust to matrix-based cost calculation
public class OrOpt extends RepairingHeuristic
{
    public OrOpt(VehicleRoutingProblem vrp)
    {
        super(vrp);
    }

    @Override
    public void improveRoutes(List<MgrRoute> routes)
    {
        for (MgrRoute r : routes)
        {
            while (improveRoute(r));
        }
    }

    private double getShiftDistanceGain(MgrRoute route, int fromIdx, int toIdx, int length, boolean reverse)
    {
        double distanceDelta = 0;

        Location fromA = getLocation(route, fromIdx - 1);
        Location toA = getLocation(route, fromIdx);

        Location fromB = getLocation(route, fromIdx + length - 1);
        Location toB = getLocation(route, fromIdx + length);

        distanceDelta -= getDistance(fromA, toA);
        distanceDelta -= getDistance(fromB, toB);
        distanceDelta += getDistance(fromA, toB);

        Location fromC = getLocation(route, toIdx - 1);
        Location toC = getLocation(route, toIdx);

        distanceDelta -= getDistance(fromC, toC);
        if (reverse)
        {
            if (!distanceIsSymmetric())
            {
                for (int i = 1; i < length; i++)
                {
                    distanceDelta -= getDistance(
                        getLocation(route, fromIdx + i - 1),
                        getLocation(route, fromIdx + i)
                    );
                    distanceDelta += getDistance(
                        getLocation(route, fromIdx + i),
                        getLocation(route, fromIdx + i - 1)
                    );
                }
            }

            distanceDelta += getDistance(fromC, fromB);
            distanceDelta += getDistance(toA, toC);
        }
        else
        {
            distanceDelta += getDistance(fromC, toA);
            distanceDelta += getDistance(fromB, toC);
        }

        return distanceDelta;
    }

    private void performShift(MgrRoute route, int fromIdx, int toIdx, int length, boolean reverse)
    {
        MgrRoute subroute = route.removeSubroute(fromIdx, length);
        if (reverse)
            subroute.reverse();
        if (toIdx > fromIdx)
            toIdx -= length;
        route.addAt(subroute, toIdx);
    }

    private boolean improveRoute(MgrRoute route)
    {
        int maxStringLen = Math.min(route.length() - 1, 3);
        for (int stringLen = maxStringLen; stringLen >= 1; stringLen--)
        {
            for (int stringStart = 0; stringStart < route.length() - stringLen; stringStart++)
            {
                for (int i = 0; i <= route.length(); i++)
                {
                    if (i >= stringStart && i <= stringStart + stringLen)
                        continue;

                    if (getShiftDistanceGain(route, stringStart, i, stringLen, false) < -EPSILON)
                    {
                        performShift(route, stringStart, i, stringLen, false);
                        return true;
                    }

                    if (getShiftDistanceGain(route, stringStart, i, stringLen, true) < -EPSILON)
                    {
                        performShift(route, stringStart, i, stringLen, true);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private Location getLocation(MgrRoute route, int index)
    {
        if (index == -1 || index == route.length())
            return getDepotLocation();
        return route.get(index).getLocation();
    }
}
