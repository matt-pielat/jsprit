package pl.pielat.algorithm;

import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import pl.pielat.heuristic.constructive.ConstructiveHeuristicProvider;
import pl.pielat.heuristic.ordering.OrderingHeuristicProvider;
import pl.pielat.heuristic.repairing.RepairingHeuristicProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GeneticOperatorManager
{
    private Random random;

    private ConstructiveHeuristicProvider constructiveHP;
    private OrderingHeuristicProvider orderingHP;
    private RepairingHeuristicProvider localImprovementHP;
    private RepairingHeuristicProvider improvementHP;

    public GeneticOperatorManager(VehicleRoutingProblem vrp, Random random)
    {
        this.random = random;

        constructiveHP = new ConstructiveHeuristicProvider(vrp, random);
        orderingHP = new OrderingHeuristicProvider(vrp, random);
        localImprovementHP = new RepairingHeuristicProvider(vrp, random);
        improvementHP = new RepairingHeuristicProvider(vrp, random);
    }

    public Chromosome[] crossChromosomes(Chromosome parentA, Chromosome parentB) throws Exception
    {
        int customersToInsert = parentA.getCustomersToInsertCount();
        if (customersToInsert != parentB.getCustomersToInsertCount())
            throw new Exception("Customers to insert count differs.");

        if (parentA.size() <= 1 || parentB.size() <= 1)
            throw new Exception("Both chromosomes must heave at least two genes.");

        Chromosome offspringA = new Chromosome(parentA);
        Chromosome offspringB = new Chromosome(parentB);

        // Perform crossover
        int min = 1;
        int max = offspringA.size() - 1;
        int crossPointA = random.nextInt(max - min + 1) + min;

        min = Math.max(crossPointA - customersToInsert + parentB.size(), 1);
        max = Math.min(crossPointA + customersToInsert - parentA.size(), offspringB.size() - 1);
        int crossPointB = random.nextInt(max - min + 1) + min;

        List<Gene> tempA = new ArrayList<Gene>(offspringA.size() - crossPointA);
        for (int i = crossPointA; i < offspringA.size(); i++)
            tempA.add(offspringA.get(i));

        List<Gene> tempB = new ArrayList<Gene>(offspringB.size() - crossPointB);
        for (int i = crossPointB; i < offspringB.size(); i++)
            tempB.add(offspringB.get(i));

        offspringA.removeRange(crossPointA, offspringA.size());
        offspringA.addAll(tempB);

        offspringB.removeRange(crossPointB, offspringB.size());
        offspringB.addAll(tempA);

        // Fix the number of customers to insert
        postCrossConsistencyFix(offspringA, customersToInsert);
        postCrossConsistencyFix(offspringB, customersToInsert);

        return new Chromosome[] {offspringA, offspringB};
    }

    private void postCrossConsistencyFix(Chromosome a, int targetCount)
    {
        int surplus = a.getCustomersToInsertCount() - targetCount;
        if (surplus == 0)
            return;

        while (surplus > 0)
        {
            for (int i = a.size() - 1; i >= 0; i--)
            {
                a.get(i).customersToInsert--;
                surplus--;

                if (a.get(i).customersToInsert == 0)
                {
                    a.remove(i);
                    continue;
                }

                if (surplus == 0)
                {
                    return;
                }
            }
        }

        while (surplus < 0)
        {
            for (int i = a.size() - 1; i >= 0; i--)
            {
                a.get(i).customersToInsert++;
                surplus++;

                if (surplus == 0)
                    return;
            }
        }
    }

    public Chromosome addRandomGene(Chromosome parent)
    {
        Chromosome offspring = new Chromosome(parent);

        int idxToCopy, customersToInsert;
        do
        {
            idxToCopy = random.nextInt(offspring.size());
            customersToInsert = offspring.get(idxToCopy).customersToInsert;
        } while (customersToInsert <= 1); //TODO make it less nasty
        Gene oldGene = offspring.get(idxToCopy);
        Gene newGene = new Gene(oldGene);
        changeRandomHeuristic(newGene);

        oldGene.customersToInsert = random.nextInt(customersToInsert - 1) + 1;
        newGene.customersToInsert = customersToInsert - oldGene.customersToInsert;

        offspring.add(idxToCopy + 1, newGene);
        return offspring;
    }

    public Chromosome replaceRandomGene(Chromosome parent)
    {
        Chromosome offspring = new Chromosome(parent);
        int idxToDistort = random.nextInt(offspring.size());
        changeRandomHeuristic(offspring.get(idxToDistort));

        return offspring;
    }

    private void changeRandomHeuristic(Gene a)
    {
        switch (random.nextInt(4))
        {
            case 0:
                a.constructiveHeuristic = constructiveHP.getRandomInstance(true);
                break;
            case 1:
                a.orderingHeuristic = orderingHP.getRandomInstance(true);
                break;
            case 2:
                a.localImprovementHeuristic = localImprovementHP.getRandomInstance(true);
                break;
            case 3:
                a.improvementHeuristic = improvementHP.getRandomInstance(true);
                break;
        }
    }

    public Chromosome deleteRandomGene(Chromosome parent) throws Exception
    {
        if (parent.size() == 1)
            throw new Exception("Can't apply delete operator on a single-gene chromosome.");

        Chromosome offspring = new Chromosome(parent);

        int idxToDelete = random.nextInt(offspring.size());
        int idxToReinforce = (idxToDelete - 1 + offspring.size()) % offspring.size();
        int surplus = offspring.get(idxToDelete).customersToInsert;

        offspring.get(idxToReinforce).customersToInsert += surplus;
        offspring.remove(idxToDelete);
        return offspring;
    }
}
