package pl.pielat.benchmark.solutionProcessing;

import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import pl.pielat.util.logging.Logger;

import java.io.*;

public class CsvResultsSerializer implements BenchmarkSolutionProcessor
{
    private Logger logger;

    private String[] problemIds;
    private File[] resultFiles;
    private boolean[] headerCreated;

    public CsvResultsSerializer(String[] csvPaths, String[] problemIds, Logger logger)
    {
        this.logger = logger;
        this.problemIds = problemIds;

        resultFiles = new File[csvPaths.length];
        headerCreated = new boolean[csvPaths.length];

        for (int i = 0; i < csvPaths.length; i++)
        {
            File resultFile = new File(csvPaths[i]);

            if (resultFile.isDirectory())
                throw new IllegalArgumentException();

            try
            {
                resultFile.delete();
                resultFile.createNewFile();
            }
            catch (IOException e)
            {
                logger.log(e);
                throw new RuntimeException(e);
            }

            resultFiles[i] = resultFile;
        }
    }

    @Override
    public void processSingleRun(VehicleRoutingProblemSolution solution, int runIdx, int problemIdx, int algorithmIdx)
    {
    }

    @Override
    public void aggregateAllRuns(VehicleRoutingProblemSolution[] solutions, int problemIdx, int algorithmIndex)
    {
        if (algorithmIndex >= resultFiles.length)
        {
            logger.log("Algorithm index greater than expected, %d>=%d.", algorithmIndex, resultFiles.length);
            return;
        }

        File file = resultFiles[algorithmIndex];

        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(file, true))))
        {
            if (!headerCreated[algorithmIndex])
            {
                writer.print("InstanceId");
                for (int i = 0; i < solutions.length; i++)
                    writer.printf(";Run%d", i + 1);
                writer.println();

                headerCreated[algorithmIndex] = true;
            }

            writer.print(problemIds[problemIdx]);
            for (int j = 0; j < solutions.length; j++)
                writer.printf(";%f", solutions[j].getCost());
            writer.println();
        }
        catch (IOException e)
        {
            logger.log("Failed to serialize results to file %s.", file.getAbsolutePath());
            logger.log(e);
        }
    }
}
