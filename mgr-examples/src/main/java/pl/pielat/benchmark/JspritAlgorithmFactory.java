package pl.pielat.benchmark;

import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;

public class JspritAlgorithmFactory extends AlgorithmFactory
{
    @Override
    public VehicleRoutingAlgorithm createAlgorithm(VehicleRoutingProblem vrp)
    {
        return Jsprit.Builder.newInstance(vrp).buildAlgorithm();
    }
}
