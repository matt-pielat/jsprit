package pl.pielat.heuristicNew.repairing.concrete;

import pl.pielat.heuristicNew.Route;
import pl.pielat.heuristicNew.repairing.RepairingHeuristic;

import java.util.ArrayList;

public class StringCross extends RepairingHeuristic
{
    protected StringCross(ProblemInfo info)
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
            if (routes.get(i).length() < 2)
                continue;

            for (int j = 0; j < i; j++)
            {
                if (routes.get(j).length() < 2)
                    continue;

                if (findImprovementCross(routes, i, j))
                    return true;
            }
        }
        return false;
    }

    public boolean findImprovementCross(ArrayList<Route> routes, int aIndex, int bIndex)
    {
        Route a = routes.get(aIndex);
        Route b = routes.get(bIndex);

        int demandA1 = 0;
        int demandA2 = a.getDemand();

        for (int cA = 1; cA < a.length(); cA++)
        {
            int demandDiff = a.getFromStart(cA - 1).demand;
            demandA1 += demandDiff;
            demandA2 -= demandDiff;

            int demandB1 = 0;
            int demandB2 = b.getDemand();

            for (int cB = 1; cB < b.length(); cB++)
            {
                demandDiff = b.getFromStart(cB - 1).demand;
                demandB1 += demandDiff;
                demandB2 -= demandDiff;

                if (demandB1 + demandA2 > vehicleCapacity)
                    break;
                if (demandA1 + demandB2 > vehicleCapacity)
                    continue;

                Route ba = createRoute(cB + a.length() - cA);
                ba.addAll(b.subroute(0, cB), false);
                ba.addAll(a.subroute(cA, a.length()), false);

                if (timeWindows && !ba.areTimeWindowsValid())
                    break;

                Route ab = createRoute(cA + b.length() - cB);
                ab.addAll(a.subroute(0, cA), false);
                ab.addAll(b.subroute(cB, b.length()), false);

                if (timeWindows && !ab.areTimeWindowsValid())
                    continue;

                if (ab.getCost() + ba.getCost() + EPSILON > a.getCost() + b.getCost())
                    continue;

                routes.set(aIndex, ab);
                routes.set(bIndex, ba);

                return true;
            }
        }

        return false;
    }
}
