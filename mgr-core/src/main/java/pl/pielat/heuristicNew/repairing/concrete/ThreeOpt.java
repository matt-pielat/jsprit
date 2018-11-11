package pl.pielat.heuristicNew.repairing.concrete;

import pl.pielat.heuristicNew.Place;
import pl.pielat.heuristicNew.Route;
import pl.pielat.heuristicNew.repairing.RepairingHeuristic;

import java.util.ArrayList;

public class ThreeOpt extends RepairingHeuristic
{
    private static final double LOCAL_MINIMUM_FLAG = Double.NEGATIVE_INFINITY;

    public ThreeOpt(ProblemInfo info)
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
        Route oldRoute = allRoutes.get(routeIndex);

        for (int i = 0; i <= oldRoute.length() - 4; i++)
        {
            int maxK = oldRoute.length() - (i > 0 ? 0 : 1);
            for (int j = i + 2; j <= oldRoute.length() - 2; j++)
            {
                for (int k = j + 2; k <= maxK; k++)
                {
                    Route newRoute = createRoute(oldRoute.length());
                    double newCost = threeOptSwap(oldRoute, newRoute, oldCost, i, j, k);

                    if (newCost + EPSILON < oldCost)
                    {
                        oldRoute.replaceInternals(newRoute);
                        return newCost;
                    }
                }
            }
        }

        return LOCAL_MINIMUM_FLAG;
    }

    @SuppressWarnings("Duplicates")
    private double threeOptSwap(Route oldRoute, Route newRoute, double oldCost, int i, int j, int k)
    {
        double ijDist, jkDist, kiDist, jiDist, kjDist, ikDist;
        ijDist = jkDist = kiDist = jiDist = kjDist = ikDist = 0;

        int q;
        if (i > 0)
            kiDist += getCost(depot, oldRoute.getFirst());
        for (q = 1; q < i; q++)
            kiDist += getCost(oldRoute, q - 1, q);
        for (q = i + 1; q < j; q++)
            ijDist += getCost(oldRoute, q - 1, q);
        for (q = j + 1; q < k; q++)
            jkDist += getCost(oldRoute, q - 1, q);
        for (q = k + 1; q < oldRoute.length(); q++)
            kiDist += getCost(oldRoute, q - 1, q);
        if (k < oldRoute.length())
            kiDist += getCost(oldRoute.getLast(), depot);

        if (transportAsymmetry)
        {
            if (i > 0)
                ikDist += getCost(oldRoute.getFirst(), depot);
            for (q = 1; q < i; q++)
                ikDist += getCost(oldRoute, q, q - 1);
            for (q = i + 1; q < j; q++)
                jiDist += getCost(oldRoute, q, q - 1);
            for (q = j + 1; q < k; q++)
                kjDist += getCost(oldRoute, q, q - 1);
            for (q = k + 1; q < oldRoute.length(); q++)
                ikDist += getCost(oldRoute, q, q - 1);
            if (k < oldRoute.length())
                ikDist += getCost(depot, oldRoute.getLast());
        }
        else
        {
            ikDist = kiDist;
            kjDist = jkDist;
            jiDist = ijDist;
        }

        for (int b = 7; b >= 0; b--)
        {
            boolean ijReversed = (b & 1) > 0;
            boolean jkReversed = (b & 2) > 0;
            boolean kiReversed = (b & 4) > 0;

            double subroutesDistances = 0;
            subroutesDistances += ijReversed ? jiDist : ijDist;
            subroutesDistances += jkReversed ? kjDist : jkDist;
            subroutesDistances += kiReversed ? ikDist : kiDist;

            Place fromA = ijReversed ? getEndVertex(oldRoute, i) : getStartVertex(oldRoute, j);
            Place toA = jkReversed ? getStartVertex(oldRoute, k) : getEndVertex(oldRoute, j);

            Place fromB = jkReversed ? getEndVertex(oldRoute, j) : getStartVertex(oldRoute, k);
            Place toB = kiReversed ? getStartVertex(oldRoute, i) : getEndVertex(oldRoute, k);

            Place fromC = kiReversed ? getEndVertex(oldRoute, k) : getStartVertex(oldRoute, i);
            Place toC = ijReversed ? getStartVertex(oldRoute, j) : getEndVertex(oldRoute, i);

            double newCost = subroutesDistances + getCost(fromA, toA) + getCost(fromB, toB) + getCost(fromC, toC);
            if (oldCost > newCost + EPSILON)
            {
                if (kiReversed)
                {
                    for (int a = oldRoute.length() - 1; a >= k; a--)
                        newRoute.add(oldRoute.getFromStart(a));
                }
                else
                {
                    for (int a = 0; a < i; a++)
                        newRoute.add(oldRoute.getFromStart(a));
                }
                if (ijReversed)
                {
                    for (int a = j - 1; a >= i; a--)
                        newRoute.add(oldRoute.getFromStart(a));
                }
                else
                {
                    for (int a = i; a < j; a++)
                        newRoute.add(oldRoute.getFromStart(a));
                }
                if (jkReversed)
                {
                    for (int a = k - 1; a >= j; a--)
                        newRoute.add(oldRoute.getFromStart(a));
                }
                else
                {
                    for (int a = j; a < k; a++)
                        newRoute.add(oldRoute.getFromStart(a));
                }
                if (kiReversed)
                {
                    for (int a = i - 1; a >= 0; a--)
                        newRoute.add(oldRoute.getFromStart(a));
                }
                else
                {
                    for (int a = k; a < oldRoute.length(); a++)
                        newRoute.add(oldRoute.getFromStart(a));
                }

                if (!timeWindows || newRoute.areTimeWindowsValid())
                {
                    return newCost;
                }
                newRoute.removeAll();
            }

            if (!transportAsymmetry && !timeWindows)
                continue;

            newCost = subroutesDistances + getCost(fromC, toA) + getCost(fromA, toB) + getCost(fromB, toC);
            if (oldCost > newCost + EPSILON)
            {
                if (kiReversed)
                {
                    for (int a = oldRoute.length() - 1; a >= k; a--)
                        newRoute.add(oldRoute.getFromStart(a));
                }
                else
                {
                    for (int a = 0; a < i; a++)
                        newRoute.add(oldRoute.getFromStart(a));
                }
                if (jkReversed)
                {
                    for (int a = k - 1; a >= j; a--)
                        newRoute.add(oldRoute.getFromStart(a));
                }
                else
                {
                    for (int a = j; a < k; a++)
                        newRoute.add(oldRoute.getFromStart(a));
                }
                if (ijReversed)
                {
                    for (int a = j - 1; a >= i; a--)
                        newRoute.add(oldRoute.getFromStart(a));
                }
                else
                {
                    for (int a = i; a < j; a++)
                        newRoute.add(oldRoute.getFromStart(a));
                }
                if (kiReversed)
                {
                    for (int a = i - 1; a >= 0; a--)
                        newRoute.add(oldRoute.getFromStart(a));
                }
                else
                {
                    for (int a = k; a < oldRoute.length(); a++)
                        newRoute.add(oldRoute.getFromStart(a));
                }

                if (!timeWindows || newRoute.areTimeWindowsValid())
                {
                    return newCost;
                }
                newRoute.removeAll();
            }
        }

        return oldCost;
    }

    private double getCost(Route route, int from, int to)
    {
        return getCost(route.getFromStart(from), route.getFromStart(to));
    }

    private Place getStartVertex(Route route, int edgeIndex)
    {
        if (edgeIndex == 0)
            return depot;
        return route.getFromStart(edgeIndex - 1);
    }

    private Place getEndVertex(Route route, int edgeIndex)
    {
        if (edgeIndex == route.length())
            return depot;
        return route.getFromStart(edgeIndex);
    }
}
