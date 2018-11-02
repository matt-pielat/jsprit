package pl.pielat.heuristicNew.repairing.concrete;

import pl.pielat.heuristicNew.Route;
import pl.pielat.heuristicNew.repairing.RepairingHeuristic;

import java.util.ArrayList;

public class StringRelocation extends RepairingHeuristic
{
    protected StringRelocation(ProblemInfo info)
    {
        super(info);
    }

    @Override
    public void improveRoutes(ArrayList<Route> routes)
    {
        while (findAnyImprovement(routes, 2) || findAnyImprovement(routes, 1));
    }

    public boolean findAnyImprovement(ArrayList<Route> routes, int k)
    {
        for (int i = 0; i < routes.size(); i++)
        {
            Route donor = routes.get(i);

            if (donor.length() < k)
                continue;

            double donorCost = donor.getCost();

            for (int j = 0; j < routes.size(); j++)
            {
                if (i == j)
                    continue;

                Route donee = routes.get(j);
                double doneeCost = donee.getCost();

                if (findImprovement(routes, i, j, k, donorCost + doneeCost))
                    return true;
            }
        }

        return false;
    }

    public boolean findImprovement(ArrayList<Route> routes, int donorIdx, int doneeIdx, int k, double costToBeat)
    {
        Route donor = routes.get(donorIdx);
        Route donee = routes.get(doneeIdx);

        int doneeDemand = donee.getDemand();
        boolean removeDonor = donor.length() == k;

        for (int from = 0; from < donor.length() - k + 1; from++)
        {
            int stringDemand = getPartialDemand(donor, from, k);

            if (stringDemand + doneeDemand > vehicleCapacity)
                continue;

            Route donorNew;
            double donorNewCost;
            if (removeDonor)
            {
                donorNew = null;
                donorNewCost = 0;
            }
            else
            {
                donorNew = donor.copy();
                donorNew.remove(from, from + k);
                donorNewCost = donorNew.getCost();
            }

            for (int to = 0; to < donee.length(); to++)
            {
                Route doneeNew = createRoute(donee.length() + k);
                doneeNew.addAll(donee, 0, to, false);
                doneeNew.addAll(donor, from, from + k, false);
                doneeNew.addAll(donee, to, donee.length(), false);

                if (timeWindows && !doneeNew.areTimeWindowsValid())
                    continue;

                if (doneeNew.getCost() + donorNewCost > costToBeat + EPSILON)
                    continue;

                routes.set(doneeIdx, doneeNew);
                if (removeDonor)
                    routes.remove(donorIdx);
                else
                    routes.set(donorIdx, donorNew);

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
