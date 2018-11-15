package pl.pielat.heuristic.constructive.concrete;

import pl.pielat.algorithm.ProblemInfo;
import pl.pielat.heuristic.constructive.ConstructiveHeuristic;
import pl.pielat.heuristic.Job;
import pl.pielat.heuristic.Route;

import java.util.*;

public class ClarkeWrightHeuristic extends ConstructiveHeuristic
{
    public ClarkeWrightHeuristic(ProblemInfo info)
    {
        super(info);
    }

    private class Saving implements Comparable<Saving>
    {
        Job jobA;
        Job jobB;
        double value;

        public Saving(Job a, Job b, double value)
        {
            jobA = a;
            jobB = b;
            this.value = value;
        }

        @Override
        public int compareTo(Saving o)
        {
            // Compare inversely because we want high value savings in front of the PriorityQueue
            return Double.compare(o.value, value);
        }
    }

    @Override
    public void insertJobs(ArrayList<Route> routes, ArrayList<Job> jobsToInsert)
    {
        int mergeableEndpointsCapacity = routes.size() * 2 + jobsToInsert.size();

        // Create new one-client routes.
        routes.ensureCapacity(routes.size() + jobsToInsert.size());
        for (Job j : jobsToInsert)
            routes.add(createRoute(j));

        List<Job> mergeableEndpoints = new ArrayList<>(mergeableEndpointsCapacity);
        Map<Integer, Route> jobIdToRouteMap = new HashMap<>(mergeableEndpointsCapacity);
        for (Route r : routes)
        {
            Job firstEndpoint = r.getFromStart(0);
            mergeableEndpoints.add(firstEndpoint);
            jobIdToRouteMap.put(firstEndpoint.id, r);

            if (r.length() > 1) // Route has second endpoint.
            {
                Job secondEndpoint = r.getFromEnd(0);
                mergeableEndpoints.add(secondEndpoint);
                jobIdToRouteMap.put(secondEndpoint.id, r);
            }
        }

        List<Saving> savings = new LinkedList<>();

        for (int a = 1; a < mergeableEndpoints.size(); a++)
        {
            Job jobA = mergeableEndpoints.get(a);
            Route routeA = jobIdToRouteMap.get(jobA.id);

            for (int b = 0; b < a; b++)
            {
                Job jobB = mergeableEndpoints.get(b);
                Route routeB = jobIdToRouteMap.get(jobB.id);

                if (routeA == routeB)
                    continue;
                if (routeA.getDemand() + routeB.getDemand() > vehicleCapacity)
                    continue;

                double savingValue = -getCost(jobA, jobB) + getCost(jobA, depot) + getCost(depot, jobB);

                if (savingValue > 0)
                {
                    Saving newSaving = new Saving(jobA, jobB, savingValue);
                    savings.add(newSaving);
                }
            }
        }

        // Jobs whose locations became interior within their routes.
        Set<Integer> innerJobIds = new HashSet<>(mergeableEndpoints.size());

        PriorityQueue<Saving> savingsPriorityQueue = new PriorityQueue<>(savings);
        while (!savingsPriorityQueue.isEmpty())
        {
            Saving saving = savingsPriorityQueue.poll();

            Job jobA = saving.jobA;
            Job jobB = saving.jobB;

            if (innerJobIds.contains(jobA.id) || innerJobIds.contains(jobB.id))
                continue;

            Route routeA = jobIdToRouteMap.get(jobA.id);
            Route routeB = jobIdToRouteMap.get(jobB.id);

            if (routeA == routeB)
                continue;
            if (routeA.getDemand() + routeB.getDemand() > vehicleCapacity)
                continue;

            int originalLengthA = routeA.length();
            int originalLengthB = routeB.length();

            Route routeToKeep = routeA;
            Route routeToRemove = routeB;

            if (timeWindows)
            {
                Route merged;

                if (routeA.getLast().equals(jobA))
                {
                    merged = routeA.copy();
                    if (routeB.getFirst().equals(jobB))
                    {
                        merged.addAll(routeB, false);
                    }
                    else
                    {
                        merged.addAll(routeB, true);
                    }
                }
                else if (routeB.getLast().equals(jobB))
                {
                    // routeA.getFirst().equals(jobA) is true
                    merged = routeB.copy();
                    merged.addAll(routeA, false);

                    routeToKeep = routeB;
                    routeToRemove = routeA;
                }
                else
                {
                    // routeA.getFirst().equals(jobA) is true
                    // routeB.getFirst().equals(jobB) is true
                    merged = routeA.copy();
                    merged.reverse();
                    merged.addAll(routeB, false);
                }

                if (!merged.areTimeWindowsValid())
                {
                    merged.reverse();
                    if (!merged.areTimeWindowsValid())
                        continue;
                }

                routeToKeep.replaceInternals(merged);
            }
            else if (transportAsymmetry)
            {
                // Order is important and we don't want to create a route node
                // with both roads going outward (or inward).

                if (routeA.getLast().equals(jobA) && routeB.getFirst().equals(jobB))
                {
                    routeA.addAll(routeB, false);
                }
                else if (routeB.getLast().equals(jobB) && routeA.getFirst().equals(jobA))
                {
                    routeB.addAll(routeA, false);

                    routeToRemove = routeA;
                    routeToKeep = routeB;
                }
                else
                {
                    continue;
                }
            }
            else
            {
                if (routeA.getLast().equals(jobA))
                {
                    if (routeB.getFirst().equals(jobB))
                    {
                        routeA.addAll(routeB, false);
                    }
                    else
                    {
                        routeA.addAll(routeB, true);
                    }
                }
                else if (routeB.getLast().equals(jobB))
                {
                    // routeA.getFirst().equals(jobA) is true
                    routeB.addAll(routeA, false);

                    routeToRemove = routeA;
                    routeToKeep = routeB;
                }
                else
                {
                    // routeA.getFirst().equals(jobA) is true
                    // routeB.getFirst().equals(jobB) is true
                    routeA.reverse();
                    routeA.addAll(routeB, false);
                }
            }

            // Update inner jobs.
            if (originalLengthA > 1)
                innerJobIds.add(jobA.id);
            if (originalLengthB > 1)
                innerJobIds.add(jobB.id);

            // Update job-to-route map.
            jobIdToRouteMap.put(routeToRemove.getFirst().id, routeToKeep);
            jobIdToRouteMap.put(routeToRemove.getLast().id, routeToKeep);

            routes.remove(routeToRemove);
        }
    }
}
