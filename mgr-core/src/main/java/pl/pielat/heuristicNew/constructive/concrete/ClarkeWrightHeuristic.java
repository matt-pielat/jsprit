package pl.pielat.heuristicNew.constructive.concrete;

import pl.pielat.heuristicNew.constructive.ConstructiveHeuristic;
import pl.pielat.heuristicNew.Job;
import pl.pielat.heuristicNew.Route;

import java.util.*;

class ClarkeWrightHeuristic extends ConstructiveHeuristic
{
    protected ClarkeWrightHeuristic(ProblemInfo info)
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

            int routeAOriginalLength = routeA.length();

            if (timeWindows)
            {
                Route merged;

                if (routeA.getLast().equals(jobA))
                {
                    merged = routeA.copy();
                    boolean invert = !routeB.getFirst().equals(jobB);
                    merged.addAll(routeB, invert);
                }
                else if (routeB.getLast().equals(jobB))
                {
                    merged = routeB.copy();
                    boolean invert = !routeA.getFirst().equals(jobA);
                    merged.addAll(routeA, invert);
                }
                else
                {
                    throw new RuntimeException("Neither route has last node");
                }

                if (!merged.areTimeWindowsValid())
                {
                    merged.reverse();
                    if (!merged.areTimeWindowsValid())
                        continue;
                }

                routeA.replaceInternals(merged);
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
                    routeA.addAll(0, routeB, false);
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
                    boolean invert = !routeB.getFirst().equals(jobB);
                    routeA.addAll(routeB, invert);
                }
                else if (routeB.getLast().equals(jobB))
                {
                    boolean invert = !routeA.getFirst().equals(jobA);
                    routeA.addAll(routeB, invert);
                }
                else
                {
                    throw new RuntimeException("Neither route has last node");
                }
            }

            // Update inner jobs.
            if (routeAOriginalLength > 1)
                innerJobIds.add(jobA.id);
            if (routeB.length() > 1)
                innerJobIds.add(jobB.id);

            // Update job-to-route map.
            jobIdToRouteMap.put(routeB.getFirst().id, routeA);
            jobIdToRouteMap.put(routeB.getLast().id, routeA);

            routes.remove(routeB);
        }
    }
}
