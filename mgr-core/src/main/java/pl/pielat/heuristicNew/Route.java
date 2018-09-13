package pl.pielat.heuristicNew;

import java.util.*;

public class Route
{
    private final boolean timeWindows;
    private final boolean transportAsymmetry;
    private final BaseHeuristic.TransportCostFunction costFunction;
    private final Place depot;

    private ArrayList<Job> jobs;

    private int demand;
    private boolean recalculateDemand = true;
    private boolean isTimeFeasible;
    private boolean recheckTimeFeasibility = true;

    Route(BaseHeuristic.ProblemInfo params, int initialCapacity)
    {
        timeWindows = params.timeWindows;
        transportAsymmetry = params.transportAsymmetry;
        costFunction = params.costFunction;
        depot = params.depot;

        jobs = new ArrayList<>(initialCapacity);
    }

    private Route(Route other)
    {
        timeWindows = other.timeWindows;
        transportAsymmetry = other.transportAsymmetry;
        costFunction = other.costFunction;
        depot = other.depot;

        jobs = new ArrayList<>(other.jobs);

        demand = other.demand;
        recalculateDemand = other.recalculateDemand;
        isTimeFeasible = other.isTimeFeasible;
        recheckTimeFeasibility = other.recheckTimeFeasibility;
    }

    public Route copy()
    {
        return new Route(this);
    }

    public void replaceInternals(Route other)
    {
        jobs = other.jobs;

        demand = other.demand;
        recalculateDemand = other.recalculateDemand;

        isTimeFeasible = other.isTimeFeasible;
        recheckTimeFeasibility = other.recheckTimeFeasibility;
    }

    public int length()
    {
        return jobs.size();
    }

    public Job getFirst()
    {
        return getFromStart(0);
    }

    public Job getLast()
    {
        return getFromEnd(0);
    }

    public Job getFromStart(int index)
    {
        return jobs.get(index);
    }

    public Job getFromEnd(int index)
    {
        return jobs.get(jobs.size() - 1 - index);
    }

    private Place getPrevious(int index)
    {
        if (index == 0)
            return depot;
        return jobs.get(index - 1);
    }

    private Place getNext(int index)
    {
        if (index == jobs.size())
            return depot;
        return jobs.get(index + 1);
    }

    private double getCost(Place from, Place to)
    {
        return costFunction.getCost(from, to);
    }

    public int getDemand()
    {
        if (recalculateDemand)
        {
            demand = 0;
            for (Job j : jobs)
                demand += j.demand;
            recalculateDemand = false;
        }
        return demand;
    }

    public boolean isTimeFeasible()
    {
        if (recheckTimeFeasibility)
        {
            isTimeFeasible = recalculateTimeFeasibility();
            recheckTimeFeasibility = false;
        }
        return isTimeFeasible;
    }

    private boolean recalculateTimeFeasibility()
    {
        Place prev = depot;
        double departure = depot.windowStart;

        for (Job job : jobs)
        {
            double earliestArrival = departure + getCost(prev, job);
            departure = Math.max(earliestArrival, job.windowStart) + job.serviceTime;

            if (departure > job.windowEnd)
                return false;

            prev = job;
        }
        return departure + getCost(prev, depot) <= depot.windowEnd;
    }

    public void reverseOrder()
    {
        if (jobs.size() <= 1)
            return;

        Collections.reverse(jobs);
        recheckTimeFeasibility = true;
    }

    public void addToEnd(Route o, boolean inversely)
    {
        if (inversely)
        {
            jobs.ensureCapacity(jobs.size() + o.jobs.size());

            ListIterator<Job> iterator = o.jobs.listIterator(o.jobs.size());
            while (iterator.hasPrevious())
                jobs.add(iterator.previous());
        }
        else
        {
            jobs.addAll(o.jobs);
        }

        demand += o.getDemand();
        recheckTimeFeasibility = true;
    }

    public void addToFront(Route o, boolean inversely)
    {
        ArrayList<Job> newJobs = new ArrayList<>(jobs.size() + o.jobs.size());

        if (inversely)
        {
            ListIterator<Job> iterator = o.jobs.listIterator(o.jobs.size());
            while (iterator.hasPrevious())
                newJobs.add(iterator.previous());
        }
        else
        {
            newJobs.addAll(o.jobs);
        }

        newJobs.addAll(jobs);
        jobs = newJobs;

        demand += o.getDemand();
        recheckTimeFeasibility = true;
    }
}
