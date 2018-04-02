package pl.pielat.heuristic.constructive.concrete;

import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Delivery;
import pl.pielat.heuristic.constructive.ConstructiveHeuristic;
import pl.pielat.algorithm.MgrRoute;

import java.util.*;

//TODO adjust to matrix-based cost calculation
public class ClarkeWrightHeuristic extends ConstructiveHeuristic
{
    private class Saving implements Comparable<Saving>
    {
        Delivery jobA;
        Delivery jobB;
        double savingValue;

        Saving(Delivery a, Delivery b, double value)
        {
            jobA = a;
            jobB = b;
            savingValue = value;
        }

        @Override
        public int compareTo(Saving o)
        {
            return Double.compare(o.savingValue, savingValue);
        }
    }

    public ClarkeWrightHeuristic(VehicleRoutingProblem vrp)
    {
        super(vrp);
    }

    @Override
    public void insertJobs(List<MgrRoute> routes, List<Delivery> jobsToInsert)
    {
        int initialCapacity = routes.size() * 2 + jobsToInsert.size();

        for (Delivery job : jobsToInsert)
            routes.add(new MgrRoute(job));

        List<Delivery> jobsToProcess = new ArrayList<>(initialCapacity);
        Map<Delivery, MgrRoute> jobToRoute = new HashMap<>(initialCapacity);

        for (MgrRoute route : routes)
        {
            jobsToProcess.add(route.getFirst());
            jobToRoute.put(route.getFirst(), route);

            if (route.length() > 1) // route has another end
            {
                jobsToProcess.add(route.getLast());
                jobToRoute.put(route.getLast(), route);
            }
        }

        if (jobsToProcess.size() == 1)
            return;

        initialCapacity = (jobsToProcess.size() * jobsToProcess.size() - jobsToProcess.size()) / 2;
        PriorityQueue<Saving> savings = new PriorityQueue<>(initialCapacity);

        for (int i = 1; i < jobsToProcess.size(); i++)
        {
            Delivery jobA = jobsToProcess.get(i);
            for (int j = 0; j < i; j++)
            {
                Delivery jobB = jobsToProcess.get(j);

                if (jobToRoute.get(jobA) == jobToRoute.get(jobB))
                    continue;

                double savingValue = getDistance(jobA.getLocation(), getDepotLocation())
                    + getDistance(getDepotLocation(), jobB.getLocation())
                    - getDistance(jobA.getLocation(), jobB.getLocation());

                if (savingValue <= 0)
                    continue;

                Saving saving = new Saving(jobA, jobB, savingValue);
                savings.add(saving);
            }
        }

        // Jobs whose locations became interior within their routes
        Set<Delivery> innerJobs = new HashSet<>(jobsToProcess.size());

        while (!savings.isEmpty())
        {
            Saving saving = savings.poll();
            if (innerJobs.contains(saving.jobA) || innerJobs.contains(saving.jobB))
                continue;

            MgrRoute routeA = jobToRoute.get(saving.jobA);
            MgrRoute routeB = jobToRoute.get(saving.jobB);

            if (routeA == routeB)
                continue;
            if (getDemand(routeA) + getDemand(routeB) > getVehicleCapacity())
                continue;

            for (Delivery job : routeB)
                jobToRoute.put(job, routeA);

            if (routeA.length() > 1)
                innerJobs.add(saving.jobA);
            if (routeB.length() > 1)
                innerJobs.add(saving.jobB);

            // Merge routes
            if (saving.jobA == routeA.getFirst())
            {
                if (saving.jobB == routeB.getFirst())
                    routeB.reverse();
                routeA.addToFront(routeB);
            }
            else
            {
                if (saving.jobB != routeB.getFirst())
                    routeB.reverse();
                routeA.addToEnd(routeB);
            }

            routes.remove(routeB);
        }
    }
}
