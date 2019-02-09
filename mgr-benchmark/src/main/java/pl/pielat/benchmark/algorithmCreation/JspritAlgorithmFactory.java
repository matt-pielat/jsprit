package pl.pielat.benchmark.algorithmCreation;

import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import pl.pielat.algorithm.ExtendedProblemDefinition;
import pl.pielat.algorithm.ObjectiveFunction;

import java.util.Random;

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
        Jsprit.Builder builder = Jsprit.Builder.newInstance(vrp.vrp);

        if (vrp.timeWindows)
        {
            builder.setObjectiveFunction(new ObjectiveFunction(vrp, true));
        }

        builder.setRandom(new Random());

        return builder.buildAlgorithm();
    }
}
