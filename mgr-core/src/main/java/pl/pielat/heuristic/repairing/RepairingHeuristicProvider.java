package pl.pielat.heuristic.repairing;

import pl.pielat.algorithm.ProblemInfo;
import pl.pielat.algorithm.TabuRandomizer;
import pl.pielat.heuristic.repairing.concrete.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class RepairingHeuristicProvider extends TabuRandomizer<RepairingHeuristic>
{
    private List<RepairingHeuristic> heuristics;

    public RepairingHeuristicProvider(ProblemInfo problemInfo, Random random)
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
