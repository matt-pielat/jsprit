package pl.pielat.heuristic.ordering;

import pl.pielat.algorithm.ProblemInfo;
import pl.pielat.heuristic.BaseHeuristic;
import pl.pielat.heuristic.Job;

import java.util.ArrayList;
import java.util.Collections;

public abstract class OrderingHeuristic extends BaseHeuristic
{
    public enum Order
    {
        ASCENDING,
        DESCENDING
    }

    protected final Order order;

    protected OrderingHeuristic(ProblemInfo info, Order order)
    {
        super(info);
        this.order = order;
    }

    protected abstract void orderJobsAscending(ArrayList<Job> jobs);

    public void orderJobs(ArrayList<Job> jobs)
    {
        orderJobsAscending(jobs);

        if (order == Order.DESCENDING)
            Collections.reverse(jobs);
    }

}
