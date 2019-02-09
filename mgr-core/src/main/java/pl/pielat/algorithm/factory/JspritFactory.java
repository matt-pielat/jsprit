package pl.pielat.algorithm.factory;

import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import pl.pielat.algorithm.ExtendedProblemDefinition;
import pl.pielat.algorithm.ObjectiveFunction;

import java.util.Random;

public class JspritFactory extends AlgorithmFactory
{
    @Override
    protected VehicleRoutingAlgorithm internalBuild(ExtendedProblemDefinition epd)
    {
        Jsprit.Builder builder = Jsprit.Builder.newInstance(epd.vrp);

        if (epd.timeWindows)
        {
            builder.setObjectiveFunction(new ObjectiveFunction(epd, true));
        }

        builder.setRandom(new Random());

        return builder.buildAlgorithm();
    }

    @Override
    public String getSerializableAlgorithmId()
    {
        return "jsprit";
    }
}
