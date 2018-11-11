package pl.pielat.heuristicNew.repairing.concrete;

import pl.pielat.heuristicNew.Route;
import pl.pielat.heuristicNew.repairing.RepairingHeuristic;

import java.util.ArrayList;

public class StringExchange extends RepairingHeuristic
{
    public StringExchange(ProblemInfo info)
    {
        super(info);
    }

    @Override
    public void improveRoutes(ArrayList<Route> routes)
    {
        while (findAnyImprovement(routes));
    }

    public boolean findAnyImprovement(ArrayList<Route> routes)
    {
        for (int a = 1; a < routes.size(); a++)
        {
            Route alpha = routes.get(a);
            double alphaCost = alpha.getCost();

            for (int b = 0; b < a; b++)
            {
                Route beta = routes.get(b);
                double betaCost = beta.getCost();

                double costToBeat = alphaCost + betaCost;

                if (performExchange(routes, a, b, 2, 2, costToBeat) ||
                    performExchange(routes, a, b, 2, 1, costToBeat) ||
                    performExchange(routes, a, b, 1, 2, costToBeat) ||
                    performExchange(routes, a, b, 1, 1, costToBeat))
                {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean performExchange(ArrayList<Route> routes, int alphaIdx, int betaIdx, int kA, int kB, double costToBeat)
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

                if (alphaNew.getCost() + betaNew.getCost() + EPSILON > costToBeat)
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
}
