package pl.pielat.algorithm.factory;

import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.termination.TimeTermination;
import pl.pielat.algorithm.ExtendedProblemDefinition;

public abstract class AlgorithmFactory
{
    private int maxIterations = Integer.MAX_VALUE;
    private long timeLimitInMs = -1;

    public void setTimeLimit(long milliseconds)
    {
        timeLimitInMs = milliseconds;
    }

    public void setMaxIterations(int iterations)
    {
        maxIterations = iterations;
    }

    public VehicleRoutingAlgorithm build(ExtendedProblemDefinition epd)
    {
        VehicleRoutingAlgorithm vra = internalBuild(epd);

        if (timeLimitInMs > 0)
        {
            TimeTermination timeTermination = new TimeTermination(timeLimitInMs);
            vra.setPrematureAlgorithmTermination(timeTermination);
            vra.addListener(timeTermination);
        }

        vra.setMaxIterations(maxIterations);

        return vra;
    }

    protected abstract VehicleRoutingAlgorithm internalBuild(ExtendedProblemDefinition epd);

    public abstract String getSerializableAlgorithmId();
}
