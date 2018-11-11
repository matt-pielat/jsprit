package pl.pielat.heuristicNew.constructive;

import pl.pielat.algorithm.TabuRandomizer;
import pl.pielat.heuristicNew.BaseHeuristic;
import pl.pielat.heuristicNew.constructive.concrete.ClarkeWrightHeuristic;
import pl.pielat.heuristicNew.constructive.concrete.KilbyAlgorithm;
import pl.pielat.heuristicNew.constructive.concrete.MoleJamesonHeuristic;
import pl.pielat.heuristicNew.constructive.concrete.SweepAlgorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class ConstructiveHeuristicProvider extends TabuRandomizer<ConstructiveHeuristic>
{
    private ArrayList<ConstructiveHeuristic> heuristics;

    public ConstructiveHeuristicProvider(BaseHeuristic.ProblemInfo problemInfo, Random random)
    {
        super(random);
        heuristics = new ArrayList<>(4);

        heuristics.addAll(Arrays.asList(
            new ClarkeWrightHeuristic(problemInfo),
            new KilbyAlgorithm(problemInfo),
            new MoleJamesonHeuristic(problemInfo)));

        if (!problemInfo.timeWindows)
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
