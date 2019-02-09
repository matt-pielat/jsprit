package pl.pielat.algorithm.factory;

import com.graphhopper.jsprit.core.algorithm.SearchStrategy;
import com.graphhopper.jsprit.core.algorithm.SearchStrategyManager;
import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.acceptor.GreedyAcceptance;
import com.graphhopper.jsprit.core.algorithm.selector.SelectBest;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.solution.SolutionCostCalculator;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import pl.pielat.algorithm.EvolutionaryHyperheuristicModule;
import pl.pielat.algorithm.ExtendedProblemDefinition;
import pl.pielat.algorithm.GarridoRiff;

import java.util.Random;
import java.util.Stack;

public class GarridoRiffFactory extends AlgorithmFactory
{
    public static final int DEFAULT_POPULATION_SIZE = 10;
    public static final int DEFAULT_OFFSPRING_SIZE = 5;
    public static final int DEFAULT_CHROMOSOME_SIZE = 5;

    private int populationSize;
    private int offspringSize;
    private int chromosomeSize;

    private Random random = new Random();

    public GarridoRiffFactory()
    {
        populationSize = DEFAULT_POPULATION_SIZE;
        offspringSize = DEFAULT_OFFSPRING_SIZE;
        chromosomeSize = DEFAULT_CHROMOSOME_SIZE;
    }

    public void setPopulationSize(int populationSize)
    {
        this.populationSize = populationSize;
    }

    public void setOffspringSize(int offspringSize)
    {
        this.offspringSize = offspringSize;
    }

    public void setChromosomeSize(int chromosomeSize)
    {
        this.chromosomeSize = chromosomeSize;
    }

    public void setRandom(Random random)
    {
        this.random = random;
    }

    @Override
    protected VehicleRoutingAlgorithm internalBuild(ExtendedProblemDefinition epd)
    {
        VehicleRoutingProblem vrp = epd.vrp;
        SolutionCostCalculator dummyCostCalculator = new SolutionCostCalculator() {
            @Override
            public double getCosts(VehicleRoutingProblemSolution solution)
            {
                return solution.getCost();
            }
        };

        EvolutionaryHyperheuristicModule searchStrategyModule = new EvolutionaryHyperheuristicModule(
            epd, populationSize, offspringSize, chromosomeSize);
        searchStrategyModule.setRandom(random);

        SearchStrategy searchStrategy = new SearchStrategy(
            "evolutionaryHyperheuristic",
            new SelectBest(),
            new GreedyAcceptance(1),
            dummyCostCalculator);

        searchStrategy.addModule(searchStrategyModule);

        SearchStrategyManager searchStrategyManager = new SearchStrategyManager();
        searchStrategyManager.setRandom(random);
        searchStrategyManager.addStrategy(searchStrategy, 1);

        VehicleRoutingAlgorithm vra = new VehicleRoutingAlgorithm(vrp, searchStrategyManager, dummyCostCalculator);

        VehicleRoutingProblemSolution initialSolution = new VehicleRoutingProblemSolution(
            new Stack<VehicleRoute>(),
            vrp.getJobs().values(),
            Double.POSITIVE_INFINITY);
        vra.addInitialSolution(initialSolution);

        return vra;
    }

    @Override
    public String getSerializableAlgorithmId()
    {
        return "GarridoRiff";
    }
}
