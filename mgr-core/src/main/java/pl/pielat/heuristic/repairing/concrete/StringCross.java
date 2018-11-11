package pl.pielat.heuristic.repairing.concrete;

import pl.pielat.algorithm.ProblemInfo;
import pl.pielat.heuristic.Route;
import pl.pielat.heuristic.repairing.RepairingHeuristic;

import java.util.ArrayList;

public class StringCross extends RepairingHeuristic
{
    public StringCross(ProblemInfo info)
    {
        super(info);
    }

    @Override
    public void improveRoutes(ArrayList<Route> routes)
    {
        while (findAnyImprovementCross(routes));
    }

    public boolean findAnyImprovementCross(ArrayList<Route> routes)
    {
        for (int i = 1; i < routes.size(); i++)
        {
            Route alpha = routes.get(i);

            if (alpha.length() < 2)
                continue;

            double alphaCost = alpha.getCost();

            for (int j = 0; j < i; j++)
            {
                Route beta = routes.get(j);

                if (beta.length() < 2)
                    continue;

                double betaCost = beta.getCost();

                if (findImprovementCross(routes, i, j, alphaCost + betaCost))
                    return true;
            }
        }
        return false;
    }

    public boolean findImprovementCross(ArrayList<Route> routes, int alphaIdx, int betaIdx, double costToBeat)
    {
        Route alpha = routes.get(alphaIdx);
        Route beta = routes.get(betaIdx);

        int demandA1 = 0;
        int demandA2 = alpha.getDemand();

        for (int cA = 1; cA < alpha.length(); cA++)
        {
            int demandDeltaA = alpha.getFromStart(cA - 1).demand;
            demandA1 += demandDeltaA;
            demandA2 -= demandDeltaA;

            int demandB1 = 0;
            int demandB2 = beta.getDemand();

            for (int cB = 1; cB < beta.length(); cB++)
            {
                int demandDeltaB = beta.getFromStart(cB - 1).demand;
                demandB1 += demandDeltaB;
                demandB2 -= demandDeltaB;

                if (demandB1 + demandA2 > vehicleCapacity)
                    break;
                if (demandA1 + demandB2 > vehicleCapacity)
                    continue;

                Route ba = createRoute(cB + alpha.length() - cA);
                ba.addAll(beta, 0, cB, false);
                ba.addAll(alpha, cA, alpha.length(), false);

                if (timeWindows && !ba.areTimeWindowsValid())
                    break;

                Route ab = createRoute(cA + beta.length() - cB);
                ab.addAll(alpha, 0, cA, false);
                ab.addAll(beta, cB, beta.length(), false);

                if (timeWindows && !ab.areTimeWindowsValid())
                    continue;

                if (ab.getCost() + ba.getCost() + EPSILON > costToBeat)
                    continue;

                routes.set(alphaIdx, ab);
                routes.set(betaIdx, ba);

                return true;
            }
        }

        return false;
    }
}
