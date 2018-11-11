package pl.pielat.heuristic.constructive.concrete;

import pl.pielat.algorithm.ProblemInfo;
import pl.pielat.heuristic.Job;
import pl.pielat.heuristic.Place;
import pl.pielat.heuristic.Route;
import pl.pielat.heuristic.constructive.ConstructiveHeuristic;

import java.util.ArrayList;

public class KilbyAlgorithm extends ConstructiveHeuristic
{
    public KilbyAlgorithm(ProblemInfo info)
    {
        super(info);
    }

    @Override
    public void insertJobs(ArrayList<Route> routes, ArrayList<Job> jobsToInsert)
    {
        for (Job job : jobsToInsert)
            insertJob(routes, job);
    }

    private void insertJob(ArrayList<Route> routes, Job job)
    {
        Route bestRoute = null;
        int bestPosition = -1;
        double minExtraCost = Double.POSITIVE_INFINITY;

        for (Route route : routes)
        {
            if (route.getDemand() + job.demand > vehicleCapacity)
                continue;

            Place prev = depot;
            Place next;
            for (int i = 0; i < route.length(); i++)
            {
                if (timeWindows && !route.canFitIntoTimeSchedule(i, job))
                    continue;

                next = route.getFromStart(i);
                double extraCost = getCost(prev, job) + getCost(job, next) - getCost(prev, next);

                if (extraCost < minExtraCost)
                {
                    minExtraCost = extraCost;
                    bestRoute = route;
                    bestPosition = i;
                }
                prev = next;
            }

            if (route.length() > 1 || transportAsymmetry)
            {
                if (timeWindows && !route.canFitIntoTimeSchedule(route.length(), job))
                    continue;

                next = depot;
                double extraCost = getCost(prev, job) + getCost(job, next) - getCost(prev, next);

                if (extraCost < minExtraCost)
                {
                    minExtraCost = extraCost;
                    bestRoute = route;
                    bestPosition = route.length();
                }
            }
        }

        if (bestRoute == null)
        {
            Route newRoute = createRoute(job);
            routes.add(newRoute);
        }
        else
        {
            bestRoute.add(bestPosition, job);
        }
    }
}
