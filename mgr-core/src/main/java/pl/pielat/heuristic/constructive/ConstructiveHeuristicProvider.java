package pl.pielat.heuristic.constructive;

import pl.pielat.algorithm.ProblemInfo;
import pl.pielat.algorithm.TabuRandomizer;
import pl.pielat.heuristic.constructive.concrete.ClarkeWrightHeuristic;
import pl.pielat.heuristic.constructive.concrete.KilbyAlgorithm;
import pl.pielat.heuristic.constructive.concrete.MoleJamesonHeuristic;
import pl.pielat.heuristic.constructive.concrete.SweepAlgorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class ConstructiveHeuristicProvider extends TabuRandomizer<ConstructiveHeuristic>
{
    private ArrayList<ConstructiveHeuristic> heuristics;

    public ConstructiveHeuristicProvider(ProblemInfo problemInfo, Random random)
    {
        super(random);
        heuristics = new ArrayList<>(4);

        heuristics.addAll(Arrays.asList(
            new ClarkeWrightHeuristic(problemInfo),
            new KilbyAlgorithm(problemInfo),
            new MoleJamesonHeuristic(problemInfo)));

        if (!problemInfo.timeWindows && !problemInfo.noCoordinates)
            heuristics.add( new SweepAlgorithm(problemInfo));
    }

    @Override
    protected int getSetSize()
    {
        return heuristics.size();
    }

    @Override
    protected ConstructiveHeuristic getItemByIndex(int index)
    {
        return heuristics.get(index);
    }
}
