package pl.pielat.algorithm;

import com.graphhopper.jsprit.core.algorithm.SearchStrategyManager;
import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.termination.PrematureAlgorithmTermination;
import com.graphhopper.jsprit.core.algorithm.termination.TimeTermination;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.solution.SolutionCostCalculator;
import pl.pielat.util.metadata.HeuristicUsageStatistics;
import pl.pielat.util.metadata.HeuristicUsages;

import java.util.List;

public class GarridoRiffAlgorithm extends VehicleRoutingAlgorithm
{
    public GarridoRiffAlgorithm(
        VehicleRoutingProblem problem,
        SearchStrategyManager searchStrategyManager,
        SolutionCostCalculator objectiveFunction)
    {
        super(problem, searchStrategyManager, objectiveFunction);
    }

    @Override
    public void setPrematureAlgorithmTermination(PrematureAlgorithmTermination termination)
    {
        super.setPrematureAlgorithmTermination(termination);

        EvolutionaryHyperheuristicModule module = (EvolutionaryHyperheuristicModule)super
            .getSearchStrategyManager()
            .getRandomStrategy()
            .getSearchStrategyModules()
            .iterator()
            .next();

        module.setTimeTermination(null);
        if (termination instanceof TimeTermination)
        {
            module.setTimeTermination((TimeTermination)termination);
        }
    }

    public HeuristicUsageStatistics getHeuristicUsageStatistics()
    {
        EvolutionaryHyperheuristicModule module = (EvolutionaryHyperheuristicModule)super
            .getSearchStrategyManager()
            .getRandomStrategy()
            .getSearchStrategyModules()
            .iterator()
            .next();

        return module.getHeuristicUsageStatistics();
    }
}
