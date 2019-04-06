package pl.pielat.util.metadata;

import pl.pielat.algorithm.Gene;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HeuristicUsageStatisticsGatherer
{
    private HashMap<String, Integer> heuristicUsages;

    public HeuristicUsageStatisticsGatherer()
    {
        heuristicUsages = new HashMap<>();
    }

    public void setHeuristicUsages(String id, int value)
    {
        heuristicUsages.put(id, value);
    }

    public void incrementHeuristicUsages(String id)
    {
        if (!heuristicUsages.containsKey(id))
        {
            heuristicUsages.put(id, 1);
        }
        else
        {
            Integer oldValue = heuristicUsages.get(id);
            heuristicUsages.put(id, oldValue + 1);
        }
    }

    public void incrementHeuristicUsages(Gene gene)
    {
        incrementHeuristicUsages(gene.orderingHeuristic.Id);
        incrementHeuristicUsages(gene.constructiveHeuristic.Id);
        incrementHeuristicUsages(gene.localImprovementHeuristic.Id);
        incrementHeuristicUsages(gene.improvementHeuristic.Id);
    }

    public List<HeuristicUsages> getStatistics()
    {
        ArrayList<HeuristicUsages> statistics = new ArrayList<>(heuristicUsages.size());

        for (String key : heuristicUsages.keySet())
        {
            HeuristicUsages usages = new HeuristicUsages();
            usages.id = key;
            usages.usageCount = heuristicUsages.get(key);

            statistics.add(usages);
        }

        return statistics;
    }
}
