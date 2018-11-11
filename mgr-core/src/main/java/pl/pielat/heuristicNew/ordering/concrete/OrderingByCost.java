package pl.pielat.heuristicNew.ordering.concrete;

import pl.pielat.heuristicNew.Job;
import pl.pielat.heuristicNew.ordering.OrderingHeuristic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class OrderingByCost extends OrderingHeuristic
{
    private class CostComparator implements Comparator<Job>
    {
        @Override
        public int compare(Job o1, Job o2)
        {
            return Double.compare(distances.get(o1), distances.get(o2));
        }
    }

    public enum Direction
    {
        FROM_DEPOT,
        TO_DEPOT
    }

    private CostComparator comparator = new CostComparator();
    private Direction direction;
    private HashMap<Job, Double> distances;

    public OrderingByCost(ProblemInfo info, Order order, Direction direction)
    {
        super(info, order);
        this.direction = direction;
    }

    @Override
    protected void orderJobsAscending(ArrayList<Job> jobs)
    {
        distances = new HashMap<>(jobs.size());

        if (direction == Direction.FROM_DEPOT)
        {
            for (Job job : jobs)
                distances.put(job, getCost(depot, job));
        }
        else
        {
            for (Job job : jobs)
                distances.put(job, getCost(job, depot));
        }

        Collections.sort(jobs, comparator);
    }
}
