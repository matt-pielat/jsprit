package pl.pielat.util.metadata;

import pl.pielat.algorithm.Gene;

import java.util.ArrayList;
import java.util.HashMap;

public class EhDvrpStatisticsGatherer
{
    private HashMap<String, HeuristicUsages> orderingHeuristicUsages;
    private HashMap<String, HeuristicUsages> constructiveHeuristicUsages;
    private HashMap<String, HeuristicUsages> repairingHeuristicUsages;

    private HashMap<Integer, Integer> chromosomeSizes;

    public EhDvrpStatisticsGatherer()
    {
        orderingHeuristicUsages = new HashMap<>();
        constructiveHeuristicUsages = new HashMap<>();
        repairingHeuristicUsages = new HashMap<>();

        chromosomeSizes = new HashMap<>();
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
        incrementHeuristicUsages(orderingHeuristicUsages, gene.orderingHeuristic.getId());
        incrementHeuristicUsages(constructiveHeuristicUsages, gene.constructiveHeuristic.getId());
        incrementHeuristicUsages(repairingHeuristicUsages, gene.localImprovementHeuristic.getId());
        incrementHeuristicUsages(repairingHeuristicUsages, gene.improvementHeuristic.getId());
    }

    public void incrementChromosomeSizeCounter(int size)
    {
        Integer count = chromosomeSizes.get(size);
        if (count == null)
            count = 0;
        count++;
        chromosomeSizes.put(size, count);
    }

    public EhDvrpStatistics getStatistics()
    {
        EhDvrpStatistics statistics = new EhDvrpStatistics();
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

        int maxSize = 0;
        for (Integer size : chromosomeSizes.keySet())
        {
            maxSize = size > maxSize ? size : maxSize;
        }
        statistics.chromosomeSizes = new int[maxSize + 1];
        for (Integer size : chromosomeSizes.keySet())
        {
            statistics.chromosomeSizes[size] = chromosomeSizes.get(size);
        }

        return statistics;
    }
}
