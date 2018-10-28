package pl.pielat.heuristicNew;

import pl.pielat.util.ArrayUtils;

import java.util.Arrays;

public class Route
{
    private final boolean timeWindows;
    private final boolean transportAsymmetry;
    private final BaseHeuristic.TransportCostFunction costFunction;
    private final Place depot;

    private int length = 0;
    private Job[] jobs;
    private double[] earlyDeparture;
    private double[] lateArrival;

    private int demand;
    private boolean recalculateDemand = true;
    private boolean areTimeWindowsOk;
    private boolean recalculateTimeWindows = true;

    Route(BaseHeuristic.ProblemInfo params, int initialCapacity)
    {
        timeWindows = params.timeWindows;
        transportAsymmetry = params.transportAsymmetry;
        costFunction = params.costFunction;
        depot = params.depot;

        jobs = new Job[initialCapacity];
        if (timeWindows)
        {
            earlyDeparture = new double[initialCapacity];
            lateArrival = new double[initialCapacity];
        }
    }

    private Route(Route other, int beginIndex, int endIndex, boolean invert)
    {
        timeWindows = other.timeWindows;
        transportAsymmetry = other.transportAsymmetry;
        costFunction = other.costFunction;
        depot = other.depot;

        length = endIndex - beginIndex;
        jobs = new Job[other.length];
        if (timeWindows)
        {
            earlyDeparture = new double[other.length];
            lateArrival = new double[other.length];
        }

        copy(other, beginIndex, this, 0, length, invert);

        if (length == other.length) // whole arrays were copied
        {
            demand = other.demand;
            recalculateDemand = other.recalculateDemand;

            areTimeWindowsOk = other.areTimeWindowsOk;
            recalculateTimeWindows = other.recalculateTimeWindows || invert;
        }
    }

    public Route copy()
    {
        return new Route(this, 0, length, false);
    }

    public Route subroute(int beginIndex, int endIndex)
    {
        return new Route(this, beginIndex, endIndex, false);
    }

    public void replaceInternals(Route other)
    {
        jobs = other.jobs;
        if (timeWindows)
        {
            earlyDeparture = other.earlyDeparture;
            lateArrival = other.lateArrival;
        }
        length = other.length;

        demand = other.demand;
        recalculateDemand = other.recalculateDemand;
    }

    public void removeAll()
    {
        length = 0;

        demand = 0;
        recalculateDemand = false;
        areTimeWindowsOk = true;
        recalculateTimeWindows = false;
    }

    public void remove(int beginIndex, int endIndex)
    {
        int jobsToShift = length - endIndex;
        if (jobsToShift > 0)
        {
            copy(this, endIndex, this, beginIndex, jobsToShift, false);
        }

        length -= (endIndex - beginIndex);
        recalculateDemand = true;
        recalculateTimeWindows = true;
    }

    public int length()
    {
        return length;
    }

    public Job getFirst()
    {
        return getFromStart(0);
    }

    public Job getLast()
    {
        return getFromEnd(0);
    }

    public Job getFromStart(int index)
    {
        assert index < length;
        return jobs[index];
    }

    public Job getFromEnd(int index)
    {
        assert index < length;
        return jobs[length - 1 - index];
    }

    private double getCost(Place from, Place to)
    {
        return costFunction.getCost(from, to);
    }

    public double getCost()
    {
        double cost = 0;

        Place prev = depot;
        for (int i = 0; i < length; i++)
        {
            Job job = jobs[i];
            cost += getCost(prev, job);
            prev = job;
        }
        cost += getCost(prev, depot);

        return cost;
    }

    public int getDemand()
    {
        if (recalculateDemand)
        {
            demand = 0;
            for (int i = 0; i < length; i++)
            {
                Job job = jobs[i];
                demand += job.demand;
            }
            recalculateDemand = false;
        }
        return demand;
    }

    public boolean areTimeWindowsValid()
    {
        if (recalculateTimeWindows)
        {
            areTimeWindowsOk = recalculateEarlyDepartures() || recalculateLateArrivals();
            recalculateTimeWindows = false;
        }
        return areTimeWindowsOk;
    }

    public void reverse()
    {
        if (length <= 1)
            return;

        ArrayUtils.reverseArray(jobs, length);
        recalculateTimeWindows = true;
    }

    public void add(Job j)
    {
        add(length, j);
    }

    public void add(int index, Job j)
    {
        ensureCapacity(length + 1);
        copyInternalArrays(this, index, this, index + 1, length - index, false);
        jobs[index] = j;

        length++;
        demand += j.demand;
        recalculateTimeWindows = true;
    }

    public void addAll(Route o, boolean inversely)
    {
        ensureCapacity(length + o.length);
        copyInternalArrays(o, 0, this, length, o.length, inversely);

        length += o.length;
        demand += o.getDemand();
        recalculateTimeWindows = true;
    }

    public void addAll(int index, Route o, boolean inversely)
    {
        ensureCapacity(length + o.length);
        copyInternalArrays(this, index, this, index + o.length, length - index, false);
        copyInternalArrays(o, 0, this, index, o.length, inversely);

        length += o.length;
        demand += o.getDemand();
        recalculateTimeWindows = true;
    }

    public boolean canFitIntoTimeSchedule(int index, Job job)
    {
        return canFitIntoTimeSchedule(index, new Job[]{job}, 1);
    }

    public boolean canFitIntoTimeSchedule(int index, Route route)
    {
        return canFitIntoTimeSchedule(index, route.jobs, route.length);
    }

    private boolean canFitIntoTimeSchedule(int index, Job[] arr, int arrLength)
    {
        if (!areTimeWindowsValid())
            return false;

        Place first, last, prev, next;
        double firstEarlyDeparture, lastLateArrival, prevEarlyDeparture, nextLateArrival;

        if (index == 0)
        {
            first = depot;
            firstEarlyDeparture = depot.windowStart;
        }
        else
        {
            first = jobs[index - 1];
            firstEarlyDeparture = earlyDeparture[index - 1];
        }
        if (index == length)
        {
            last = depot;
            lastLateArrival = depot.windowEnd;
        }
        else
        {
            last = jobs[index];
            lastLateArrival = lateArrival[index];
        }

        prev = first;
        prevEarlyDeparture = firstEarlyDeparture;
        for (int i = 0; i < arrLength; i++)
        {
            Job j = arr[i];
            double earlyArrival = Math.max(prevEarlyDeparture + getCost(prev, j), j.windowStart);
            double earlyDeparture = earlyArrival + j.serviceTime;

            if (earlyDeparture > j.windowEnd)
                return false;

            prev = j;
            prevEarlyDeparture = earlyDeparture;
        }
        if (prevEarlyDeparture + getCost(prev, last) > lastLateArrival)
            return false;

        next = last;
        nextLateArrival = lastLateArrival;
        for (int i = arrLength - 1; i >= 0; i--)
        {
            Job j = arr[i];
            double lateDeparture = Math.min(nextLateArrival - getCost(j, next), j.windowEnd);
            double lateArrival = lateDeparture - j.serviceTime;

            if (lateDeparture < j.windowStart)
                return false;

            next = j;
            nextLateArrival = lateArrival;
        }
        if (nextLateArrival - getCost(first, next) < firstEarlyDeparture)
            return false;

        return true;
    }

    public static void copy(Route src, int srcPos, Route dst, int dstPos, int length, boolean inversely)
    {
        if (srcPos + length > src.length)
            throw new IndexOutOfBoundsException(String.format("%d + %d > %d", srcPos, length, src.length));

        if (dstPos + length > dst.length)
            throw new IndexOutOfBoundsException(String.format("%d + %d > %d", dstPos, length, dst.length));

        copyInternalArrays(src, srcPos, dst, dstPos, length, inversely);

        dst.recalculateTimeWindows = true;
        dst.recalculateDemand = true;
    }

    private static void copyInternalArrays(Route src, int srcPos, Route dst, int dstPos, int length, boolean inversely)
    {
        System.arraycopy(src.jobs, srcPos, dst.jobs, dstPos, length);
        if (src.timeWindows)
        {
            System.arraycopy(src.earlyDeparture, srcPos, dst.earlyDeparture, dstPos, length);
            System.arraycopy(src.lateArrival, srcPos, dst.lateArrival, dstPos, length);
        }

        if (inversely)
        {
            ArrayUtils.reverseArray(dst.jobs, srcPos, srcPos + length);
            if (src.timeWindows)
            {
                // Reversing earlyDeparture and lateArrival makes no sense.
            }
        }
    }

    private boolean recalculateEarlyDepartures()
    {
        Place prev = depot;
        double departure = depot.windowStart;

        for (int i = 0; i < length; i++)
        {
            Job job = jobs[i];
            double earlyArrival = departure + getCost(prev, job);
            earlyDeparture[i] = Math.max(earlyArrival, job.windowStart) + job.serviceTime;

            if (earlyDeparture[i] > job.windowEnd)
                return false;

            prev = job;
            departure = earlyDeparture[i];
        }
        return departure + getCost(prev, depot) > depot.windowEnd;
    }

    private boolean recalculateLateArrivals()
    {
        Place next = depot;
        double arrival = depot.windowEnd;

        for (int i = length - 1; i >= 0; i--)
        {
            Job job = jobs[i];
            double lateDeparture = arrival - getCost(job, next);
            lateArrival[i] = Math.min(lateDeparture, job.windowEnd) - job.serviceTime;

            if (lateArrival[i] < job.windowStart)
                return false;

            next = job;
            arrival = lateArrival[i];
        }
        return arrival - getCost(depot, next) < depot.windowStart;
    }

    private void ensureCapacity(int minCapacity)
    {
        int oldCapacity = jobs.length;
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity;

        jobs = Arrays.copyOf(jobs, newCapacity);
        if (timeWindows)
        {
            earlyDeparture = Arrays.copyOf(earlyDeparture, newCapacity);
            lateArrival = Arrays.copyOf(lateArrival, newCapacity);
        }
    }
}
