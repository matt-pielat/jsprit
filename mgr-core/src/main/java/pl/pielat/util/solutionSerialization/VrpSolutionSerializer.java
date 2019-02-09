package pl.pielat.util.solutionSerialization;

import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;

import java.io.PrintWriter;

public interface VrpSolutionSerializer
{
    void serialize(VehicleRoutingProblemSolution solution, long millisecondsElapsed, PrintWriter writer);
}
