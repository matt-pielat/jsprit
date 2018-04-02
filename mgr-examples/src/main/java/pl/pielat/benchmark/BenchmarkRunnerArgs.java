package pl.pielat.benchmark;

import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import pl.pielat.util.logging.Logger;
import pl.pielat.util.problemParsing.VrpFileParser;
import pl.pielat.util.solutionSerialization.VrpSolutionSerializer;

import java.io.File;

class BenchmarkRunnerArgs
{
    public VehicleRoutingProblem[] problemInstances;
    public AlgorithmFactory algorithmFactories[];
    public Logger logger;
    public int runsPerProblem;
    public BenchmarkSolutionProcessor solutionProcessor;
}
