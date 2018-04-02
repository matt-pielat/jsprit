package pl.pielat.heuristic.ordering;

import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import pl.pielat.heuristic.BaseHeuristicProvider;
import pl.pielat.heuristic.ordering.concrete.OrderingByDemand;
import pl.pielat.heuristic.ordering.concrete.OrderingByDistance;
import pl.pielat.heuristic.ordering.concrete.RadialSweepOrdering;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class OrderingHeuristicProvider extends BaseHeuristicProvider<OrderingHeuristic>
{
    public OrderingHeuristicProvider(VehicleRoutingProblem vrp, Random random)
    {
        super(vrp, random);
    }

    @Override
    protected List<String> getAllIds()
    {
        List<String> ids = new ArrayList<String>(4);
        ids.add("increasingDemand");
        ids.add("decreasingDemand");
        ids.add("increasingDistance");
        ids.add("decreasingDistance");
        ids.add("sweepFarthest");
        ids.add("sweepNearest");
        return ids;
    }

    @Override
    protected OrderingHeuristic getInstanceById(String id) throws Exception
    {
        OrderingHeuristic instance;
        switch (id)
        {
            case "increasingDemand":
                instance = new OrderingByDemand(vrp, true);
                break;
            case "decreasingDemand":
                instance = new OrderingByDemand(vrp, false);
                break;
            case "increasingDistance":
                instance = new OrderingByDistance(vrp, true, true);
                break;
            case "decreasingDistance":
                instance = new OrderingByDistance(vrp, false, true);
                break;
            case "sweepFarthest":
                instance = new RadialSweepOrdering(vrp, true, false);
                break;
            case "sweepNearest":
                instance = new RadialSweepOrdering(vrp, false, false);
                break;
            default:
                throw new Exception("Unknown id.");
        }
        return instance;
    }


}
