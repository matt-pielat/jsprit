package pl.pielat.heuristic.constructive.concrete;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Delivery;
import pl.pielat.heuristic.constructive.ConstructiveHeuristic;
import pl.pielat.algorithm.MgrRoute;

import java.util.ArrayList;
import java.util.List;

public class MoleJamesonHeuristic extends ConstructiveHeuristic
{
    private class InsertInfo
    {
        int indexToAddAt = -1;
        Double saving;
        Double strain;
        Delivery job;

        InsertInfo(Delivery job)
        {
            this.job = job;
        }
    }

    public MoleJamesonHeuristic(VehicleRoutingProblem vrp)
    {
        super(vrp);
    }

    @Override
    public void insertJobs(List<MgrRoute> routes, List<Delivery> jobsToInsert)
    {
        List<InsertInfo> insertInfos = new ArrayList<>(jobsToInsert.size());
        for (Delivery job : jobsToInsert)
            insertInfos.add(new InsertInfo(job));

        while (insertInfos.size() > 0)
        {
            Delivery firstJob = insertInfos.remove(0).job;
            MgrRoute emergingRoute = new MgrRoute(firstJob);

            for (InsertInfo info : insertInfos)
                recalculateInsertInfo(emergingRoute, info);

            addJobsToEmergingRoute(emergingRoute, insertInfos);
            routes.add(emergingRoute);
        }
    }

    private void addJobsToEmergingRoute(MgrRoute route, List<InsertInfo> insertInfos)
    {
        double routeDemand = getDemand(route);
        while (insertInfos.size() > 0)
        {
            int bestI = -1;
            double maxSaving = Double.NEGATIVE_INFINITY;
            for (int i = 0; i < insertInfos.size(); i++)
            {
                InsertInfo current = insertInfos.get(i);
                if (getDemand(current.job) + routeDemand > getVehicleCapacity())
                    continue;

                if (current.saving <= maxSaving)
                    continue;

                maxSaving = current.saving;
                bestI = i;
            }
            if (bestI == -1)
                return;

            //Insert job
            InsertInfo newInsertion = insertInfos.remove(bestI);
            route.addAt(newInsertion.job, newInsertion.indexToAddAt);
            routeDemand += getDemand(newInsertion.job);
            int insertedJobIdx = newInsertion.indexToAddAt;

            Location depot = getDepotLocation();

            for (InsertInfo insertionToUpdate : insertInfos)
            {
                //Recalculate altogether
                if (insertionToUpdate.indexToAddAt == insertedJobIdx)
                {
                    recalculateInsertInfo(route, insertionToUpdate);
                    continue;
                }

                //Check only the two new possible spots for insertion
                Location current = insertionToUpdate.job.getLocation();
                Location inserted = route.get(insertedJobIdx).getLocation();
                Location beforeInserted = insertedJobIdx == 0 ?
                    depot : route.get(insertedJobIdx - 1).getLocation();
                Location afterInserted = insertedJobIdx == route.length() - 1 ?
                    depot : route.get(insertedJobIdx + 1).getLocation();

                double strainA = getStrain(beforeInserted, current, inserted);
                double strainB = getStrain(inserted, current, afterInserted);

                if (strainA < strainB && strainA < insertionToUpdate.strain)
                {
                    insertionToUpdate.strain = strainA;
                    insertionToUpdate.indexToAddAt = insertedJobIdx;
                    insertionToUpdate.saving = getDistance(depot, current) + getDistance(current, depot) - strainA;
                }
                else if (strainB < strainA && strainB < insertionToUpdate.strain)
                {
                    insertionToUpdate.strain = strainB;
                    insertionToUpdate.indexToAddAt = insertedJobIdx + 1;
                    insertionToUpdate.saving = getDistance(depot, current) + getDistance(current, depot) - strainB;
                }
                else if (insertionToUpdate.indexToAddAt > insertedJobIdx)
                {
                    //Fix indices for nodes after newly added job.
                    insertionToUpdate.indexToAddAt += 1;
                }
            }
        }
    }

    private void recalculateInsertInfo(MgrRoute route, InsertInfo insertInfo)
    {
        Location depot = getDepotLocation();
        Location current = insertInfo.job.getLocation();
        Location prev = route.getLast().getLocation();
        Location next = depot;

        double minStrain = getStrain(prev, current, next);
        int bestI = route.length();

        if (route.length() != 1 || !distanceIsSymmetric())
        {
            prev = depot;
            for (int i = 0; i < route.length(); i++)
            {
                next = route.get(i).getLocation();
                double strain = getStrain(prev, current, next);
                if (strain < minStrain)
                {
                    minStrain = strain;
                    bestI = i;
                }
                prev = next;
            }
        }

        insertInfo.strain = minStrain;
        insertInfo.indexToAddAt = bestI;
        insertInfo.saving = getDistance(depot, current) + getDistance(current, depot) - minStrain;
    }

    private double getStrain(Location prev, Location current, Location next)
    {
        return getDistance(prev, current) + getDistance(current, next) - getDistance(prev, next);
    }
}
