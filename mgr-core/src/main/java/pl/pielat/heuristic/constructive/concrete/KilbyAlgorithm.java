package pl.pielat.heuristic.constructive.concrete;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Delivery;
import pl.pielat.heuristic.constructive.ConstructiveHeuristic;
import pl.pielat.algorithm.MgrRoute;

import java.util.List;

public class KilbyAlgorithm extends ConstructiveHeuristic
{
    public KilbyAlgorithm(VehicleRoutingProblem vrp)
    {
        super(vrp);
    }

    @Override
    public void insertJobs(List<MgrRoute> routes, List<Delivery> jobsToInsert)
    {
        for (Delivery job : jobsToInsert)
            insertJob(routes, job);
    }

    private void insertJob(List<MgrRoute> routes, Delivery job)
    {
        Location jobLocation = job.getLocation();

        MgrRoute bestRoute = null;
        int bestPosition = -1;
        double minExtraDist = Double.POSITIVE_INFINITY;

        for (MgrRoute route : routes)
        {
            if (getDemand(route) + getDemand(job) > getVehicleCapacity())
                continue;

            Location prev = getDepotLocation();
            Location next;
            for (int i = 0; i < route.length(); i++)
            {
                next = route.get(i).getLocation();
                double extraDist = getDistance(prev, jobLocation) + getDistance(jobLocation, next) - getDistance(prev, next);
                if (extraDist < minExtraDist)
                {
                    minExtraDist = extraDist;
                    bestRoute = route;
                    bestPosition = i;
                }
                prev = next;
            }
            if (route.length() > 1 || !distanceIsSymmetric())
            {
                next = getDepotLocation();
                double extraCost = getDistance(prev, jobLocation) + getDistance(jobLocation, next) - getDistance(prev, next);
                if (extraCost < minExtraDist)
                {
                    minExtraDist = extraCost;
                    bestRoute = route;
                    bestPosition = route.length();
                }
            }
        }

        if (bestRoute == null)
        {
            routes.add(new MgrRoute(job));
            return;
        }

        bestRoute.addAt(job, bestPosition);
    }
}
