package pl.pielat.heuristicNew.ordering.concrete;

import pl.pielat.heuristicNew.Job;
import pl.pielat.heuristicNew.ordering.OrderingHeuristic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class OrderingByTimeWindow extends OrderingHeuristic
{
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

    protected OrderingByTimeWindow(ProblemInfo info, Property property)
    {
        super(info);

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
