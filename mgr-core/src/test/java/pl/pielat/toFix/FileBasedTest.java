package pl.pielat.toFix;

import com.graphhopper.jsprit.analysis.toolbox.Plotter;
import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.algorithm.selector.SelectBest;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import pl.pielat.algorithm.GarridoRiff;
import pl.pielat.util.solutionSerialization.AugeratFormatSolutionSerializer;
import pl.pielat.util.problemParsing.Tsplib95FileReader;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Random;

public abstract class FileBasedTest
{
    private AugeratFormatSolutionSerializer serializer = new AugeratFormatSolutionSerializer();

    private void serializeSolution(String outputPath, VehicleRoutingProblemSolution solution)
    {
        try {
            PrintWriter writer = new PrintWriter(outputPath);
            serializer.serialize(solution, writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected double runTestFromFile(String inputDir, String fileName, String solutionDir, int iterations)
    {
        VehicleRoutingProblem vrp = null;
        try
        {
            vrp = new Tsplib95FileReader().parse(inputDir + fileName + ".vrp");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        VehicleRoutingAlgorithm vra = new GarridoRiff().createAlgorithm(vrp, false, false);
        vra.setMaxIterations(iterations);

        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
        VehicleRoutingProblemSolution solution = new SelectBest().selectSolution(solutions);
        serializeSolution(solutionDir + fileName + ".vrp", solution);

        vra = Jsprit.createAlgorithm(vrp);
        vra.setMaxIterations(iterations);

        solutions = vra.searchSolutions();
        solution = new SelectBest().selectSolution(solutions);
        serializeSolution(solutionDir + fileName + "_jsprit.vrp", solution);

        return solution.getCost();
    }

    protected void runAll(String inputDir, String resultPath, int iterations)
    {
        try
        {
            PrintWriter writer = new PrintWriter(resultPath);

            int i = 0;
            File[] vrpFiles = new File(inputDir).listFiles();
            for (File f : vrpFiles)
            {
                writer.write(f.getName());
                writer.write(";");

                VehicleRoutingProblem vrp = new Tsplib95FileReader().parse(f.getPath());

                VehicleRoutingAlgorithm vra = new GarridoRiff().createAlgorithm(vrp, false, false);
                vra.setMaxIterations(iterations);

                Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
                VehicleRoutingProblemSolution solution = new SelectBest().selectSolution(solutions);
                writer.write(Double.toString(solution.getCost()));
                writer.write(";");

                vra = Jsprit.createAlgorithm(vrp);
                vra.setMaxIterations(iterations);

                solutions = vra.searchSolutions();
                solution = new SelectBest().selectSolution(solutions);
                writer.write(Double.toString(solution.getCost()));
                writer.write("\n");
            }

            writer.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    protected void runAllSeveralTimes(String problemDir, String resultPath, int algorithmIters, int runsPerProblem)
    {
        try
        {
            PrintWriter writer = new PrintWriter(resultPath);

            File[] vrpFiles = new File(problemDir).listFiles();
            for (File f : vrpFiles)
            {
                writer.print(f.getName());

                VehicleRoutingProblem vrp = new Tsplib95FileReader().parse(f.getPath());

                writer.print(";mgr");
                for (int i = 0; i < runsPerProblem; i++)
                {
                    VehicleRoutingAlgorithm vra = new GarridoRiff().createAlgorithm(vrp, false, false);
                    vra.setMaxIterations(algorithmIters);
                    Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
                    VehicleRoutingProblemSolution solution = new SelectBest().selectSolution(solutions);

                    writer.print(';');
                    writer.print(solution.getCost());

                    System.out.println(f.getName() + " " + (i + 1) + "/" + runsPerProblem);
                }

                writer.print(";jsprit");
                for (int i = 0; i < runsPerProblem; i++)
                {
                    VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp).setRandom(new Random()).buildAlgorithm();
                    vra.setMaxIterations(algorithmIters);
                    Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
                    VehicleRoutingProblemSolution solution = new SelectBest().selectSolution(solutions);

                    writer.print(';');
                    writer.print(solution.getCost());

                    System.out.println(f.getName() + " " + (i + 1) + "/" + runsPerProblem);
                }

                System.out.println(f.getName() + " done");
                writer.println();
                writer.flush();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
