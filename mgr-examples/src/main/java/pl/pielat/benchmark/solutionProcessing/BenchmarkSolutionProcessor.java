package pl.pielat.benchmark.solutionProcessing;

import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;

public interface BenchmarkSolutionProcessor
{
    void processSingleRun(VehicleRoutingProblemSolution solution, int runIdx, int problemIdx, int algorithmIdx);

    void aggregateAllRuns(VehicleRoutingProblemSolution[] solutions, int problemIdx, int algorithmIndex);
}
