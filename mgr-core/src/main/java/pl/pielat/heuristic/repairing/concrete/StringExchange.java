package pl.pielat.heuristic.repairing.concrete;

import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import pl.pielat.algorithm.MgrRoute;
import pl.pielat.heuristic.repairing.RepairingHeuristic;

import java.util.List;

public class StringExchange extends RepairingHeuristic
{
    public StringExchange(VehicleRoutingProblem vrp)
    {
        super(vrp);
    }

    @Override
    public void improveRoutes(List<MgrRoute> routes)
    {
        while (optimize(routes));
    }

    private boolean optimize(List<MgrRoute> routes)
    {
        for (int a = 1; a < routes.size(); a++)
        {
            MgrRoute alpha = routes.get(a);
            for (int b = 0; b < a; b++)
            {
                MgrRoute beta = routes.get(b);

                if (performExchange(alpha, beta, 2, 2) ||
                    performExchange(alpha, beta, 2, 1) ||
                    performExchange(alpha, beta, 1, 2) ||
                    performExchange(alpha, beta, 1, 1))
                {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean performExchange(MgrRoute alpha, MgrRoute beta, int kA, int kB)
    {
        double costToBeat = getDistance(alpha) + getDistance(beta);

        for (int i = 0; i < alpha.length() - kA + 1; i++)
        {
            for (int j = 0; j < beta.length() - kB + 1; j++)
            {
                MgrRoute gamma = new MgrRoute(alpha);
                MgrRoute subGamma = gamma.removeSubroute(i, kA);

                MgrRoute delta = new MgrRoute(beta);
                MgrRoute subDelta = delta.removeSubroute(j, kB);

                if (getDemand(gamma) + getDemand(subDelta) > getVehicleCapacity() ||
                    getDemand(delta) + getDemand(subGamma) > getVehicleCapacity())
                {
                    continue;
                }

                gamma = addToRouteAt(gamma, subDelta, i);
                delta = addToRouteAt(delta, subGamma, j);

                double gammaDist = getDistance(gamma);
                double deltaDist = getDistance(delta);
                if (gammaDist + deltaDist + EPSILON < costToBeat)
                {
                    alpha.replace(gamma);
                    beta.replace(delta);
                    return true;
                }
            }
        }
        return false;
    }

    private MgrRoute addToRouteAt(MgrRoute route, MgrRoute insert, int atIdx)
    {
        MgrRoute alpha = new MgrRoute(route);
        alpha.addAt(insert, atIdx);

        insert.reverse();
        MgrRoute beta = new MgrRoute(route);
        beta.addAt(insert, atIdx);

        double alphaDist = reverseIfDistanceIsSmaller(alpha);
        double betaDist = reverseIfDistanceIsSmaller(beta);
        return alphaDist < betaDist ? alpha : beta;
    }
}
