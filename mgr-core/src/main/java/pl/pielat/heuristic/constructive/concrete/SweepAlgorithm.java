package pl.pielat.heuristic.constructive.concrete;

import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Delivery;
import pl.pielat.heuristic.constructive.ConstructiveHeuristic;
import pl.pielat.algorithm.MgrRoute;
import pl.pielat.util.RadialJobComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SweepAlgorithm extends ConstructiveHeuristic
{
    private RadialJobComparator _comparator;


    public SweepAlgorithm(VehicleRoutingProblem vrp)
    {
        super(vrp);
        _comparator = new RadialJobComparator(getDepotLocation().getCoordinate());
    }

    @Override
    public void insertJobs(List<MgrRoute> routes, List<Delivery> jobsToInsert)
    {
        List<Delivery> jobsLeft = new ArrayList<>(jobsToInsert);

        Collections.sort(jobsLeft, _comparator);

        while (jobsLeft.size() > 0)
        {
            List<Delivery> newRoute = new ArrayList<>();
            int demand = 0;
            while (jobsLeft.size() > 0)
            {
                Delivery last = jobsLeft.get(jobsLeft.size() - 1);

                demand += getDemand(last);
                if (demand > getVehicleCapacity())
                    break;

                jobsLeft.remove(jobsLeft.size() - 1);
                newRoute.add(last);
            }
            routes.add(solveTsp(newRoute));
        }

    }

    private MgrRoute solveTsp(List<Delivery> jobs)
    {
        //TODO
        return new MgrRoute(jobs);
    }
}
