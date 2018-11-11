package pl.pielat.heuristicNew.ordering;

import pl.pielat.algorithm.TabuRandomizer;
import pl.pielat.heuristicNew.BaseHeuristic;
import pl.pielat.heuristicNew.ordering.concrete.OrderingByCost;
import pl.pielat.heuristicNew.ordering.concrete.OrderingByDemand;
import pl.pielat.heuristicNew.ordering.concrete.OrderingByRadialSweep;
import pl.pielat.heuristicNew.ordering.concrete.OrderingByTimeWindow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class OrderingHeuristicProvider extends TabuRandomizer<OrderingHeuristic>
{
    private BaseHeuristic.ProblemInfo problemInfo;
    private Random random;
    private ArrayList<OrderingHeuristic> heuristics;

    public OrderingHeuristicProvider(BaseHeuristic.ProblemInfo problemInfo, Random random)
    {
        super(random);
        this.problemInfo = problemInfo;
        this.random = random;
        heuristics = new ArrayList<>();

        final OrderingHeuristic.Order ascending = OrderingHeuristic.Order.ASCENDING;
        final OrderingHeuristic.Order descending = OrderingHeuristic.Order.DESCENDING;
        final OrderingByCost.Direction fromDepot = OrderingByCost.Direction.FROM_DEPOT;
        final OrderingByCost.Direction toDepot = OrderingByCost.Direction.TO_DEPOT;
        final OrderingByRadialSweep.SweepStart closestJob = OrderingByRadialSweep.SweepStart.CLOSEST_JOB;
        final OrderingByRadialSweep.SweepStart farthestJob = OrderingByRadialSweep.SweepStart.FARTHEST_JOB;
        final OrderingByRadialSweep.RadialOrder clockwise = OrderingByRadialSweep.RadialOrder.CLOCKWISE;
        final OrderingByRadialSweep.RadialOrder counterclockwise = OrderingByRadialSweep.RadialOrder.COUNTERCLOCKWISE;
        final OrderingByTimeWindow.Property windowStart = OrderingByTimeWindow.Property.WINDOW_START;
        final OrderingByTimeWindow.Property windowEnd = OrderingByTimeWindow.Property.WINDOW_END;
        final OrderingByTimeWindow.Property windowSize = OrderingByTimeWindow.Property.WINDOW_SIZE;

        heuristics.addAll(Arrays.asList(
            new OrderingByCost(problemInfo, ascending, fromDepot),
            new OrderingByCost(problemInfo, descending, fromDepot),
            new OrderingByDemand(problemInfo, ascending),
            new OrderingByDemand(problemInfo, descending),
            new OrderingByRadialSweep(problemInfo, clockwise, closestJob),
            new OrderingByRadialSweep(problemInfo, counterclockwise, closestJob),
            new OrderingByRadialSweep(problemInfo, clockwise, farthestJob),
            new OrderingByRadialSweep(problemInfo, counterclockwise, farthestJob)
        ));

        if (problemInfo.transportAsymmetry)
        {
            heuristics.addAll(Arrays.asList(
                new OrderingByCost(problemInfo, ascending, toDepot),
                new OrderingByCost(problemInfo, descending, toDepot)
            ));
        }

        if (problemInfo.timeWindows)
        {
            heuristics.addAll(Arrays.asList(
                new OrderingByTimeWindow(problemInfo, ascending, windowStart),
                new OrderingByTimeWindow(problemInfo, descending, windowStart),
                new OrderingByTimeWindow(problemInfo, ascending, windowEnd),
                new OrderingByTimeWindow(problemInfo, descending, windowEnd),
                new OrderingByTimeWindow(problemInfo, ascending, windowSize),
                new OrderingByTimeWindow(problemInfo, descending, windowSize)
            ));
        }
    }

    @Override
    protected int getSetSize()
    {
        return heuristics.size();
    }

    @Override
    protected OrderingHeuristic getItemByIndex(int index)
    {
        return heuristics.get(index);
    }
}
