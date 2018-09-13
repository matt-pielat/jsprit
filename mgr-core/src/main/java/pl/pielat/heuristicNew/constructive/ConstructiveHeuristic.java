package pl.pielat.heuristicNew.constructive;

import pl.pielat.heuristicNew.BaseHeuristic;
import pl.pielat.heuristicNew.Job;
import pl.pielat.heuristicNew.Route;

import java.util.ArrayList;

public abstract class ConstructiveHeuristic extends BaseHeuristic
{
    protected ConstructiveHeuristic(ProblemInfo info)
    {
        super(info);
    }

    public abstract void insertJobs(ArrayList<Route> routes, ArrayList<Job> jobsToInsert);
}
