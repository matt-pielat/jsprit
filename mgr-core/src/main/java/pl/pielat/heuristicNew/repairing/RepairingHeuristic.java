package pl.pielat.heuristicNew.repairing;

import pl.pielat.heuristicNew.BaseHeuristic;
import pl.pielat.heuristicNew.Route;

import java.util.ArrayList;

public abstract class RepairingHeuristic extends BaseHeuristic
{
    protected RepairingHeuristic(ProblemInfo info)
    {
        super(info);
    }

    public abstract void improveRoutes(ArrayList<Route> routes);
}
