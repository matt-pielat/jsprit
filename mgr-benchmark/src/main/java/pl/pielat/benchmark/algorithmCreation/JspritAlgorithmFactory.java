package pl.pielat.benchmark.algorithmCreation;

import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import pl.pielat.algorithm.ExtendedProblemDefinition;

public class JspritAlgorithmFactory extends AlgorithmFactory
{
    @Override
    public String getSerializableAlgorithmId()
    {
        return "Jsprit";
    }

    @Override
    public VehicleRoutingAlgorithm createAlgorithm(ExtendedProblemDefinition vrp)
    {
        return Jsprit.Builder.newInstance(vrp.vrp).buildAlgorithm();
    }
}
