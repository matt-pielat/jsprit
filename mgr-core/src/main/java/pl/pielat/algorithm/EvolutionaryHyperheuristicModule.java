package pl.pielat.algorithm;

import com.graphhopper.jsprit.core.algorithm.SearchStrategyModule;
import com.graphhopper.jsprit.core.algorithm.listener.SearchStrategyModuleListener;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.solution.SolutionCostCalculator;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import pl.pielat.heuristic.constructive.ConstructiveHeuristic;
import pl.pielat.heuristic.constructive.ConstructiveHeuristicProvider;
import pl.pielat.heuristic.ordering.OrderingHeuristic;
import pl.pielat.heuristic.ordering.OrderingHeuristicProvider;
import pl.pielat.heuristic.repairing.RepairingHeuristic;
import pl.pielat.heuristic.repairing.RepairingHeuristicProvider;

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
    private VehicleRoutingProblemSolution bestSolution;
    private int initChromoSize;
    private boolean initialized;
    private int customersToInsert;

    private Random random;
    private GeneticOperatorManager operatorManager;
    private SolutionCostCalculator costCalculator;
    private int epoch = 0;

    private VehicleRoutingProblem problem;
    private ProblemInfo problemInfo;

    private EntityConverter converter;

    private static boolean DEBUG = false;


    public EvolutionaryHyperheuristicModule(VehicleRoutingProblem problem,
                                            boolean transportAsymmetry, boolean timeWindows,
                                            int popSize, int offspringSize, int initChromoSize,
                                            SolutionCostCalculator costCalculator)
    {
        this.problem = problem;

        converter = new EntityConverter(problem);
        problemInfo = converter.getProblemInfo(transportAsymmetry, timeWindows);

        this.popSize = popSize;
        this.offspringSize = offspringSize;
        this.initChromoSize = initChromoSize;
        initialized = false;
        this.costCalculator = costCalculator;
        customersToInsert = problem.getJobs().size();

        random = new Random(); //TODO parametrize?
        operatorManager = new GeneticOperatorManager(problemInfo, random);
    }

    private void createRandomPopulation()
    {
        int jobCount = customersToInsert;

        try
        {
            if (initChromoSize > jobCount)
                throw new Exception("Chromosome size is bigger than job count.");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            initChromoSize = jobCount;
        }

        ConstructiveHeuristicProvider constructiveHP = new ConstructiveHeuristicProvider(problemInfo, random);
        OrderingHeuristicProvider orderingHP = new OrderingHeuristicProvider(problemInfo, random);
        RepairingHeuristicProvider localImprovementHP = new RepairingHeuristicProvider(problemInfo, random);
        RepairingHeuristicProvider improvementHP = new RepairingHeuristicProvider(problemInfo, random);

        population = new Chromosome[popSize];
        fitness = new double[popSize];
        eliteIdx = -1;

        for (int i = 0; i < popSize; i++)
        {
            List<Gene> genes = new ArrayList<>(initChromoSize);
            jobCount = problem.getJobs().size();

            for (int j = 0; j < initChromoSize; j++)
            {
                Gene gene = new Gene();
                gene.customersToInsert = 1;
                jobCount--;

                // Constructive
                ConstructiveHeuristic ch = constructiveHP.getRandomInstance(false);
                gene.constructiveHeuristic = ch;

                // Ordering
                OrderingHeuristic oh = orderingHP.getRandomInstance(false);
                gene.orderingHeuristic = oh;

                // Local improvement
                RepairingHeuristic lih = localImprovementHP.getRandomInstance(false);
                gene.localImprovementHeuristic = lih;

                // Improvement
                RepairingHeuristic rh = improvementHP.getRandomInstance(false);
                gene.improvementHeuristic = rh;

                genes.add(gene);
            }

            while (jobCount > 0)
            {
                int geneIdx = random.nextInt(initChromoSize);
                genes.get(geneIdx).customersToInsert++;
                jobCount--;
            }

            population[i] = new Chromosome();
            population[i].addAll(genes);

            VehicleRoutingProblemSolution solution = converter.getSolution(population[i].calculateSolution(problemInfo));
            fitness[i] = 1 / solution.getCost();

            if (eliteIdx == -1 || fitness[i] > fitness[eliteIdx])
            {
                eliteIdx = i;
                bestSolution = solution;
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
            boolean[] ignoredPops = new boolean[popSize];
            try
            {
                int breederIdx = selectRandomIndividualIndex(ignoredPops);
                Chromosome breeder = population[breederIdx];

                // Don't allow more genes than customers to insert
                int rMin = breeder.size() == customersToInsert ? 1 : 0;
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
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        // Kill off stochastically least fit individuals
        boolean[] popsToKeepAlive = new boolean[popSize];
        popsToKeepAlive[eliteIdx] = true;
        try
        {
            for (int i = 0; i < offspringSize; i++)
            {
                int selectedIdx = selectRandomIndividualIndex(popsToKeepAlive);
                popsToKeepAlive[selectedIdx] = true;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        // Replace part of the population individuals with offspring
        for (int i = 0, j = 0; i < popSize; i++)
        {
            if (popsToKeepAlive[i])
                continue;

            population[i] = offspring.get(j);
            j++;

            VehicleRoutingProblemSolution newSolution = converter.getSolution(population[i].calculateSolution(problemInfo));
            fitness[i] = 1 / newSolution.getCost();

            // Update elite individual
            if (fitness[i] > fitness[eliteIdx])
            {
                eliteIdx = i;
                bestSolution = newSolution;
            }
        }

        if (DEBUG)
        {
            System.out.print("Best solution cost: " + bestSolution.getCost() + "\n");
            for (int i = 0; i < popSize; i++)
            {
                if (i == eliteIdx)
                    System.out.print("(" + population[i].size() + ") ");
                else
                    System.out.print(population[i].size() + " ");
            }
            System.out.println();
        }

        return bestSolution;
    }

    private int selectRandomIndividualIndex(boolean[] ignoredPops) throws Exception
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
        throw new Exception("Could not find any valid individual.");
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
