package pl.pielat.heuristicNew.repairing;

import pl.pielat.algorithm.TabuRandomizer;
import pl.pielat.heuristicNew.BaseHeuristic;
import pl.pielat.heuristicNew.repairing.concrete.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class RepairingHeuristicProvider extends TabuRandomizer<RepairingHeuristic>
{
    private List<RepairingHeuristic> heuristics;

    public RepairingHeuristicProvider(BaseHeuristic.ProblemInfo problemInfo, Random random)
    {
        super(random);

        heuristics = new ArrayList<>(Arrays.asList(
            new OrOpt(problemInfo),
            new StringCross(problemInfo),
            new StringExchange(problemInfo),
            new StringRelocation(problemInfo),
            new ThreeOpt(problemInfo),
            new TwoOpt(problemInfo)
        ));
    }

    @Override
    protected int getSetSize()
    {
        return heuristics.size();
    }

    @Override
    protected RepairingHeuristic getItemByIndex(int index)
    {
        return heuristics.get(index);
    }
}
