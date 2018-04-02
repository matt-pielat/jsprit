package pl.pielat.heuristic.repairing.concrete;

import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import pl.pielat.algorithm.MgrRoute;
import pl.pielat.heuristic.repairing.RepairingHeuristic;

import java.util.List;

public class LambdaOptTwo extends RepairingHeuristic
{
    public LambdaOptTwo(VehicleRoutingProblem vrp)
    {
        super(vrp);
    }

    @Override
    public void improveRoutes(List<MgrRoute> routes)
    {
        for (MgrRoute r : routes)
        {
            while (improveRoute(r));
        }
    }

    private boolean improveRoute(MgrRoute route)
    {
        double oldDistance = getDistance(route);
        for (int x = 1; x < route.length() - 1; x++)
        {
            for (int y = x + 1; y < route.length(); y++)
            {
                MgrRoute newRoute = twoOptSwap(route, x, y);
                double newDistance = reverseIfDistanceIsSmaller(newRoute);
                if (newDistance + EPSILON < oldDistance)
                {
                    route.replace(newRoute);
                    return true;
                }
            }
        }
        return false;
    }

    private MgrRoute twoOptSwap(MgrRoute route, int x, int y)
    {
        MgrRoute result = new MgrRoute(route.length());
        for (int i = 0; i < x; i++)
            result.addToEnd(route.get(i));
        for (int i = y; i >= x; i--)
            result.addToEnd(route.get(i));
        for (int i = y + 1; i < route.length(); i++)
            result.addToEnd(route.get(i));
        return result;
    }
}
