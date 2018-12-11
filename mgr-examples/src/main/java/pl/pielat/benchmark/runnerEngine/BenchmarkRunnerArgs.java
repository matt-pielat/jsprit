package pl.pielat.benchmark.runnerEngine;

import pl.pielat.algorithm.ExtendedProblemDefinition;
import pl.pielat.benchmark.algorithmCreation.AlgorithmFactory;
import pl.pielat.benchmark.solutionProcessing.BenchmarkSolutionProcessor;
import pl.pielat.util.logging.Logger;

public class BenchmarkRunnerArgs
{
    public ExtendedProblemDefinition[] problemInstances;
    public AlgorithmFactory algorithmFactories[];
    public Logger logger;
    public int runsPerProblem;
    public BenchmarkSolutionProcessor solutionProcessor;
}
