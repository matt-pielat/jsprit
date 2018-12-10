package pl.pielat.benchmark;

import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.selector.SelectBest;
import com.graphhopper.jsprit.core.algorithm.selector.SolutionSelector;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import pl.pielat.algorithm.ExtendedProblemDefinition;
import pl.pielat.util.logging.DummyLogger;
import pl.pielat.util.logging.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;

class BenchmarkRunner
{
    private final ExtendedProblemDefinition[] problemInstances;
    private final BenchmarkSolutionProcessor solutionProcessor;
    private final AlgorithmFactory[] algorithmFactories;
    private final SolutionSelector solutionSelector;
    private final Logger logger;

    private final int runsPerProblem;

    private File currentlyProcessedFile;

    public BenchmarkRunner(BenchmarkRunnerArgs args) throws FileNotFoundException
    {
        problemInstances = args.problemInstances;
        solutionProcessor = args.solutionProcessor;
        algorithmFactories = args.algorithmFactories;
        solutionSelector = new SelectBest();

        if (args.logger == null)
            logger = new DummyLogger();
        else
            logger = args.logger;

        runsPerProblem = args.runsPerProblem;
    }

    public void run()
    {
        int algorithmCount = algorithmFactories.length;
        int problemCount = problemInstances.length;

        logger.log("Starting main loop...");
        for (int p = 0; p < problemInstances.length; p++)
        {
            ExtendedProblemDefinition vrp = problemInstances[p];
            logger.log("Problem %d/%d processing start.", p + 1, problemCount);

            VehicleRoutingProblemSolution[][] solutions =
                new VehicleRoutingProblemSolution[algorithmCount][runsPerProblem];

            for (int r = 0; r < runsPerProblem; r++)
            {
                for (int a = 0; a < algorithmCount; a++)
                {
                    logger.log("Algorithm %d start (run %d/%d).", a + 1, r + 1, runsPerProblem);

                    VehicleRoutingAlgorithm vra = algorithmFactories[a].createAlgorithm(vrp);
                    Collection<VehicleRoutingProblemSolution> foundSolutions = vra.searchSolutions();

                    if (foundSolutions.isEmpty())
                    {
                        logger.log("Algorithm finished - solution not found.");
                        continue;
                    }
                    VehicleRoutingProblemSolution bestSolution = solutionSelector.selectSolution(foundSolutions);
                    logger.log("Algorithm finished - solution found.");

                    solutions[a][r] = bestSolution;
                    solutionProcessor.processSingleRun(bestSolution, r, p, a);
                }
            }

            logger.log("Problem processing finished.");

            for (int a = 0; a < algorithmCount; a++)
                solutionProcessor.aggregateAllRuns(solutions[a], p, a);
        }
        logger.log("Main loop finished.");
    }
}
