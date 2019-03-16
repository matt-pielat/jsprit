package pl.pielat.heuristic.repairing.concrete;

import pl.pielat.algorithm.ProblemInfo;
import pl.pielat.heuristic.Place;
import pl.pielat.heuristic.Route;
import pl.pielat.heuristic.repairing.RepairingHeuristic;

import java.util.List;

public class StringRelocation extends RepairingHeuristic
{
    public StringRelocation(ProblemInfo info)
    {
        super(info);
    }

    @Override
    public void improveRoutes(List<Route> routes)
    {
        while (findAnyImprovement(routes, 2) || findAnyImprovement(routes, 1));
    }

    public boolean findAnyImprovement(List<Route> routes, int k)
    {
        for (int i = 0; i < routes.size(); i++)
        {
            Route donor = routes.get(i);

            if (donor.length() < k)
                continue;

            for (int j = 0; j < routes.size(); j++)
            {
                if (i == j)
                    continue;

                if (findImprovement(routes, i, j, k))
                    return true;
            }
        }

        return false;
    }

    public boolean findImprovement(List<Route> routes, int donorIdx, int doneeIdx, int k)
    {
        Route donor = routes.get(donorIdx);
        Route donee = routes.get(doneeIdx);

        int doneeDemand = donee.getDemand();

        for (int from = 0; from < donor.length() - k + 1; from++)
        {
            int stringDemand = getPartialDemand(donor, from, k);

            if (stringDemand + doneeDemand > vehicleCapacity)
                continue;

            for (int to = 0; to < donee.length(); to++)
            {
                if (getCostDelta(donor, donee, from, to, k) > -EPSILON)
                    continue;

                if (timeWindows)
                {
                    Route doneeNew = createRoute(donee.length() + k);
                    doneeNew.addAll(donee, 0, to, false);
                    doneeNew.addAll(donor, from, from + k, false);
                    doneeNew.addAll(donee, to, donee.length(), false);

                    if (!doneeNew.areTimeWindowsValid())
                        continue;

                    routes.set(doneeIdx, doneeNew);
                }
                else
                {
                    donee.addAll(to, donor, from, from + k, false);
                }

                if (donor.length() == k)
                    routes.remove(donorIdx);
                else
                    donor.remove(from, from + k);

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

    private double getCostDelta(Route donor, Route donee, int idxFrom, int idxTo, int k)
    {
        // BEFORE:
        // ... - bA - sA - ... - eA - aA - ...
        // ... - bB - aB - ...

        // AFTER:
        // ... - bA - aA - ...
        // ... - bB - sA - ... - eA - aB - ...

        Place bA = getRouteNodeSafely(donor, idxFrom - 1);
        Place sA = getRouteNodeSafely(donor, idxFrom);
        Place eA = getRouteNodeSafely(donor, idxFrom + k - 1);
        Place aA = getRouteNodeSafely(donor, idxFrom + k);

        Place bB = getRouteNodeSafely(donee, idxTo - 1);
        Place aB = getRouteNodeSafely(donee, idxTo);

        return
            - getCost(bA, sA) - getCost(eA, aA) - getCost(bB, aB)
            + getCost(bA, aA) + getCost(bB, sA) + getCost(eA, aB);
    }
}
