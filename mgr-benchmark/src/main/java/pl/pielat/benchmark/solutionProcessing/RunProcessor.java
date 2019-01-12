package pl.pielat.benchmark.solutionProcessing;

public interface RunProcessor
{
    void processIteration(ProcessingArgs args, int iterationIdx);

    void processRun(ProcessingArgs args);
}
