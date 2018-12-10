package pl.pielat.algorithm;

import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;

public class ExtendedProblemDefinition
{
    public final String id;
    public final VehicleRoutingProblem vrp;
    public final boolean timeWindows;
    public final boolean transportAsymmetry;

    public ExtendedProblemDefinition(String id, VehicleRoutingProblem vrp,
                                     boolean timeWindows, boolean transportAsymmetry)
    {
        this.id = id;
        this.vrp = vrp;
        this.timeWindows = timeWindows;
        this.transportAsymmetry = transportAsymmetry;
    }
}
