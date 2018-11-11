package pl.pielat.heuristicNew.ordering;

import pl.pielat.heuristicNew.BaseHeuristic;
import pl.pielat.heuristicNew.Job;

import java.util.ArrayList;
import java.util.Collections;

public abstract class OrderingHeuristic extends BaseHeuristic
{
    public enum Order
    {
        ASCENDING,
        DESCENDING
    }

    protected OrderingHeuristic(ProblemInfo info)
    {
        super(info);
    }

    protected abstract void orderJobsAscending(ArrayList<Job> jobs);

    public void orderJobs(ArrayList<Job> jobs, Order order)
    {
        orderJobsAscending(jobs);

        if (order == Order.DESCENDING)
            Collections.reverse(jobs);
    }

}
