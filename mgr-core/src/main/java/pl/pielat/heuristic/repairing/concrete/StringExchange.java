package pl.pielat.heuristic.repairing.concrete;

import pl.pielat.algorithm.ProblemInfo;
import pl.pielat.heuristic.Place;
import pl.pielat.heuristic.Route;
import pl.pielat.heuristic.repairing.RepairingHeuristic;

import java.util.List;

public class StringExchange extends RepairingHeuristic
{
    public StringExchange(ProblemInfo info)
    {
        super(info);
    }

    @Override
    public String getId()
    {
        return "String exchange";
    }

    @Override
    public void improveRoutes(List<Route> routes)
    {
        while (findAnyImprovement(routes));
    }

    public boolean findAnyImprovement(List<Route> routes)
    {
        for (int a = 1; a < routes.size(); a++)
        {
            Route alpha = routes.get(a);

            for (int b = 0; b < a; b++)
            {
                Route beta = routes.get(b);

                if (performExchange(routes, a, b, 2, 2) ||
                    performExchange(routes, a, b, 2, 1) ||
                    performExchange(routes, a, b, 1, 2) ||
                    performExchange(routes, a, b, 1, 1))
                {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean performExchange(List<Route> routes, int alphaIdx, int betaIdx, int kA, int kB)
    {
        Route alpha = routes.get(alphaIdx);
        Route beta = routes.get(betaIdx);

        int alphaDemand = alpha.getDemand();
        int betaDemand = beta.getDemand();

        for (int i = 0; i < alpha.length() - kA + 1; i++)
        {
            int alphaStringDemand = getPartialDemand(alpha, i, kA);

            for (int j = 0; j < beta.length() - kB + 1; j++)
            {
                int betaStringDemand = getPartialDemand(beta, j, kB);

                if (alphaDemand - alphaStringDemand + betaStringDemand > vehicleCapacity)
                    continue;
                if (betaDemand - betaStringDemand + alphaStringDemand > vehicleCapacity)
                    continue;
                if (getCostDelta(alpha, beta, i, j, kA, kB) > -EPSILON)
                    continue;

                Route alphaNew = createRoute(alpha.length() - kA + kB);
                alphaNew.addAll(alpha, 0, i, false);
                alphaNew.addAll(beta, j, j + kB, false);
                alphaNew.addAll(alpha, i + kA, alpha.length(), false);

                if (timeWindows && !alphaNew.areTimeWindowsValid())
                    continue;

                Route betaNew = createRoute(beta.length() - kB + kA);
                betaNew.addAll(beta, 0, j, false);
                betaNew.addAll(alpha, i, i + kA, false);
                betaNew.addAll(beta, j + kB, beta.length(), false);

                if (timeWindows && !betaNew.areTimeWindowsValid())
                    continue;

                routes.set(alphaIdx, alphaNew);
                routes.set(betaIdx, betaNew);

                return true;
            }
        }

        return false;
    }

    private int getPartialDemand(Route route, int from, int length)
    {
        int result = 0;
        for (int i = from; i < from + length; i++)
            result += route.getFromStart(i).demand;
        return result;
    }

    private Place getRouteNodeSafely(Route route, int idx)
    {
        if (idx == -1 || idx == route.length())
            return depot;
        return route.getFromStart(idx);
    }

    private double getCostDelta(Route alpha, Route beta, int idxA, int idxB, int kA, int kB)
    {
        // BEFORE:
        // ... - bA - sA - ... - eA - aA - ...
        // ... - bB - sB - ... - eB - aB - ...

        // AFTER:
        // ... - bA - sB - ... - eB - aA - ...
        // ... - bB - sA - ... - eA - aB - ...

        Place bA = getRouteNodeSafely(alpha, idxA - 1);
        Place sA = getRouteNodeSafely(alpha, idxA);
        Place eA = getRouteNodeSafely(alpha, idxA + kA - 1);
        Place aA = getRouteNodeSafely(alpha, idxA + kA);

        Place bB = getRouteNodeSafely(beta, idxB - 1);
        Place sB = getRouteNodeSafely(beta, idxB);
        Place eB = getRouteNodeSafely(beta, idxB + kB - 1);
        Place aB = getRouteNodeSafely(beta, idxB + kB);

        double costDelta =
            - getCost(bA, sA) - getCost(eA, aA) - getCost(bB, sB) - getCost(eB, aB)
            + getCost(bA, sB) + getCost(eB, aA) + getCost(bB, sA) + getCost(eA, aB);
        return costDelta;
    }
}
