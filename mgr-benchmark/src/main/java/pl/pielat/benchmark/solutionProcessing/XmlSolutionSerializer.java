package pl.pielat.benchmark.solutionProcessing;

import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.thoughtworks.xstream.XStream;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class XmlSolutionSerializer
{
    private static class Solution
    {
        public double cost;
        public long millisecondsElapsed;
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

    public void serialize(VehicleRoutingProblemSolution solution, long millisecondsElapsed, PrintWriter writer)
    {
        Solution s = new Solution();
        s.cost = solution.getCost();
        s.millisecondsElapsed = millisecondsElapsed;

        VehicleRoute[] routes = solution.getRoutes().toArray(new VehicleRoute[0]);
        s.routes = new ArrayList(routes.length);

        for (int i = 0; i < routes.length; i++)
        {
            Route route = new Route();

            Collection<Job> jobs = routes[i].getTourActivities().getJobs();
            route.nodes = new ArrayList(jobs.size());

            for (Job job : jobs)
                route.nodes.add(job.getId());

            s.routes.add(route);
        }

        writer.write(xStream.toXML(s));
    }
}
