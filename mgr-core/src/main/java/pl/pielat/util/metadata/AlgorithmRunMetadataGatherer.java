package pl.pielat.util.metadata;

import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.listener.AlgorithmEndsListener;
import com.graphhopper.jsprit.core.algorithm.listener.AlgorithmStartsListener;
import com.graphhopper.jsprit.core.algorithm.listener.IterationEndsListener;
import com.graphhopper.jsprit.core.algorithm.listener.IterationStartsListener;
import com.graphhopper.jsprit.core.algorithm.selector.SelectBest;
import com.graphhopper.jsprit.core.algorithm.selector.SolutionSelector;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;

import java.util.*;

@SuppressWarnings("MethodParameterNamingConvention")
public class AlgorithmRunMetadataGatherer implements IterationStartsListener, IterationEndsListener, AlgorithmStartsListener, AlgorithmEndsListener
{
    private int iterationStarted;
    private int iterationEnded;

    private long algorithmStartNanoTime;
    private long algorithmEndNanoTime;

    private final long minIntermediateCostWriteDelayInNano;
    private long lastIntermediateCostWriteTimeInNano;
    private boolean gatherIntermediateCosts;

    private Queue<Long> intermediateCostWriteNanoTimes;
    private Queue<Double> intermediateCosts;
    private double lastCost = Double.POSITIVE_INFINITY;

    private final SolutionSelector selector = new SelectBest();

    public AlgorithmRunMetadataGatherer(long minIntermediateCostWriteDelayInMs)
    {
        gatherIntermediateCosts = minIntermediateCostWriteDelayInMs >= 0;
        minIntermediateCostWriteDelayInNano = minIntermediateCostWriteDelayInMs * 1000000;

        iterationStarted = -1;
        iterationEnded = -1;

        intermediateCostWriteNanoTimes = new LinkedList<>();
        intermediateCosts = new LinkedList<>();
    }

    public AlgorithmRunMetadata getMetadata()
    {
        AlgorithmRunMetadata result = new AlgorithmRunMetadata();

        result.iterationCount = iterationStarted;
        result.millisecondsElapsed = (algorithmEndNanoTime - algorithmStartNanoTime) / 1000000;
        result.intermediateCosts = new ArrayList<>(intermediateCosts.size());

        while (!intermediateCosts.isEmpty())
        {
            IntermediateCost ic = new IntermediateCost();
            ic.cost = intermediateCosts.remove();
            ic.timeInMs = intermediateCostWriteNanoTimes.remove() / 1000000;
            result.intermediateCosts.add(ic);
        }

        return result;
    }

    @Override
    public void informIterationStarts(
        int i,
        VehicleRoutingProblem problem,
        Collection<VehicleRoutingProblemSolution> solutions)
    {
        iterationStarted = i;
    }

    @Override
    public void informIterationEnds(
        int i,
        VehicleRoutingProblem problem,
        Collection<VehicleRoutingProblemSolution> solutions)
    {
        iterationEnded = i;

        long now = System.nanoTime();
        if (!gatherIntermediateCosts || now - lastIntermediateCostWriteTimeInNano < minIntermediateCostWriteDelayInNano)
            return;

        VehicleRoutingProblemSolution solution = selector.selectSolution(solutions);

        double cost = solution.getCost();
        if (cost < lastCost)
        {
            intermediateCosts.add(cost);
            intermediateCostWriteNanoTimes.add(now - algorithmStartNanoTime);
            lastCost = cost;
        }

        lastIntermediateCostWriteTimeInNano = now;
    }

    @Override
    public void informAlgorithmStarts(
        VehicleRoutingProblem problem,
        VehicleRoutingAlgorithm algorithm,
        Collection<VehicleRoutingProblemSolution> solutions)
    {
        algorithmStartNanoTime = System.nanoTime();
    }

    @Override
    public void informAlgorithmEnds(
        VehicleRoutingProblem problem,
        Collection<VehicleRoutingProblemSolution> solutions)
    {
        algorithmEndNanoTime = System.nanoTime();

        if (iterationStarted == iterationEnded)
            return;
        informIterationEnds(iterationStarted, problem, solutions);
    }
}
