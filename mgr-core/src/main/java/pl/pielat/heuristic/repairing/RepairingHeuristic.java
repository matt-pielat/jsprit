package pl.pielat.heuristic.repairing;

import pl.pielat.algorithm.ProblemInfo;
import pl.pielat.heuristic.BaseHeuristic;
import pl.pielat.heuristic.Route;

import java.util.ArrayList;

public abstract class RepairingHeuristic extends BaseHeuristic
{
    protected RepairingHeuristic(ProblemInfo info)
    {
        super(info);
    }

    public abstract void improveRoutes(ArrayList<Route> routes);
}
