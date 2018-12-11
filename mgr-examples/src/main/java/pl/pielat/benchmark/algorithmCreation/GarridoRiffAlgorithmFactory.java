package pl.pielat.benchmark.algorithmCreation;

import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import pl.pielat.algorithm.ExtendedProblemDefinition;
import pl.pielat.algorithm.GarridoRiff;

public class GarridoRiffAlgorithmFactory extends AlgorithmFactory
{
    @Override
    public VehicleRoutingAlgorithm createAlgorithm(ExtendedProblemDefinition vrp)
    {
        return new GarridoRiff().createAlgorithm(vrp.vrp, vrp.transportAsymmetry, vrp.timeWindows);
    }
}
