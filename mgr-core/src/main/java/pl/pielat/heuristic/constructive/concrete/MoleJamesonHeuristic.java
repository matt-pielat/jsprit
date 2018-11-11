package pl.pielat.heuristic.constructive.concrete;

import pl.pielat.algorithm.ProblemInfo;
import pl.pielat.heuristic.Job;
import pl.pielat.heuristic.Place;
import pl.pielat.heuristic.Route;
import pl.pielat.heuristic.constructive.ConstructiveHeuristic;

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

    public MoleJamesonHeuristic(ProblemInfo info)
    {
        super(info);
    }

    @Override
    public void insertJobs(ArrayList<Route> routes, ArrayList<Job> jobsToInsert)
    {
        ArrayList<InsertInfo> insertInfos = new ArrayList<>(jobsToInsert.size());
        for (Job job : jobsToInsert)
        {
            InsertInfo insertInfo = new InsertInfo();
            insertInfo.job = job;
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
                Job jobToUpdate = insertionToUpdate.job;

                if (jobToUpdate.demand > maxDemand)
                {
                    insertionToUpdate.saving = Double.NEGATIVE_INFINITY;
                    continue;
                }
                if (insertionToUpdate.indexToAddAt == changedEdgeIndex)
                {
                    recalculateInsertInfo(route, insertionToUpdate);
                    continue;
                }
                if (timeWindows && !route.canFitIntoTimeSchedule(insertionToUpdate.indexToAddAt, jobToUpdate))
                {
                    recalculateInsertInfo(route, insertionToUpdate);
                    continue;
                }

                // There are two new possible insertion spots,
                // check if either is the new best.

                Place precedingInserted = getJob(route, changedEdgeIndex - 1);
                Place inserted = getJob(route, changedEdgeIndex);
                Place succeedingInserted = getJob(route, changedEdgeIndex + 1);

                double strainBefore = calculateStrain(precedingInserted, jobToUpdate, inserted);
                double strainAfter = calculateStrain(inserted, jobToUpdate, succeedingInserted);

                boolean canInsertBefore;
                boolean canInsertAfter;
                if (timeWindows)
                {
                    canInsertBefore = strainBefore < insertionToUpdate.strain
                        && route.canFitIntoTimeSchedule(changedEdgeIndex, jobToUpdate);
                    canInsertAfter = strainBefore < insertionToUpdate.strain
                        && route.canFitIntoTimeSchedule(changedEdgeIndex + 1, jobToUpdate);

                    if (canInsertBefore && canInsertAfter)
                        canInsertBefore = strainBefore < strainAfter;
                }
                else
                {
                    canInsertBefore = strainBefore < strainAfter && strainBefore < insertionToUpdate.strain;
                    canInsertAfter = strainAfter < strainBefore && strainAfter < insertionToUpdate.strain;
                }

                if (canInsertBefore)
                {
                    insertionToUpdate.strain = strainBefore;
                    insertionToUpdate.indexToAddAt = changedEdgeIndex;
                    insertionToUpdate.saving = calculateSaving(jobToUpdate, strainBefore);
                }
                else if (canInsertAfter)
                {
                    insertionToUpdate.strain = strainAfter;
                    insertionToUpdate.indexToAddAt = changedEdgeIndex + 1;
                    insertionToUpdate.saving = calculateSaving(jobToUpdate, strainAfter);
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

        double minStrain = Double.POSITIVE_INFINITY;
        int bestIndex = -1;

        if (!timeWindows || route.canFitIntoTimeSchedule(route.length(), job))
        {
            bestIndex = route.length();
            minStrain = calculateStrain(prev, job, next);
        }

        if (route.length() != 1 || transportAsymmetry || timeWindows)
        {
            prev = depot;
            for (int i = 0; i < route.length(); i++)
            {
                if (timeWindows && !route.canFitIntoTimeSchedule(i, job))
                    continue;

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

        if (bestIndex == -1)
        {
            insertInfo.strain = minStrain;
            insertInfo.indexToAddAt = bestIndex;
            insertInfo.saving = calculateSaving(job, minStrain);
        }
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
