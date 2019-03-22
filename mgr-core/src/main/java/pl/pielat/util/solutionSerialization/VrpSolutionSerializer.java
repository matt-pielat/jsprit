package pl.pielat.util.solutionSerialization;

import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import pl.pielat.util.metadata.AlgorithmRunMetadata;

import java.io.PrintWriter;

public interface VrpSolutionSerializer
{
    void serialize(VehicleRoutingProblemSolution solution, AlgorithmRunMetadata metadata, PrintWriter writer);
}
