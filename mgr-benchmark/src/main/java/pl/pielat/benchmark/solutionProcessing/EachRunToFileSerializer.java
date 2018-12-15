package pl.pielat.benchmark.solutionProcessing;

import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import pl.pielat.util.logging.Logger;
import pl.pielat.util.solutionSerialization.VrpSolutionSerializer;

import java.io.*;

public class EachRunToFileSerializer implements BenchmarkSolutionProcessor
{
    private VrpSolutionSerializer serializer;
    private String[] problemIds;
    private File[] outputDirs;

    private Logger logger;

    public EachRunToFileSerializer(VrpSolutionSerializer serializer, String[] problemIds, File[] outputDirs, Logger logger)
    {
        this.serializer = serializer;
        this.problemIds = problemIds;
        this.outputDirs = outputDirs;

        this.logger = logger;
    }

    @Override
    public void processSingleRun(VehicleRoutingProblemSolution solution, int runIdx, int problemIdx, int algorithmIdx)
    {
        String filename = String.format("%s_%d.sol", problemIds[problemIdx], runIdx);

        File file = new File(outputDirs[algorithmIdx], filename);
        file.delete();

        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(file, false))))
        {
            serializer.serialize(solution, writer);
        }
        catch (IOException e)
        {
            logger.log("Failed to serialize results to file %s.", file.getAbsolutePath());
            logger.log(e);
        }
    }

    @Override
    public void aggregateAllRuns(VehicleRoutingProblemSolution[] solutions, int problemIdx, int algorithmIndex)
    {
    }
}
