package pl.pielat.algorithm;

import pl.pielat.heuristic.Job;
import pl.pielat.heuristic.Route;
import pl.pielat.heuristic.constructive.ConstructiveHeuristic;
import pl.pielat.heuristic.ordering.OrderingHeuristic;
import pl.pielat.heuristic.repairing.RepairingHeuristic;
import pl.pielat.util.Diagnostics;

import java.util.ArrayList;
import java.util.List;

public class Chromosome extends ArrayList<Gene>
{
    public Chromosome()
    {
    }

    public Chromosome(Chromosome other)
    {
        super(other.size());
        for (Gene otherGene : other)
            add(new Gene(otherGene));
    }

    public int getJobsToInsertCount()
    {
        int result = 0;
        for (Gene g : this)
            result += g.jobsToInsert;
        return result;
    }

    public void removeRange(int fromIndex, int toIndex)
    {
        super.removeRange(fromIndex, toIndex);
    }

    public ArrayList<Route> calculateSolution(ProblemInfo problemInfo)
    {
        ArrayList<Job> unassignedJobs = new ArrayList<>(problemInfo.jobs);
        ArrayList<Route> routes = new ArrayList<>();

        for (Gene gene: this)
        {
            long ohStart, chStart, ihStart, ihEnd;

            ohStart = System.nanoTime();
            OrderingHeuristic oh = gene.orderingHeuristic;
            oh.orderJobs(unassignedJobs);

            chStart = System.nanoTime();
            ConstructiveHeuristic ch = gene.constructiveHeuristic;
            List<Job> jobsToInsert = unassignedJobs.subList(0, gene.jobsToInsert);
            ch.insertJobs(routes, new ArrayList<>(jobsToInsert));
            jobsToInsert.clear();

            ihStart = System.nanoTime();
            RepairingHeuristic ih = gene.improvementHeuristic;
            ih.improveRoutes(routes);

            ihEnd = System.nanoTime();
            if (Diagnostics.Enabled)
            {
                Diagnostics.Logger.log(
                    "[OH]%s: %d ms", oh.getClass().getSimpleName(), (chStart-ohStart)/1000000L);
                Diagnostics.Logger.log(
                    "[CH]%s: %d ms", ch.getClass().getSimpleName(), (ihStart-chStart)/1000000L);
                Diagnostics.Logger.log(
                    "[IH]%s: %d ms", ih.getClass().getSimpleName(), (ihEnd-ihStart)/1000000L);
            }
        }

        return routes;
    }
}
