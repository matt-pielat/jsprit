package pl.pielat.algorithm.heuristic;

import org.junit.Assert;
import pl.pielat.algorithm.ProblemInfo;
import pl.pielat.heuristic.Job;
import pl.pielat.heuristic.Route;

import java.util.Collection;

public class HeuristicsTestsBase
{
    protected void checkSolutionConsistency(Collection<Route> routes, ProblemInfo problemInfo)
    {
        int jobCounter = 0;
        int jobCount = problemInfo.jobs.size();

        boolean[] presentJobs = new boolean[jobCount];
        for (Route route : routes)
        {
            Assert.assertTrue(route.length() > 0);
            Assert.assertTrue(route.getDemand() <= problemInfo.vehicleCapacity);

            for (int i = 0; i < route.length(); i++)
            {
                Job job = route.getFromStart(i);

                Assert.assertFalse(presentJobs[job.id]);
                presentJobs[job.id] = true;

                jobCounter++;
            }
        }

        Assert.assertEquals(jobCounter, jobCount);
    }

}
