package pl.pielat.benchmark.runnerEngine;

import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.listener.AlgorithmEndsListener;
import com.graphhopper.jsprit.core.algorithm.listener.AlgorithmStartsListener;
import com.graphhopper.jsprit.core.algorithm.listener.IterationEndsListener;
import com.graphhopper.jsprit.core.algorithm.listener.IterationStartsListener;
import com.graphhopper.jsprit.core.algorithm.selector.SelectBest;
import com.graphhopper.jsprit.core.algorithm.selector.SolutionSelector;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import pl.pielat.algorithm.ExtendedProblemDefinition;
import pl.pielat.algorithm.ObjectiveFunction;
import pl.pielat.benchmark.algorithmCreation.AlgorithmFactory;
import pl.pielat.benchmark.solutionProcessing.IterationProcessor;
import pl.pielat.benchmark.solutionProcessing.ProcessingArgs;
import pl.pielat.benchmark.solutionProcessing.RunProcessor;
import pl.pielat.util.logging.DummyLogger;
import pl.pielat.util.logging.Logger;

import java.io.File;
import java.util.Collection;

public class BenchmarkRunner
{
    private final ExtendedProblemDefinition[] problemInstances;
    private RunProcessor runProcessor;
    private IterationProcessor iterationProcessor;
    private final AlgorithmFactory[] algorithmFactories;
    private final SolutionSelector solutionSelector;
    private final Logger logger;

    private final int runsPerProblem;

    private File currentlyProcessedFile;

    public BenchmarkRunner(BenchmarkRunnerArgs args)
    {
        problemInstances = args.problemInstances;
        algorithmFactories = args.algorithmFactories;
        solutionSelector = new SelectBest();

        if (args.logger == null)
            logger = new DummyLogger();
        else
            logger = args.logger;

        runsPerProblem = args.runsPerProblem;
    }

    public void setRunProcessor(RunProcessor processor)
    {
        runProcessor = processor;
    }

    public void setIterationProcessor(IterationProcessor processor)
    {
        iterationProcessor = processor;
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

            final ObjectiveFunction finalCostFunction = new ObjectiveFunction(vrp, false);

            VehicleRoutingProblemSolution[][] solutions =
                new VehicleRoutingProblemSolution[algorithmCount][runsPerProblem];

            for (int r = 0; r < runsPerProblem; r++)
            {
                final long[] runStartTimes = new long[algorithmCount];

                for (int a = 0; a < algorithmCount; a++)
                {
                    VehicleRoutingProblemSolution bestSolution;
                    logger.log("Algorithm %d start (run %d/%d).", a + 1, r + 1, runsPerProblem);

                    final int[] iterationStarted = new int[1];
                    iterationStarted[0] = -1;

                    final int[] iterationProcessed = new int[1];
                    iterationProcessed[0] = -1;

                    try
                    {
                        VehicleRoutingAlgorithm vra = algorithmFactories[a].build(vrp);

                        final int finalR = r;
                        final int finalP = p;
                        final int finalA = a;

                        vra.addListener(new IterationStartsListener() {
                            @Override
                            public void informIterationStarts(
                                int iterationIdx,
                                VehicleRoutingProblem problem,
                                Collection<VehicleRoutingProblemSolution> solutions)
                            {
                                iterationIdx--; // make iterations indices start at 0

                                if (iterationIdx == 0)
                                    runStartTimes[finalA] = System.nanoTime();

                                iterationStarted[0] = iterationIdx;
                            }
                        });

                        vra.addListener(new IterationEndsListener() {
                            @Override
                            public void informIterationEnds(
                                int iterationIdx,
                                VehicleRoutingProblem problem,
                                Collection<VehicleRoutingProblemSolution> solutions)
                            {
                                iterationIdx--; // make iterations indices start at 0

                                long msSinceStart = (System.nanoTime() - runStartTimes[finalA]) / 1000000;

                                VehicleRoutingProblemSolution bestSolution;
                                if (solutions.isEmpty())
                                {
                                    logger.log("Iteration finished - solution not found.");
                                    bestSolution = null;
                                }
                                else
                                {
                                    bestSolution = solutionSelector.selectSolution(solutions);
                                    bestSolution.setCost(finalCostFunction.getCosts(bestSolution));
                                }

                                ProcessingArgs args = new ProcessingArgs(finalR, finalP, finalA, msSinceStart, bestSolution);
                                iterationProcessor.processIteration(args, iterationIdx);

                                iterationProcessed[0] = iterationIdx;
                            }
                        });

                        vra.addListener(new AlgorithmEndsListener() {
                            @Override
                            public void informAlgorithmEnds(
                                VehicleRoutingProblem problem,
                                Collection<VehicleRoutingProblemSolution> solutions)
                            {
                                long msSinceStart = (System.nanoTime() - runStartTimes[finalA]) / 1000000;

                                VehicleRoutingProblemSolution bestSolution;
                                if (solutions.isEmpty())
                                {
                                    logger.log("Algorithm finished - solution not found.");
                                    bestSolution = null;
                                }
                                else
                                {
                                    bestSolution = solutionSelector.selectSolution(solutions);
                                    bestSolution.setCost(finalCostFunction.getCosts(bestSolution));
                                }

                                ProcessingArgs args = new ProcessingArgs(finalR, finalP, finalA, msSinceStart, bestSolution);

                                if (iterationStarted[0] > iterationProcessed[0])
                                    iterationProcessor.processIteration(args, iterationStarted[0]);
                                runProcessor.processRun(args);
                            }
                        });

                        vra.searchSolutions();
                    }
                    catch (Exception e)
                    {
                        logger.log(e);
                        continue;
                    }
                }
            }

            logger.log("Problem processing finished.");
        }

        logger.log("Main loop finished.");
    }
}
