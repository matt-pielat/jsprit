package pl.pielat.util.solutionSerialization;

import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import pl.pielat.util.metadata.AlgorithmRunMetadata;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.Locale;

public class AugeratFormatSolutionSerializer implements VrpSolutionSerializer
{
    @Override
    public void serialize(VehicleRoutingProblemSolution solution, AlgorithmRunMetadata metadata, PrintWriter writer)
    {
        VehicleRoute[] routes = solution.getRoutes().toArray(new VehicleRoute[0]);

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < routes.length; i++)
        {
            builder.append("Route #");
            builder.append(i + 1);
            builder.append(':');

            Collection<Job> jobs = routes[i].getTourActivities().getJobs();
            for (Job job : jobs)
            {
                builder.append(' ');
                builder.append(job.getId());
            }
            builder.append(System.lineSeparator());
        }

        builder.append("cost ");
        builder.append(String.format(Locale.getDefault(),"%.2f", solution.getCost()));
        builder.append(System.lineSeparator());

        writer.write(builder.toString());
    }
}
