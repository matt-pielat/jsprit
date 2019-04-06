package pl.pielat.algorithm;

import com.graphhopper.jsprit.core.algorithm.SearchStrategyModule;
import com.graphhopper.jsprit.core.algorithm.listener.SearchStrategyModuleListener;
import com.graphhopper.jsprit.core.algorithm.termination.TimeTermination;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import pl.pielat.heuristic.Route;
import pl.pielat.heuristic.constructive.ConstructiveHeuristicProvider;
import pl.pielat.heuristic.ordering.OrderingHeuristicProvider;
import pl.pielat.heuristic.repairing.RepairingHeuristicProvider;
import pl.pielat.util.metadata.HeuristicUsageStatistics;
import pl.pielat.util.metadata.HeuristicUsageStatisticsGatherer;
import pl.pielat.util.metadata.HeuristicUsages;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EvolutionaryHyperheuristicModule implements SearchStrategyModule
{
    private Chromosome[] population;
    private double[] fitness;
    private final int popSize;
    private final int offspringSize;
    private int eliteIdx;

    private int initChromosomeSize;
    private boolean initialized = false;
    private final int jobCount;

    private VehicleRoutingProblemSolution bestSolution;
    private ArrayList<Route> bestSolutionRoutes;

    private Random random = new Random();
    private int epoch = 0;

    private TimeTermination timeTermination = null;
    private ProblemInfo problemInfo;
    private EntityConverter converter;
    private ObjectiveFunction objectiveFunction;
    private GeneticOperatorManager operatorManager;
    private HeuristicUsageStatisticsGatherer statisticsGatherer;

    public EvolutionaryHyperheuristicModule(
        ExtendedProblemDefinition problemDefinition,
        int popSize,
        int offspringSize,
        int initChromosomeSize)
    {
        converter = new EntityConverter(problemDefinition.vrp);
        problemInfo = converter.getProblemInfo(problemDefinition.transportAsymmetry, problemDefinition.timeWindows);
        jobCount = problemInfo.jobs.size();

        if (initChromosomeSize > jobCount)
        {
            initChromosomeSize = jobCount;
        }

        this.popSize = popSize;
        this.offspringSize = offspringSize;
        this.initChromosomeSize = initChromosomeSize;

        objectiveFunction = new ObjectiveFunction(problemDefinition, problemDefinition.timeWindows);
        operatorManager = new GeneticOperatorManager(problemInfo);
        statisticsGatherer = new HeuristicUsageStatisticsGatherer();
    }

    public void setRandom(Random random)
    {
        this.random = random;
        operatorManager.setRandom(this.random);
    }

    public void setTimeTermination(TimeTermination timeTermination)
    {
        this.timeTermination = timeTermination;
    }

    private void createRandomPopulation()
    {
        ConstructiveHeuristicProvider constructiveHP = new ConstructiveHeuristicProvider(problemInfo, random);
        OrderingHeuristicProvider orderingHP = new OrderingHeuristicProvider(problemInfo, random);
        RepairingHeuristicProvider localImprovementHP = new RepairingHeuristicProvider(problemInfo, random);
        RepairingHeuristicProvider improvementHP = new RepairingHeuristicProvider(problemInfo, random);

        population = new Chromosome[popSize];
        fitness = new double[popSize];
        eliteIdx = -1;

        for (int i = 0; i < popSize; i++)
        {
            List<Gene> genes = new ArrayList<>(initChromosomeSize);
            int jobsToInsert = jobCount;

            for (int j = 0; j < initChromosomeSize; j++)
            {
                Gene gene = new Gene();
                gene.jobsToInsert = 1;
                jobsToInsert--;

                // Constructive
                gene.constructiveHeuristic = constructiveHP.getRandomInstance(false);

                // Ordering
                gene.orderingHeuristic = orderingHP.getRandomInstance(false);

                // Local improvement
                gene.localImprovementHeuristic = localImprovementHP.getRandomInstance(false);

                // Improvement
                gene.improvementHeuristic = improvementHP.getRandomInstance(false);

                genes.add(gene);
            }

            while (jobsToInsert > 0)
            {
                int geneIdx = random.nextInt(initChromosomeSize);
                genes.get(geneIdx).jobsToInsert++;
                jobsToInsert--;
            }

            population[i] = new Chromosome();
            population[i].addAll(genes);

            ArrayList<Route> resultRoutes = population[i].calculateSolution(problemInfo);
            double routesCost = objectiveFunction.getCosts(resultRoutes);

            fitness[i] = 1 / routesCost;

            if (eliteIdx == -1 || fitness[i] > fitness[eliteIdx])
            {
                eliteIdx = i;

                bestSolutionRoutes = resultRoutes;
                bestSolution = converter.getSolution(bestSolutionRoutes, routesCost);
            }
        }
    }

    @Override
    public VehicleRoutingProblemSolution runAndGetSolution(VehicleRoutingProblemSolution ignored)
    {
        if (!initialized)
        {
            createRandomPopulation();
            initialized = true;
        }
        epoch++;

        List<Chromosome> offspring = new ArrayList<>(offspringSize);
        for (int i = 0; i < offspringSize; i++)
        {
            if (timeTermination != null && timeTermination.isPrematureBreak(null))
            {
                break;
            }

            boolean[] ignoredPops = new boolean[popSize];

            int breederIdx = selectRandomIndividualIndex(ignoredPops);
            Chromosome breeder = population[breederIdx];

            // Don't allow more genes than customers to insert
            int rMin = breeder.size() == jobCount ? 1 : 0;

            // Don't apply delete & cross operator to one-gene individuals
            int rMax = breeder.size() == 1 ? 1 : 3;

            int r = random.nextInt(rMax - rMin + 1) + rMin;
            switch (r)
            {
                case 0: // Add
                    Chromosome addResult = operatorManager.addRandomGene(breeder);
                    offspring.add(addResult);
                    break;
                case 1: // Replace
                    Chromosome replaceResult = operatorManager.replaceRandomGene(breeder);
                    offspring.add(replaceResult);
                    break;
                case 2: // Delete
                    Chromosome deleteResult = operatorManager.deleteRandomGene(breeder);
                    offspring.add(deleteResult);
                    break;
                case 3: // Cross
                    for (int j = 0; j < popSize; j++)
                        ignoredPops[j] = population[j].size() == 1;
                    ignoredPops[breederIdx] = true;

                    int mateIdx = selectRandomIndividualIndex(ignoredPops);
                    if (mateIdx == -1)
                    {
                        i--;
                        continue;
                    }
                    Chromosome mate = population[mateIdx];

                    Chromosome[] crossResult = operatorManager.crossChromosomes(breeder, mate);
                    offspring.add(crossResult[0]);
                    if (offspring.size() < offspringSize)
                    {
                        offspring.add(crossResult[1]);
                        i++;
                    }
                    break;
            }
        }

        // Stochastically kill off least fit individuals
        boolean[] popsToKeepAlive = new boolean[popSize];
        popsToKeepAlive[eliteIdx] = true;

        for (int i = 0; i < popSize - offspring.size() - 1; i++)
        {
            int selectedIdx = selectRandomIndividualIndex(popsToKeepAlive);
            popsToKeepAlive[selectedIdx] = true;
        }

        // Replace part of the population individuals with offspring
        for (int i = 0, j = 0; i < popSize; i++)
        {
            if (popsToKeepAlive[i])
                continue;

            population[i] = offspring.get(j);
            j++;

            ArrayList<Route> resultRoutes = population[i].calculateSolution(problemInfo);
            double routesCost = objectiveFunction.getCosts(resultRoutes);

            fitness[i] = 1 / routesCost;

            // Update elite individual
            if (fitness[i] > fitness[eliteIdx])
            {
                eliteIdx = i;

                bestSolutionRoutes = resultRoutes;
                bestSolution = converter.getSolution(bestSolutionRoutes, routesCost);
            }

            for (Gene gene : population[i])
                statisticsGatherer.incrementHeuristicUsages(gene);
        }

        return bestSolution;
    }

    public HeuristicUsageStatistics getHeuristicUsageStatistics()
    {
        return statisticsGatherer.getStatistics();
    }

    private int selectRandomIndividualIndex(boolean[] ignoredPops)
    {
        double fitnessSum = 0;

        for (int i = 0; i < popSize; i++)
        {
            if (ignoredPops[i])
                continue;
            fitnessSum += fitness[i];
        }

        if (fitnessSum == 0)
            return -1;

        double threshold = random.nextDouble() * fitnessSum;
        double acc = 0;

        for (int i = 0; i < popSize; i++)
        {
            if (ignoredPops[i])
                continue;

            acc += fitness[i];
            if (acc > threshold)
                return i;
        }
        throw new RuntimeException("Could not find any valid individual.");
    }

    @Override
    public String getName()
    {
        return "evolutionaryHyperheuristic";
    }

    @Override
    public void addModuleListener(SearchStrategyModuleListener moduleListener)
    {
        //TODO
    }
}
