package pl.pielat.util;

import com.graphhopper.jsprit.analysis.toolbox.Plotter;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;

public class PlotterHelper
{
    private void plotSolution(VehicleRoutingProblem vrp, VehicleRoutingProblemSolution solution, String outputPath, String plotTitle)
    {
        Plotter plotter = new Plotter(vrp,solution);

        double minX, minY, maxX, maxY;
        minX = minY = Double.POSITIVE_INFINITY;
        maxX = maxY = Double.NEGATIVE_INFINITY;
        for (Location location : vrp.getAllLocations())
        {
            double x = location.getCoordinate().getX();
            double y = location.getCoordinate().getY();

            if (x < minX)
                minX = x;
            if (x > maxX)
                maxX = x;
            if (y < minY)
                minY = y;
            if (y > maxY)
                maxY = y;
        }

        double width = maxX - minX;
        double height = maxY - minY;

        if (width > height)
        {
            minY = minY + (maxY - minY) / 2 - width / 2;
            maxY = minY + width;
        }
        else
        {
            minX = minX + (maxX - minX) / 2 - height / 2;
            maxX = minX + height;
        }

        plotter.setBoundingBox(minX, minY, maxX, maxY);
        plotter.plot(outputPath,plotTitle);
    }
}
