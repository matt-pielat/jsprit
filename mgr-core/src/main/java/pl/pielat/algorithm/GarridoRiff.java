package pl.pielat.algorithm;

import com.graphhopper.jsprit.core.algorithm.SearchStrategy;
import com.graphhopper.jsprit.core.algorithm.SearchStrategyManager;
import com.graphhopper.jsprit.core.algorithm.SearchStrategyModule;
import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.acceptor.GreedyAcceptance;
import com.graphhopper.jsprit.core.algorithm.selector.SelectBest;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.solution.SolutionCostCalculator;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import pl.pielat.algorithm.objectiveFunction.TotalDistance;

import java.util.Random;
import java.util.Stack;

public class GarridoRiff
{
    private int populationSize;
    private int offspringSize;
    private int chromosomeSize;

    private Random random;

    public GarridoRiff()
    {
        //TODO tune
        // Defaults
        populationSize = 10;
        offspringSize = 5;
        chromosomeSize = 4;

        random = new Random();
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

    public VehicleRoutingAlgorithm createAlgorithm(final VehicleRoutingProblem vrp, boolean transportAsymmetry, boolean timeWindows)
    {
        SolutionCostCalculator dummyCostCalculator = new SolutionCostCalculator() {
            @Override
            public double getCosts(VehicleRoutingProblemSolution solution)
            {
                return solution.getCost();
            }
        };
        SolutionCostCalculator objectiveFunction = new TotalDistance(vrp.getTransportCosts());

        SearchStrategyModule searchStrategyModule = new EvolutionaryHyperheuristicModule(
            vrp, transportAsymmetry, timeWindows, populationSize, offspringSize, chromosomeSize,
            createCostCalculator(vrp));

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

    private static SolutionCostCalculator createCostCalculator(final VehicleRoutingProblem vrp)
    {
        return new SolutionCostCalculator() //TODO revise
        {
            @Override
            public double getCosts(VehicleRoutingProblemSolution solution)
            {
                double costs = 0.;
                for (VehicleRoute route : solution.getRoutes()) {
                    TourActivity prevAct = route.getStart();
                    for (TourActivity act : route.getActivities()) {
                        costs += vrp.getTransportCosts().getTransportCost(prevAct.getLocation(), act.getLocation(), prevAct.getEndTime(), route.getDriver(), route.getVehicle());
                        prevAct = act;
                    }
                    costs += vrp.getTransportCosts().getTransportCost(prevAct.getLocation(), route.getEnd().getLocation(), prevAct.getEndTime(), route.getDriver(), route.getVehicle());
                }
                return costs;
            }
        };
    }
}
