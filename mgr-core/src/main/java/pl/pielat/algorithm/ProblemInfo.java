package pl.pielat.algorithm;

import pl.pielat.heuristic.Job;
import pl.pielat.heuristic.Place;

import java.util.List;

public class ProblemInfo
{
    public interface TransportCostFunction
    {
        double getCost(Place from, Place to);
    }

    public final boolean transportAsymmetry;
    public final boolean timeWindows;
    public final boolean noCoordinates;

    public final TransportCostFunction costFunction;
    public final int vehicleCapacity;

    public final Place depot;
    public final List<Job> jobs; //should be a readonly list

    public ProblemInfo(TransportCostFunction costFunction, boolean transportAsymmetry, boolean timeWindows,
                       int vehicleCapacity, Place depot, List<Job> jobs)
    {
        this.transportAsymmetry = transportAsymmetry;
        this.timeWindows = timeWindows;
        this.costFunction = costFunction;
        this.vehicleCapacity = vehicleCapacity;
        this.depot = depot;
        this.jobs = jobs;

        boolean allJobsHaveCoordinates = depot.location.getCoordinate() != null;
        for (Job j : jobs)
            allJobsHaveCoordinates &= (j.location.getCoordinate() != null);
        noCoordinates = !allJobsHaveCoordinates;
    }
}
