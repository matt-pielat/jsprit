package pl.pielat.util;

import pl.pielat.algorithm.ProblemInfo;
import pl.pielat.heuristic.Job;
import pl.pielat.heuristic.Route;
import pl.pielat.heuristic.constructive.ConstructiveHeuristic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Stack;

public class SimpleConstructiveHeuristic extends ConstructiveHeuristic
{
    private int maxJobsInRoute;

    public SimpleConstructiveHeuristic(ProblemInfo info, int maxJobsInRoute)
    {
        super(info);

        this.maxJobsInRoute = maxJobsInRoute;
    }

    @Override
    public void insertJobs(ArrayList<Route> routes, ArrayList<Job> jobsToInsert)
    {
        Stack<Job> jobsLeft = new Stack<>();
        jobsLeft.addAll(jobsToInsert);

        Route currentRoute = createRoute(maxJobsInRoute);
        routes.add(currentRoute);

        while (!jobsLeft.empty())
        {
            Job jobToInsert = jobsLeft.peek();

            if (currentRoute.length() == maxJobsInRoute)
            {
                currentRoute = createRoute(maxJobsInRoute);
                routes.add(currentRoute);
                continue;
            }
            if (currentRoute.length() == 0)
            {
                jobsLeft.pop();
                currentRoute.add(jobToInsert);
                continue;
            }
            if (currentRoute.getDemand() + jobToInsert.demand > vehicleCapacity)
            {
                currentRoute = createRoute(maxJobsInRoute);
                routes.add(currentRoute);
                continue;
            }

            if (timeWindows)
            {
                if (!currentRoute.canFitIntoTimeSchedule(currentRoute.length(), jobToInsert))
                {
                    currentRoute = createRoute(maxJobsInRoute);
                    routes.add(currentRoute);
                    continue;
                }
            }

            jobsLeft.pop();
            currentRoute.add(jobToInsert);
        }
    }
}
