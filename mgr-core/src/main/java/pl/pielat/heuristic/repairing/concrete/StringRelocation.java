package pl.pielat.heuristic.repairing.concrete;

import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Delivery;
import pl.pielat.algorithm.MgrRoute;
import pl.pielat.heuristic.repairing.RepairingHeuristic;

import java.util.List;

public class StringRelocation extends RepairingHeuristic
{
    public StringRelocation(VehicleRoutingProblem vrp)
    {
        super(vrp);
    }

    @Override
    public void improveRoutes(List<MgrRoute> routes)
    {
        while (findImprovement(routes));
    }

    private boolean findImprovement(List<MgrRoute> routes)
    {
        for (int i = 0; i < routes.size(); i++)
        {
            MgrRoute alpha = routes.get(i);
            for (int j = 0; j < routes.size(); j++)
            {
                if (i == j)
                    continue;

                MgrRoute beta = routes.get(j);

                if (alpha.length() >= 2 && findImprovementK2(alpha, beta))
                {
                    if (alpha.length() == 0)
                        routes.remove(alpha);
                    return true;
                }

                if (findImprovementK1(alpha, beta))
                {
                    if (alpha.length() == 0)
                        routes.remove(alpha);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean findImprovementK1(MgrRoute alpha, MgrRoute beta)
    {
        double currentCost = getDistance(alpha) + getDistance(beta);

        for (int j = 0; j < alpha.length(); j++)
        {
            MgrRoute alphaMod = new MgrRoute(alpha);
            Delivery job = alphaMod.removeAt(j);

            for (int i = 0; i <= beta.length(); i++)
            {
                MgrRoute betaMod = new MgrRoute(beta);
                betaMod.addAt(job, i);

                if (getDemand(betaMod) > getVehicleCapacity())
                    continue;

                if (getDistance(alphaMod) + getDistance(betaMod) + EPSILON < currentCost)
                {
                    alpha.replace(alphaMod);
                    beta.replace(betaMod);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean findImprovementK2(MgrRoute alpha, MgrRoute beta)
    {
        double currentCost = getDistance(alpha) + getDistance(beta);

        for (int j = 0; j < alpha.length() - 1; j++)
        {
            MgrRoute alphaMod = new MgrRoute(alpha);
            Delivery job1 = alphaMod.removeAt(j);
            Delivery job2 = alphaMod.removeAt(j);
            double alphaModDist = reverseIfDistanceIsSmaller(alphaMod);

            for (int i = 0; i <= beta.length(); i++)
            {
                MgrRoute betaMod = new MgrRoute(beta);
                betaMod.addAt(job1, i);
                betaMod.addAt(job2, i);

                if (getDemand(betaMod) > getVehicleCapacity())
                    continue;
                double betaModDist = reverseIfDistanceIsSmaller(betaMod);

                if (alphaModDist + betaModDist + EPSILON < currentCost)
                {
                    alpha.replace(alphaMod);
                    beta.replace(betaMod);
                    return true;
                }

                // Try adding in reverse order
                betaMod = new MgrRoute(beta);
                betaMod.addAt(job2, i);
                betaMod.addAt(job1, i);
                betaModDist = reverseIfDistanceIsSmaller(betaMod);

                if (alphaModDist + betaModDist + EPSILON < currentCost)
                {
                    alpha.replace(alphaMod);
                    beta.replace(betaMod);
                    return true;
                }
            }
        }
        return false;
    }
}
