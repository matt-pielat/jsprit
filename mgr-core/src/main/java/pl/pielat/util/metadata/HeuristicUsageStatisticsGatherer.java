package pl.pielat.util.metadata;

import pl.pielat.algorithm.Gene;

import java.util.ArrayList;
import java.util.HashMap;

public class HeuristicUsageStatisticsGatherer
{
    private HashMap<String, HeuristicUsages> orderingHeuristicUsages;
    private HashMap<String, HeuristicUsages> constructiveHeuristicUsages;
    private HashMap<String, HeuristicUsages> repairingHeuristicUsages;

    public HeuristicUsageStatisticsGatherer()
    {
        orderingHeuristicUsages = new HashMap<>();
        constructiveHeuristicUsages = new HashMap<>();
        repairingHeuristicUsages = new HashMap<>();
    }

    private static void incrementHeuristicUsages(
        HashMap<String, HeuristicUsages> usagesMap,
        String id)
    {
        HeuristicUsages usages;
        if (!usagesMap.containsKey(id))
        {
            usages = new HeuristicUsages();
            usages.id = id;
            usages.usageCount = 0;
            usagesMap.put(id, usages);
        }
        else
        {
            usages = usagesMap.get(id);
        }

        usages.usageCount++;
    }

    public void incrementHeuristicUsages(Gene gene)
    {
        incrementHeuristicUsages(orderingHeuristicUsages, gene.orderingHeuristic.Id);
        incrementHeuristicUsages(constructiveHeuristicUsages, gene.constructiveHeuristic.Id);
        incrementHeuristicUsages(repairingHeuristicUsages, gene.localImprovementHeuristic.Id);
        incrementHeuristicUsages(repairingHeuristicUsages, gene.improvementHeuristic.Id);
    }

    public HeuristicUsageStatistics getStatistics()
    {
        HeuristicUsageStatistics statistics = new HeuristicUsageStatistics();
        statistics.orderingHeuristicUsages = new ArrayList<>(orderingHeuristicUsages.size());
        statistics.constructiveHeuristicUsages = new ArrayList<>(constructiveHeuristicUsages.size());
        statistics.repairingHeuristicUsages = new ArrayList<>(repairingHeuristicUsages.size());

        for (String key : orderingHeuristicUsages.keySet())
        {
            HeuristicUsages usages = new HeuristicUsages();
            usages.id = key;
            usages.usageCount = orderingHeuristicUsages.get(key).usageCount;

            statistics.orderingHeuristicUsages.add(usages);
        }

        for (String key : constructiveHeuristicUsages.keySet())
        {
            HeuristicUsages usages = new HeuristicUsages();
            usages.id = key;
            usages.usageCount = constructiveHeuristicUsages.get(key).usageCount;

            statistics.constructiveHeuristicUsages.add(usages);
        }

        for (String key : repairingHeuristicUsages.keySet())
        {
            HeuristicUsages usages = new HeuristicUsages();
            usages.id = key;
            usages.usageCount = repairingHeuristicUsages.get(key).usageCount;

            statistics.repairingHeuristicUsages.add(usages);
        }

        return statistics;
    }
}
