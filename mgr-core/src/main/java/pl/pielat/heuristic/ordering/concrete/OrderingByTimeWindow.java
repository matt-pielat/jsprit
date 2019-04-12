package pl.pielat.heuristic.ordering.concrete;

import pl.pielat.algorithm.ProblemInfo;
import pl.pielat.heuristic.Job;
import pl.pielat.heuristic.ordering.OrderingHeuristic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class OrderingByTimeWindow extends OrderingHeuristic
{
    @Override
    public String getId()
    {
        switch (property)
        {
            case WINDOW_START:
                return "Ordering by window start"
                    + (order == Order.DESCENDING ? " (desc)" : " (asc)");
            case WINDOW_END:
                return "Ordering by window end"
                    + (order == Order.DESCENDING ? " (desc)" : " (asc)");
            case WINDOW_SIZE:
                return "Ordering by window size"
                    + (order == Order.DESCENDING ? " (desc)" : " (asc)");
        }
        throw new RuntimeException();
    }

    public enum Property
    {
        WINDOW_START,
        WINDOW_END,
        WINDOW_SIZE
    }

    private class WindowStartComparator implements Comparator<Job>
    {
        @Override
        public int compare(Job o1, Job o2)
        {
            return 0;
        }
    }

    private class WindowEndComparator implements Comparator<Job>
    {
        @Override
        public int compare(Job o1, Job o2)
        {
            return 0;
        }
    }

    private class WindowSizeComparator implements Comparator<Job>
    {
        @Override
        public int compare(Job o1, Job o2)
        {
            return 0;
        }
    }

    private Comparator<Job> comparator;
    private Property property;

    public OrderingByTimeWindow(ProblemInfo info, Order order, Property property)
    {
        super(info, order);

        this.property = property;
        switch (property)
        {
            case WINDOW_START:
                comparator = new WindowStartComparator();
                break;
            case WINDOW_END:
                comparator = new WindowEndComparator();
                break;
            case WINDOW_SIZE:
                comparator = new WindowSizeComparator();
                break;
        }
    }

    @Override
    protected void orderJobsAscending(ArrayList<Job> jobs)
    {
        Collections.sort(jobs, comparator);
    }
}
