package pl.pielat.heuristic.repairing.concrete;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import pl.pielat.algorithm.MgrRoute;
import pl.pielat.heuristic.repairing.RepairingHeuristic;

import java.util.List;

public class LambdaOptThree extends RepairingHeuristic
{
    public LambdaOptThree(VehicleRoutingProblem vrp)
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
        for (int i = 0; i <= route.length() - 4; i++)
        {
            int maxK = route.length() - (i > 0 ? 0 : 1);
            for (int j = i + 2; j <= route.length() - 2; j++)
            {
                for (int k = j + 2; k <= maxK; k++)
                {
                    MgrRoute newRoute = threeOptSwap(route, oldDistance, i, j, k);
                    if (newRoute != route)
                    {
                        route.replace(newRoute);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private MgrRoute threeOptSwap(MgrRoute oldRoute, double oldDistance, int i, int j, int k)
    {
        double ijDist, jkDist, kiDist, jiDist, kjDist, ikDist;
        ijDist = jkDist = kiDist = jiDist = kjDist = ikDist = 0;

        int q;
        if (i > 0)
            kiDist += getDistance(getDepotLocation(), oldRoute.get(0).getLocation());
        for (q = 1; q < i; q++)
            kiDist += getDistance(oldRoute, q - 1, q);
        for (q = i + 1; q < j; q++)
            ijDist += getDistance(oldRoute, q - 1, q);
        for (q = j + 1; q < k; q++)
            jkDist += getDistance(oldRoute, q - 1, q);
        for (q = k + 1; q < oldRoute.length(); q++)
            kiDist += getDistance(oldRoute, q - 1, q);
        if (k < oldRoute.length())
            kiDist += getDistance(oldRoute.getLast().getLocation(), getDepotLocation());

        if (distanceIsSymmetric())
        {
            ikDist = kiDist;
            kjDist = jkDist;
            jiDist = ijDist;
        }
        else
        {
            if (i > 0)
                ikDist += getDistance(oldRoute.getFirst().getLocation(), getDepotLocation());
            for (q = 1; q < i; q++)
                ikDist += getDistance(oldRoute, q, q - 1);
            for (q = i + 1; q < j; q++)
                jiDist += getDistance(oldRoute, q, q - 1);
            for (q = j + 1; q < k; q++)
                kjDist += getDistance(oldRoute, q, q - 1);
            for (q = k + 1; q < oldRoute.length(); q++)
                ikDist += getDistance(oldRoute, q, q - 1);
            if (k < oldRoute.length())
                ikDist += getDistance(getDepotLocation(), oldRoute.getLast().getLocation());
        }

        for (int b = 7; b >= 0; b--)
        {
            boolean ijReversed = (b & 1) > 0;
            boolean jkReversed = (b & 2) > 0;
            boolean kiReversed = (b & 4) > 0;

            double subroutesDistances = 0;
            subroutesDistances += ijReversed ? jiDist : ijDist;
            subroutesDistances += jkReversed ? kjDist : jkDist;
            subroutesDistances += kiReversed ? ikDist : kiDist;

            Location fromA = ijReversed ? getEdgeVertex(oldRoute, i, true) : getEdgeVertex(oldRoute, j, false);
            Location toA = jkReversed ? getEdgeVertex(oldRoute, k, false) : getEdgeVertex(oldRoute, j, true);

            Location fromB = jkReversed ? getEdgeVertex(oldRoute, j, true) : getEdgeVertex(oldRoute, k, false);
            Location toB = kiReversed ? getEdgeVertex(oldRoute, i, false) : getEdgeVertex(oldRoute, k, true);

            Location fromC = kiReversed ? getEdgeVertex(oldRoute, k, true) : getEdgeVertex(oldRoute, i, false);
            Location toC = ijReversed ? getEdgeVertex(oldRoute, j, false) : getEdgeVertex(oldRoute, i, true);

            double newDistance = subroutesDistances + getDistance(fromA, toA) + getDistance(fromB, toB) + getDistance(fromC, toC);
            if (oldDistance > newDistance + EPSILON)
            {
                MgrRoute newRoute = new MgrRoute(oldRoute.length());
                if (kiReversed)
                {
                    for (int a = oldRoute.length() - 1; a >= k; a--)
                        newRoute.addToEnd(oldRoute.get(a));
                }
                else
                {
                    for (int a = 0; a < i; a++)
                        newRoute.addToEnd(oldRoute.get(a));
                }
                if (ijReversed)
                {
                    for (int a = j - 1; a >= i; a--)
                        newRoute.addToEnd(oldRoute.get(a));
                }
                else
                {
                    for (int a = i; a < j; a++)
                        newRoute.addToEnd(oldRoute.get(a));
                }
                if (jkReversed)
                {
                    for (int a = k - 1; a >= j; a--)
                        newRoute.addToEnd(oldRoute.get(a));
                }
                else
                {
                    for (int a = j; a < k; a++)
                        newRoute.addToEnd(oldRoute.get(a));
                }
                if (kiReversed)
                {
                    for (int a = i - 1; a >= 0; a--)
                        newRoute.addToEnd(oldRoute.get(a));
                }
                else
                {
                    for (int a = k; a < oldRoute.length(); a++)
                        newRoute.addToEnd(oldRoute.get(a));
                }
                return newRoute;
            }

            if (distanceIsSymmetric())
                continue;

            newDistance = subroutesDistances + getDistance(fromC, toA) + getDistance(fromA, toB) + getDistance(fromB, toC);
            if (oldDistance > newDistance + EPSILON)
            {
                MgrRoute newRoute = new MgrRoute(oldRoute.length());
                if (kiReversed)
                {
                    for (int a = oldRoute.length() - 1; a >= k; a--)
                        newRoute.addToEnd(oldRoute.get(a));
                }
                else
                {
                    for (int a = 0; a < i; a++)
                        newRoute.addToEnd(oldRoute.get(a));
                }
                if (jkReversed)
                {
                    for (int a = k - 1; a >= j; a--)
                        newRoute.addToEnd(oldRoute.get(a));
                }
                else
                {
                    for (int a = j; a < k; a++)
                        newRoute.addToEnd(oldRoute.get(a));
                }
                if (ijReversed)
                {
                    for (int a = j - 1; a >= i; a--)
                        newRoute.addToEnd(oldRoute.get(a));
                }
                else
                {
                    for (int a = i; a < j; a++)
                        newRoute.addToEnd(oldRoute.get(a));
                }
                if (kiReversed)
                {
                    for (int a = i - 1; a >= 0; a--)
                        newRoute.addToEnd(oldRoute.get(a));
                }
                else
                {
                    for (int a = k; a < oldRoute.length(); a++)
                        newRoute.addToEnd(oldRoute.get(a));
                }
                return newRoute;
            }
        }

        return oldRoute;
    }

    private Location getEdgeVertex(MgrRoute route, int idx, boolean afterEdge)
    {
        if (idx == 0 && !afterEdge)
            return getDepotLocation();
        else if (idx == route.length() && afterEdge)
            return getDepotLocation();

        return route.get(afterEdge ? idx : idx - 1).getLocation();
    }

    private double getDistance(MgrRoute route, int from, int to)
    {
        return getDistance(
            route.get(from).getLocation(),
            route.get(to).getLocation());
    }

}
