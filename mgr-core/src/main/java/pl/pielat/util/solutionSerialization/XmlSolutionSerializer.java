package pl.pielat.util.solutionSerialization;

import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.DeliverService;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.extended.ToAttributedValueConverter;
import com.thoughtworks.xstream.converters.extended.ToStringConverter;
import pl.pielat.util.metadata.AlgorithmRunMetadata;
import pl.pielat.util.metadata.HeuristicUsageStatistics;
import pl.pielat.util.metadata.HeuristicUsages;
import pl.pielat.util.metadata.IntermediateCost;

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
        public List intermediateCosts;
        public HeuristicUsageStatistics heuristicUsages;
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

        xStream.alias("ic", IntermediateCost.class);
        xStream.useAttributeFor(IntermediateCost.class, "cost");
        xStream.useAttributeFor(IntermediateCost.class, "timeInMs");

        xStream.alias("heuristicUsages", HeuristicUsageStatistics.class);

        xStream.alias("hu", HeuristicUsages.class);
        xStream.registerConverter(new ToAttributedValueConverter(
            HeuristicUsages.class,
            xStream.getMapper(),
            xStream.getReflectionProvider(),
            xStream.getConverterLookup(),
            "usageCount"));
    }

    @Override
    public void serialize(VehicleRoutingProblemSolution solution, AlgorithmRunMetadata metadata, PrintWriter writer)
    {
        Solution s = new Solution();
        s.cost = solution.getCost();
        s.millisecondsElapsed = metadata.millisecondsElapsed;
        s.iterationCount = metadata.iterationCount;
        s.intermediateCosts = metadata.intermediateCosts;
        s.heuristicUsages = metadata.heuristicUsageStatistics;

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
