package pl.pielat.heuristic.repairing.concrete;

import pl.pielat.algorithm.ProblemInfo;
import pl.pielat.heuristic.Place;
import pl.pielat.heuristic.Route;
import pl.pielat.heuristic.repairing.RepairingHeuristic;

import java.util.ArrayList;

public class OrOpt extends RepairingHeuristic
{
    public OrOpt(ProblemInfo info)
    {
        super(info);
    }

    @Override
    public void improveRoutes(ArrayList<Route> routes)
    {
        for (int i = 0; i < routes.size(); i++)
        {
            while (improveRouteOnce(routes, i));
        }
    }

    private boolean improveRouteOnce(ArrayList<Route> allRoutes, int routeIndex)
    {
        Route route = allRoutes.get(routeIndex);

        for (int len = 3; len >= 1; len--)
        {
            for (int from = 0; from <= route.length() - len; from++)
            {
                for (int to = 0; to <= route.length() - len; to++)
                {
                    if (from == to)
                        continue;

                    double costBefore = route.getCost();

                    double delta = getShiftCostDelta(route, from, to, len);
                    if (delta > -EPSILON)
                        continue;

                    if (timeWindows)
                    {
                        Route copy = route.copy();
                        performShift(copy, from, to, len);

                        if (!copy.areTimeWindowsValid())
                            continue;

                        allRoutes.set(routeIndex, copy);
                    }
                    else
                    {
                        performShift(route, from, to, len);
                    }

                    double costAfter = route.getCost();

                    return true;
                }
            }
        }

        return false;
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    private double getShiftCostDelta(Route route, int fromIndex, int toIndex, int segmentLength)
    {
        // Before:
        // ... A - [B - ... - C] - D - ... - E - F ...
        // After:
        // ... A - D' - ... - E' - [B' - ... - C'] - F ...

        // fromIndex = B
        // toIndex = B'

        int a = fromIndex - 1;
        int b = fromIndex;
        int c = fromIndex + segmentLength - 1;
        int d = fromIndex + segmentLength;

        if (toIndex > fromIndex)
            toIndex += segmentLength;

        int e = toIndex - 1;
        int f = toIndex;

        return
            - getCost(route, a, b) - getCost(route, c, d) + getCost(route, a, d)
            + getCost(route, e, b) + getCost(route, c, f) - getCost(route, e, f);
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    private void performShift(Route route, int fromIndex, int toIndex, int segmentLength)
    {
        // Before:
        // ... A - [B - ... - C] - D - ... - E - F ...
        // After:
        // ... A - D' - ... - E' - [B' - ... - C'] - F ...

        // fromIndex = B
        // toIndex = B'

        int b = fromIndex;
        int d = fromIndex + segmentLength;
        int bP = toIndex;

        Route segment = route.subroute(b, d);

        route.remove(b, d);
        route.addAll(bP, segment, false);
    }

    private double getCost(Route route, int fromIndex, int toIndex)
    {
        Place from = fromIndex == -1 ? depot : route.getFromStart(fromIndex);
        Place to = toIndex == route.length() ? depot : route.getFromStart(toIndex);
        return getCost(from, to);
    }
}
