package pl.pielat.benchmark.algorithmCreation;

import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.termination.TimeTermination;
import pl.pielat.algorithm.ExtendedProblemDefinition;

public abstract class AlgorithmFactory
{
    private long timeThresholdInMs = -1;
    private int maxIterations = Integer.MAX_VALUE;

    public void setTimeThreshold(long timeThresholdInMs)
    {
        this.timeThresholdInMs = timeThresholdInMs;
    }

    public void setMaxIterations(int maxIterations)
    {
        this.maxIterations = maxIterations;
    }

    public VehicleRoutingAlgorithm build(ExtendedProblemDefinition vrp)
    {
        assert timeThresholdInMs > 0;

        VehicleRoutingAlgorithm vra = createAlgorithm(vrp);

        TimeTermination timeTermination = new TimeTermination(timeThresholdInMs);
        vra.setPrematureAlgorithmTermination(timeTermination);
        vra.addListener(timeTermination);

        vra.setMaxIterations(maxIterations);

        return vra;
    }

    public abstract String getSerializableAlgorithmId();

    protected abstract VehicleRoutingAlgorithm createAlgorithm(ExtendedProblemDefinition vrp);
}
