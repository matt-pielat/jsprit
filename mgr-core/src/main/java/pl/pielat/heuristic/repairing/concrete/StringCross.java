package pl.pielat.heuristic.repairing.concrete;

import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import pl.pielat.algorithm.MgrRoute;
import pl.pielat.heuristic.repairing.RepairingHeuristic;

import java.util.List;

public class StringCross extends RepairingHeuristic
{
    public StringCross(VehicleRoutingProblem vrp)
    {
        super(vrp);
    }

    @Override
    public void improveRoutes(List<MgrRoute> routes)
    {
        while (findAnyImprovementCrossing(routes));
    }

    private boolean findAnyImprovementCrossing(List<MgrRoute> routes)
    {
        for (int i = 1; i < routes.size(); i++)
        {
            MgrRoute alpha = routes.get(i);
            for (int j = 0; j < i; j++)
            {
                MgrRoute beta = routes.get(j);
                if (findImprovementCrossing(alpha, beta))
                    return true;
            }
        }
        return false;
    }

    private boolean findImprovementCrossing(MgrRoute alpha, MgrRoute beta)
    {
        double currentCost = getDistance(alpha) + getDistance(beta);

        MgrRoute alpha1 = new MgrRoute(alpha);
        MgrRoute alpha2 = new MgrRoute();

        for (int i = 1; i < alpha.length(); i++)
        {
            alpha2.addToFront(alpha1.removeLast());

            int demandA1 = getDemand(alpha1);
            int demandA2 = getDemand(alpha2);

            MgrRoute beta1 = new MgrRoute(beta);
            MgrRoute beta2 = new MgrRoute();

            for (int j = 1; j < beta.length(); j++)
            {
                beta2.addToFront(beta1.removeLast());

                int demandB1 = getDemand(beta1);
                int demandB2 = getDemand(beta2);

                if (demandA1 + demandB2 <= getVehicleCapacity() &&
                    demandB1 + demandA2 <= getVehicleCapacity())
                {
                    MgrRoute gamma = makeCrossRoute(alpha1, beta2);
                    MgrRoute delta = makeCrossRoute(beta1, alpha2);

                    if (getDistance(gamma) + getDistance(delta) + EPSILON < currentCost)
                    {
                        alpha.replace(gamma);
                        beta.replace(delta);
                        return true;
                    }
                }

                if (demandA1 + demandB1 <= getVehicleCapacity() &&
                    demandB2 + demandA2 <= getVehicleCapacity())
                {
                    MgrRoute gamma = makeCrossRoute(alpha1, makeReversedCopy(beta1));
                    MgrRoute delta = makeCrossRoute(makeReversedCopy(beta2), alpha2);

                    if (getDistance(gamma) + getDistance(delta) + EPSILON < currentCost)
                    {
                        alpha.replace(gamma);
                        beta.replace(delta);
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private static MgrRoute makeReversedCopy(MgrRoute route)
    {
        MgrRoute result = new MgrRoute(route);
        result.reverse();
        return result;
    }

    private MgrRoute makeCrossRoute(MgrRoute front, MgrRoute end)
    {
        MgrRoute result = new MgrRoute(front.length() + end.length());
        result.addToEnd(front);
        result.addToEnd(end);

        reverseIfDistanceIsSmaller(result);
        return result;
    }
}
