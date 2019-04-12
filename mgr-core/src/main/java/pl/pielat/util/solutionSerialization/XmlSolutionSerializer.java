package pl.pielat.util.solutionSerialization;

import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.DeliverService;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.extended.ToAttributedValueConverter;
import pl.pielat.util.metadata.AlgorithmRunMetadata;
import pl.pielat.util.metadata.EhDvrpStatistics;
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
        public List<Route> routes;
        public List<IntermediateCost> intermediateCosts;
        public HeuristicUsageStats heuristicUsages;
        public List<ChromosomeSizeCount> chromosomeCountBySize;
    }

    private static class Route
    {
        public List nodes;
    }

    private static class ChromosomeSizeCount
    {
        public int size;
        public int count;
    }

    private static class HeuristicUsageStats
    {
        public List<HeuristicUsages> orderingHeuristicUsages;
        public List<HeuristicUsages> constructiveHeuristicUsages;
        public List<HeuristicUsages> repairingHeuristicUsages;
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

//        xStream.alias("ehDvrpStatistics", EhDvrpStatistics.class);

        xStream.alias("csc", ChromosomeSizeCount.class);
        xStream.registerConverter(new ToAttributedValueConverter(
            ChromosomeSizeCount.class,
            xStream.getMapper(),
            xStream.getReflectionProvider(),
            xStream.getConverterLookup(),
            "count"));

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

        EhDvrpStatistics ehdvrp = metadata.ehDvrpStatistics;
        if (ehdvrp != null)
        {
            s.heuristicUsages = new HeuristicUsageStats();
            s.heuristicUsages.orderingHeuristicUsages = ehdvrp.orderingHeuristicUsages;
            s.heuristicUsages.constructiveHeuristicUsages = ehdvrp.constructiveHeuristicUsages;
            s.heuristicUsages.repairingHeuristicUsages = ehdvrp.repairingHeuristicUsages;

            s.chromosomeCountBySize = new ArrayList<>(ehdvrp.chromosomeSizes.length - 1);
            for (int i = 1; i < ehdvrp.chromosomeSizes.length; i++)
            {
                ChromosomeSizeCount csc = new ChromosomeSizeCount();
                csc.size = i;
                csc.count = ehdvrp.chromosomeSizes[i];
                s.chromosomeCountBySize.add(csc);
            }
        }

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
