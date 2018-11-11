package pl.pielat.heuristic.repairing.concrete;

import pl.pielat.algorithm.ProblemInfo;
import pl.pielat.heuristic.Route;
import pl.pielat.heuristic.repairing.RepairingHeuristic;

import java.util.ArrayList;

public class TwoOpt extends RepairingHeuristic
{
    private static final double LOCAL_MINIMUM_FLAG = Double.NEGATIVE_INFINITY;

    public TwoOpt(ProblemInfo info)
    {
        super(info);
    }

    @Override
    public void improveRoutes(ArrayList<Route> routes)
    {
        for (int i = 0; i < routes.size(); i++)
        {
            double cost = routes.get(i).getCost();
            while (cost != LOCAL_MINIMUM_FLAG)
                cost = improveRouteOnce(routes, i, cost);
        }
    }

    private double improveRouteOnce(ArrayList<Route> allRoutes, int routeIndex, double oldCost)
    {
        Route route = allRoutes.get(routeIndex);

        for (int x = 1; x < route.length() - 1; x++)
        {
            for (int y = x + 1; y < route.length(); y++)
            {
                boolean ok = true;

                Route routeCopy = route.copy();
                twoOptSwap(routeCopy, x, y);

                double newCost = routeCopy.getCost();
                if (newCost + EPSILON < oldCost)
                {
                    if (timeWindows && !routeCopy.areTimeWindowsValid())
                        routeCopy.reverse();

                    allRoutes.set(routeIndex, routeCopy);
                    return newCost;
                }

                if (!transportAsymmetry)
                    continue;

                routeCopy.reverse();

                newCost = routeCopy.getCost();
                if (newCost + EPSILON < oldCost)
                {
                    allRoutes.set(routeIndex, routeCopy);
                    return newCost;
                }
            }
        }
        return LOCAL_MINIMUM_FLAG;
    }

    private void twoOptSwap(Route route, int x, int y)
    {
        Route.copy(route, x, route, x, y - x, true);
    }
}
