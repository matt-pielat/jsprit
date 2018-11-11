package pl.pielat.heuristicNew.ordering.concrete;

import pl.pielat.heuristicNew.Job;
import pl.pielat.heuristicNew.ordering.OrderingHeuristic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class OrderingByDemand extends OrderingHeuristic
{
    private class DemandComparator implements Comparator<Job>
    {
        @Override
        public int compare(Job o1, Job o2)
        {
            return o1.demand - o2.demand;
        }
    }

    private DemandComparator comparator = new DemandComparator();

    protected OrderingByDemand(ProblemInfo info, Order order)
    {
        super(info, order);
    }

    @Override
    protected void orderJobsAscending(ArrayList<Job> jobs)
    {
        Collections.sort(jobs, comparator);
    }
}
