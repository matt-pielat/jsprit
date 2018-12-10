package pl.pielat.benchmark;

import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import pl.pielat.algorithm.ExtendedProblemDefinition;

public class JspritAlgorithmFactory extends AlgorithmFactory
{
    @Override
    public VehicleRoutingAlgorithm createAlgorithm(ExtendedProblemDefinition vrp)
    {
        return Jsprit.Builder.newInstance(vrp.vrp).buildAlgorithm();
    }
}
