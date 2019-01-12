package pl.pielat.benchmark;

import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import pl.pielat.benchmark.solutionProcessing.IterationProcessor;
import pl.pielat.benchmark.solutionProcessing.ProcessingArgs;
import pl.pielat.benchmark.solutionProcessing.RunProcessor;

public class DummySolutionProcessor implements RunProcessor, IterationProcessor
{
    @Override
    public void processIteration(ProcessingArgs args, int iterationIdx)
    {
    }

    @Override
    public void processRun(ProcessingArgs args)
    {
    }
}
