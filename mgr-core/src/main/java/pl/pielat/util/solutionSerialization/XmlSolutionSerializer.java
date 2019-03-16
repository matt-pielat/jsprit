package pl.pielat.util.solutionSerialization;

import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.DeliverService;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.thoughtworks.xstream.XStream;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class XmlSolutionSerializer implements VrpSolutionSerializer
{
    private static class Solution
    {
        public double cost;
        public long millisecondsElapsed;
        public int iterationCount;
        public int routeCount;
        public List routes;
    }

    private static class Route
    {
        public List nodes;
    }

    private final XStream xStream = new XStream();

    public XmlSolutionSerializer()
    {
        xStream.alias("solution", Solution.class);
        xStream.alias("route", Route.class);
        xStream.alias("node", String.class);
        xStream.addImplicitCollection(Route.class, "nodes");
    }

    @Override
    public void serialize(VehicleRoutingProblemSolution solution, long millisecondsElapsed, int iterationCount, PrintWriter writer)
    {
        Solution s = new Solution();
        s.cost = solution.getCost();
        s.millisecondsElapsed = millisecondsElapsed;
        s.iterationCount = iterationCount;

        VehicleRoute[] routes = solution.getRoutes().toArray(new VehicleRoute[0]);
        s.routeCount = routes.length;
        s.routes = new ArrayList(routes.length);

        for (int i = 0; i < routes.length; i++)
        {
            Route route = new Route();

            List<TourActivity> jobs = routes[i].getTourActivities().getActivities();

            route.nodes = new ArrayList(jobs.size());

            for (TourActivity job : jobs)
            {
                if (job instanceof DeliverService)
                {
                    route.nodes.add(job.getLocation().getId());
                }
            }
            s.routes.add(route);
        }

        writer.write(xStream.toXML(s));
    }
}
