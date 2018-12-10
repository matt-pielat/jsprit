package pl.pielat.benchmark;

import pl.pielat.algorithm.ExtendedProblemDefinition;
import pl.pielat.util.logging.Logger;

class BenchmarkRunnerArgs
{
    public ExtendedProblemDefinition[] problemInstances;
    public AlgorithmFactory algorithmFactories[];
    public Logger logger;
    public int runsPerProblem;
    public BenchmarkSolutionProcessor solutionProcessor;
}
