package pl.pielat.benchmark;

import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import pl.pielat.benchmark.solutionProcessing.BenchmarkSolutionProcessor;

public class DummySolutionProcessor implements BenchmarkSolutionProcessor
{
    @Override
    public void processSingleRun(VehicleRoutingProblemSolution solution, int runIdx, int problemIdx, int algorithmIdx)
    {

    }

    @Override
    public void aggregateAllRuns(VehicleRoutingProblemSolution[] solutions, int problemIdx, int algorithmIndex)
    {

    }
}
