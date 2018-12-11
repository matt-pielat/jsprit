package pl.pielat.benchmark.algorithmCreation;

import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.termination.TimeTermination;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import pl.pielat.algorithm.ExtendedProblemDefinition;

public abstract class AlgorithmFactory
{
    private long timeThresholdInMs = -1;

    public void setTimeThreshold(long timeThresholdInMs)
    {
        this.timeThresholdInMs = timeThresholdInMs;
    }

    public VehicleRoutingAlgorithm build(ExtendedProblemDefinition vrp)
    {
        assert timeThresholdInMs > 0;

        VehicleRoutingAlgorithm vra = createAlgorithm(vrp);

        vra.setPrematureAlgorithmTermination(new TimeTermination(timeThresholdInMs));

        return vra;
    }

    public abstract String getSerializableAlgorithmId();

    protected abstract VehicleRoutingAlgorithm createAlgorithm(ExtendedProblemDefinition vrp);
}
