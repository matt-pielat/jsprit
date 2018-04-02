package pl.pielat.algorithm;

import pl.pielat.heuristic.constructive.ConstructiveHeuristic;
import pl.pielat.heuristic.ordering.OrderingHeuristic;
import pl.pielat.heuristic.repairing.RepairingHeuristic;

public class Gene
{
    public ConstructiveHeuristic constructiveHeuristic;
    public OrderingHeuristic orderingHeuristic;
    public RepairingHeuristic localImprovementHeuristic;
    public int customersToInsert = 0;

    public RepairingHeuristic improvementHeuristic;

    public Gene()
    {

    }

    public Gene(Gene other)
    {
        constructiveHeuristic = other.constructiveHeuristic;
        orderingHeuristic = other.orderingHeuristic;
        localImprovementHeuristic = other.localImprovementHeuristic;
        customersToInsert = other.customersToInsert;

        improvementHeuristic = other.improvementHeuristic;
    }
}
