package pl.pielat.heuristic;

import pl.pielat.algorithm.ProblemInfo;

import java.util.List;

public abstract class BaseHeuristic
{

    protected static final double EPSILON = 1e-5;
    private static final int INITIAL_ROUTE_CAPACITY = 10;

    private final ProblemInfo problemInfo;

    protected final boolean transportAsymmetry;
    protected final boolean timeWindows;
    protected final boolean noCoordinates;

    protected final int vehicleCapacity;
    protected final Place depot;
    private final ProblemInfo.TransportCostFunction costFunction;

    protected BaseHeuristic(ProblemInfo info)
    {
        if (info.transportAsymmetry && info.timeWindows)
        {
            throw new IllegalArgumentException(
                "Transport asymmetry and time windows cannot be enabled at the same time.");
        }

        problemInfo = info;

        transportAsymmetry = info.transportAsymmetry;
        timeWindows = info.timeWindows;
        noCoordinates = info.noCoordinates;

        vehicleCapacity = info.vehicleCapacity;
        depot = info.depot;
        costFunction = info.costFunction;
    }

    protected double getCost(Place from, Place to)
    {
        return costFunction.getCost(from, to);
    }

    protected Route createRoute(int initialCapacity)
    {
        return new Route(problemInfo, initialCapacity);
    }

    protected Route createRoute(Job job)
    {
        Route route = new Route(problemInfo, INITIAL_ROUTE_CAPACITY);
        route.add(job);
        return route;
    }

    protected Route createRoute(List<Job> jobs)
    {
        int initialCapacity = jobs.size() > INITIAL_ROUTE_CAPACITY ? jobs.size() : INITIAL_ROUTE_CAPACITY;
        Route route = new Route(problemInfo, initialCapacity);
        route.addAll(jobs);
        return route;
    }
}
