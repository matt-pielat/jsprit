package pl.pielat.heuristicNew.constructive.concrete;

import pl.pielat.heuristicNew.Job;
import pl.pielat.heuristicNew.Place;
import pl.pielat.heuristicNew.Route;
import pl.pielat.heuristicNew.constructive.ConstructiveHeuristic;

import java.util.ArrayList;

public class MoleJamesonHeuristic extends ConstructiveHeuristic
{
    private class InsertInfo
    {
        int indexToAddAt;
        Double saving = Double.NEGATIVE_INFINITY;
        Double strain;
        Job job;
    }

    protected MoleJamesonHeuristic(ProblemInfo info)
    {
        super(info);

        if (timeWindows)
            throw new RuntimeException("This type of heuristic is not supported for VRPTW");
    }

    @Override
    public void insertJobs(ArrayList<Route> routes, ArrayList<Job> jobsToInsert)
    {
        ArrayList<InsertInfo> insertInfos = new ArrayList<>(jobsToInsert.size());
        for (Job job : jobsToInsert)
        {
            InsertInfo insertInfo = new InsertInfo();
            insertInfos.add(insertInfo);
        }

        for (Route route : routes)
        {
            if (insertInfos.isEmpty())
                break;

            insertJobsIntoRoute(route, insertInfos);
        }

        while (!insertInfos.isEmpty())
        {
            Route newRoute = createRoute(insertInfos.remove(insertInfos.size() - 1).job);
            routes.add(newRoute);

            if (insertInfos.isEmpty())
                break;

            insertJobsIntoRoute(newRoute, insertInfos);
        }
    }

    public void insertJobsIntoRoute(Route route, ArrayList<InsertInfo> insertInfos)
    {
        int maxDemand = vehicleCapacity - route.getDemand();

        for (InsertInfo insertInfo : insertInfos)
        {
            if (insertInfo.job.demand > maxDemand)
                continue;

            recalculateInsertInfo(route, insertInfo);
        }

        while (!insertInfos.isEmpty())
        {
            int bestIndex = -1;
            double maxSaving = Double.NEGATIVE_INFINITY;

            for (int i = 0; i < insertInfos.size(); i++)
            {
                InsertInfo insertInfo = insertInfos.get(i);

                if (insertInfo.saving <= maxSaving)
                    continue;

                bestIndex = i;
                maxSaving = insertInfo.saving;
            }

            if (bestIndex == -1)
                break;

            InsertInfo insertInfo = insertInfos.remove(bestIndex);
            route.add(insertInfo.indexToAddAt, insertInfo.job);
            maxDemand -= insertInfo.job.demand;

            int changedEdgeIndex = insertInfo.indexToAddAt;

            for (InsertInfo insertionToUpdate : insertInfos)
            {
                if (insertionToUpdate.indexToAddAt == changedEdgeIndex)
                {
                    recalculateInsertInfo(route, insertionToUpdate);
                    continue;
                }

                // There are two new possible insertion spots,
                // check if either is the new best.

                Place toUpdate = insertionToUpdate.job;
                Place precedingInserted = getJob(route, changedEdgeIndex - 1);
                Place inserted = getJob(route, changedEdgeIndex);
                Place succeedingInserted = getJob(route, changedEdgeIndex + 1);

                double strainA = calculateStrain(precedingInserted, toUpdate, inserted);
                double strainB = calculateStrain(inserted, toUpdate, succeedingInserted);

                if (strainA < strainB && strainA < insertionToUpdate.strain)
                {
                    insertionToUpdate.strain = strainA;
                    insertionToUpdate.indexToAddAt = changedEdgeIndex;
                    insertionToUpdate.saving = calculateSaving(toUpdate, strainA);
                }
                else if (strainB < strainA && strainB < insertionToUpdate.strain)
                {
                    insertionToUpdate.strain = strainB;
                    insertionToUpdate.indexToAddAt = changedEdgeIndex + 1;
                    insertionToUpdate.saving = calculateSaving(toUpdate, strainB);
                }
                else if (insertionToUpdate.indexToAddAt > changedEdgeIndex)
                {
                    // Fix indices for nodes after newly added job.
                    insertionToUpdate.indexToAddAt += 1;
                }
            }
        }
    }

    private Place getJob(Route route, int index)
    {
        if (index == -1 || index == route.length())
            return depot;
        return route.getFromStart(index);
    }

    private void recalculateInsertInfo(Route route, InsertInfo insertInfo)
    {
        Job job = insertInfo.job;
        Place prev = route.getLast();
        Place next = depot;

        double minStrain = calculateStrain(prev, job, next);
        int bestIndex = route.length();

        if (route.length() != 1 || transportAsymmetry)
        {
            prev = depot;
            for (int i = 0; i < route.length(); i++)
            {
                next = route.getFromStart(i);
                double strain = calculateStrain(prev, job, next);
                if (strain < minStrain)
                {
                    minStrain = strain;
                    bestIndex = i;
                }
                prev = next;
            }
        }

        insertInfo.strain = minStrain;
        insertInfo.indexToAddAt = bestIndex;
        insertInfo.saving = calculateSaving(job, minStrain);
    }

    private double calculateStrain(Place prev, Place current, Place next)
    {
        return getCost(prev, current) + getCost(current, next) - getCost(prev, next);
    }

    private double calculateSaving(Place unassignedJob, double strain)
    {
        if (transportAsymmetry)
            return getCost(depot, unassignedJob) + getCost(unassignedJob, depot) - strain;
        else
            return 2 * getCost(depot, unassignedJob) - strain;
    }
}
