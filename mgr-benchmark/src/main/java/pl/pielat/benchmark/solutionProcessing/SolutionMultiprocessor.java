package pl.pielat.benchmark.solutionProcessing;

import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;

public class SolutionMultiprocessor implements BenchmarkSolutionProcessor
{
    private BenchmarkSolutionProcessor[] processors;

    public SolutionMultiprocessor(BenchmarkSolutionProcessor... processors)
    {
        this.processors = processors;
    }

    @Override
    public void processSingleRun(VehicleRoutingProblemSolution solution, int runIdx, int problemIdx, int algorithmIdx)
    {
        for (BenchmarkSolutionProcessor bsp : processors)
        {
            bsp.processSingleRun(solution, runIdx, problemIdx, algorithmIdx);
        }
    }

    @Override
    public void aggregateAllRuns(VehicleRoutingProblemSolution[] solutions, int problemIdx, int algorithmIndex)
    {
        for (BenchmarkSolutionProcessor bsp : processors)
        {
            bsp.aggregateAllRuns(solutions, problemIdx, algorithmIndex);
        }
    }
}
