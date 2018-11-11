package pl.pielat.heuristic.constructive;

import pl.pielat.algorithm.ProblemInfo;
import pl.pielat.heuristic.BaseHeuristic;
import pl.pielat.heuristic.Job;
import pl.pielat.heuristic.Route;

import java.util.ArrayList;

public abstract class ConstructiveHeuristic extends BaseHeuristic
{
    protected ConstructiveHeuristic(ProblemInfo info)
    {
        super(info);
    }

    public abstract void insertJobs(ArrayList<Route> routes, ArrayList<Job> jobsToInsert);
}
