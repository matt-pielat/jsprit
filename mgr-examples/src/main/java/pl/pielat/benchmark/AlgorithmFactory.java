package pl.pielat.benchmark;

import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.termination.TimeTermination;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;

abstract class AlgorithmFactory
{
    private long timeThresholdInMs = -1;

    void setTimeThreshold(long timeThresholdInMs)
    {
        this.timeThresholdInMs = timeThresholdInMs;
    }

    public VehicleRoutingAlgorithm build(VehicleRoutingProblem vrp)
    {
        assert timeThresholdInMs > 0;

        VehicleRoutingAlgorithm vra = createAlgorithm(vrp);

        vra.setPrematureAlgorithmTermination(new TimeTermination(timeThresholdInMs));

        return vra;
    }

    protected abstract VehicleRoutingAlgorithm createAlgorithm(VehicleRoutingProblem vrp);
}
