package pl.pielat.heuristic.constructive.concrete;

import pl.pielat.algorithm.ProblemInfo;
import pl.pielat.heuristic.Job;
import pl.pielat.heuristic.Place;
import pl.pielat.heuristic.Route;
import pl.pielat.heuristic.constructive.ChristofidesAlgorithm;
import pl.pielat.heuristic.constructive.ConstructiveHeuristic;
import pl.pielat.util.RadialPlaceSweepComparator;

import java.util.ArrayList;
import java.util.Collections;

public class SweepAlgorithm extends ConstructiveHeuristic
{
    private ChristofidesAlgorithm tspAlgorithm;
    private RadialPlaceSweepComparator comparator;

    public SweepAlgorithm(ProblemInfo info)
    {
        super(info);

        if (timeWindows)
            throw new IllegalArgumentException("This heuristic cannot be used to solve VRPTW.");
        if (noCoordinates)
            throw new IllegalArgumentException("This heuristic cannot be used when jobs or depots have no coordinates.");

        tspAlgorithm = new ChristofidesAlgorithm(info.costFunction);
        comparator = new RadialPlaceSweepComparator(depot.location.getCoordinate());
    }

    @Override
    public void insertJobs(ArrayList<Route> routes, ArrayList<Job> jobsToInsert)
    {
        ArrayList<Job> jobsLeft = new ArrayList<>(jobsToInsert);

        Collections.sort(jobsLeft, comparator);

        while (!jobsLeft.isEmpty())
        {
            ArrayList<Job> newRouteJobs = new ArrayList<>(jobsLeft.size());
            int demand = 0;

            while (!jobsLeft.isEmpty())
            {
                Job last = jobsLeft.get(jobsLeft.size() - 1);

                demand += last.demand;
                if (demand > vehicleCapacity)
                    break;

                jobsLeft.remove(jobsLeft.size() - 1);
                newRouteJobs.add(last);
            }

            if (newRouteJobs.isEmpty())
                throw new RuntimeException("No customers were added to a route.");

            ArrayList<Place> temp = new ArrayList<>(newRouteJobs.size() + 1);
            temp.addAll(newRouteJobs);
            temp.add(depot);

            ArrayList<Integer> order = tspAlgorithm.solveTsp(temp);
            int depotIndex = order.indexOf(temp.size() - 1);

            Route newRoute = createRoute(newRouteJobs, order, depotIndex);
            routes.add(newRoute);
        }
    }

    private Route createRoute(ArrayList<Job> jobs, ArrayList<Integer> order, int depotIndex)
    {
        ArrayList<Job> orderedJobs = new ArrayList<>(jobs.size());

        for (int i = depotIndex + 1; i < order.size(); i++)
        {
            int jobIndex = order.get(i);
            Job job = jobs.get(jobIndex);
            orderedJobs.add(job);
        }

        for (int i = 0; i < depotIndex; i++)
        {
            int jobIndex = order.get(i);
            Job job = jobs.get(jobIndex);
            orderedJobs.add(job);
        }

        return createRoute(orderedJobs);
    }
}
