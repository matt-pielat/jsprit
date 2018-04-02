package pl.pielat.benchmark;

import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import pl.pielat.algorithm.GarridoRiff;

public class GarridoRiffAlgorithmFactory extends AlgorithmFactory
{
    @Override
    public VehicleRoutingAlgorithm createAlgorithm(VehicleRoutingProblem vrp)
    {
        return new GarridoRiff().createAlgorithm(vrp);
    }
}
