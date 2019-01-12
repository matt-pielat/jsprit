package pl.pielat.benchmark.solutionProcessing;

import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;

public class ProcessingArgs
{
    public final int runIndex;
    public final int problemIndex;
    public final int algorithmIndex;
    public final long millisecondsSinceRunStart;

    public final VehicleRoutingProblemSolution bestSolution;

    public ProcessingArgs(
        int runIndex,
        int problemIndex,
        int algorithmIndex,
        long millisecondsSinceRunStart,
        VehicleRoutingProblemSolution bestSolution)
    {
        this.runIndex = runIndex;
        this.problemIndex = problemIndex;
        this.algorithmIndex = algorithmIndex;
        this.millisecondsSinceRunStart = millisecondsSinceRunStart;
        this.bestSolution = bestSolution;
    }
}
