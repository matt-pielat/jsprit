package pl.pielat.algorithm.hardcoded;

import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.selector.SelectBest;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import org.junit.Assert;
import pl.pielat.util.solutionSerialization.SimpleSolutionSerializer;
import pl.pielat.util.solutionSerialization.VrpSolutionSerializer;

import java.io.PrintWriter;
import java.util.Collection;

public class SimpleTestsBase
{
    private final VrpSolutionSerializer solutionSerializer = new SimpleSolutionSerializer();
    private final PrintWriter outputWriter = new PrintWriter(System.out);

    protected class VrpAssertion
    {
        private final VehicleRoutingProblemSolution[] solutions;

        private VrpAssertion(VehicleRoutingProblemSolution runSolution)
        {
            this(new VehicleRoutingProblemSolution[]{runSolution});
        }

        private VrpAssertion(VehicleRoutingProblemSolution[] runSolutions)
        {
            solutions = runSolutions;

            for (int i = 0; i < runSolutions.length; i++) //TODO limit output
            {
                outputWriter.printf("Run #%d:", i + 1);
                outputWriter.println();

                solutionSerializer.serialize(runSolutions[i], 0, outputWriter);

                outputWriter.println();
                outputWriter.println();
                outputWriter.flush();
            }
        }

        public VrpAssertion eachRunHasNoUnassignedJobs()
        {
            for (VehicleRoutingProblemSolution solution : solutions)
                Assert.assertEquals(0, solution.getUnassignedJobs().size());
            return this;
        }

        public VrpAssertion eachCostIsLesserThan(double costBound)
        {
            for (VehicleRoutingProblemSolution solution : solutions)
            {
                double cost = solution.getCost();
                Assert.assertTrue(cost < costBound);
            }
            return this;
        }

        public VrpAssertion eachCostIsGreaterThan(double costBound)
        {
            for (VehicleRoutingProblemSolution solution : solutions)
            {
                double cost = solution.getCost();
                Assert.assertTrue(cost > costBound);
            }
            return this;
        }

        public VrpAssertion eachCostIsEqualTo(double costBound, double epsilon)
        {
            eachCostIsLesserThan(costBound + epsilon);
            eachCostIsGreaterThan(costBound - epsilon);
            return this;
        }

        public VrpAssertion eachRouteCountIsEqualTo(int routeCount)
        {
            for (VehicleRoutingProblemSolution solution : solutions)
            {
                int actualRouteCount = solution.getRoutes().size();
                Assert.assertEquals(routeCount, actualRouteCount);
            }
            return this;
        }
    }

    protected VrpAssertion runOnce(VehicleRoutingAlgorithm vra)
    {
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
        VehicleRoutingProblemSolution bestSolution = new SelectBest().selectSolution(solutions);

        return new VrpAssertion(bestSolution);
    }

    protected VrpAssertion runMultipleTimes(VehicleRoutingAlgorithm vra, int runs)
    {
        if (runs < 0)
        {
            Assert.fail("Negative number of runs");
            return null;
        }

        VehicleRoutingProblemSolution[] solutions = new VehicleRoutingProblemSolution[runs];
        for (int i = 0; i < runs; i++)
        {
            Collection<VehicleRoutingProblemSolution> foundSolutions = vra.searchSolutions();
            solutions[i] = new SelectBest().selectSolution(foundSolutions);
        }

        return new VrpAssertion(solutions);
    }
}
