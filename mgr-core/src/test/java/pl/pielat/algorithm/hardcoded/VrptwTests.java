package pl.pielat.algorithm.hardcoded;

import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import org.junit.Test;
import pl.pielat.algorithm.ExtendedProblemDefinition;
import pl.pielat.algorithm.GarridoRiff;
import pl.pielat.util.simpleBuilder.SimpleVrpBuilder;

public class VrptwTests extends SimpleTestsBase
{
    private GarridoRiff garridoRiff = new GarridoRiff();

    @Test
    public void moreThanEnoughTimeToServiceAll()
    {
        VehicleRoutingProblem vrp = new SimpleVrpBuilder()
            .setDepotLocation(0, 0)
            .setVehicleCapacity(100)
            .setSchedulingHorizon(0, 1000)
            .addJob(-10, -10).setDemand(10).setTimeWindow(100, 200).build()
            .addJob(10, -10).setDemand(10).setTimeWindow(300, 400).build()
            .addJob(-10, 10).setDemand(10).setTimeWindow(500, 600).build()
            .addJob(10, 10).setDemand(10).setTimeWindow(700, 800).build()
            .build();

        ExtendedProblemDefinition epd = new ExtendedProblemDefinition("1", vrp, true, false);
        VehicleRoutingAlgorithm vra = garridoRiff.createAlgorithm(epd);
        vra.setMaxIterations(100);

        runOnce(vra)
            .eachRunHasNoUnassignedJobs();
    }

    @Test
    public void twoRoutesNeededDueToSchedulingHorizon()
    {
        VehicleRoutingProblem vrp = new SimpleVrpBuilder()
            .setDepotLocation(0, 0)
            .setVehicleCapacity(40)
            .setSchedulingHorizon(0, 800)
            .addJob(0, 100).setDemand(10).setServiceTime(0).build()
            .addJob(0, 200).setDemand(10).setServiceTime(1).build()
            .addJob(0, 300).setDemand(10).setServiceTime(0).build()
            .addJob(0, 400).setDemand(10).setServiceTime(0).build()
            .build();

        ExtendedProblemDefinition epd = new ExtendedProblemDefinition("1", vrp, true, false);
        VehicleRoutingAlgorithm vra = garridoRiff.createAlgorithm(epd);
        vra.setMaxIterations(100);

        runOnce(vra)
            .eachRunHasNoUnassignedJobs()
            .eachRouteCountIsEqualTo(2);
    }

    @Test
    public void twoRoutesNeededDueToClientTimeWindows()
    {
        VehicleRoutingProblem vrp = new SimpleVrpBuilder()
            .setDepotLocation(0, 0)
            .setVehicleCapacity(40)
            .setSchedulingHorizon(0, 3000)
            .addJob(0, 100).setDemand(10).setServiceTime(10).setTimeWindow(0, 400).build()
            .addJob(0, 200).setDemand(10).setServiceTime(10).setTimeWindow(0, 400).build()
            .addJob(0, 300).setDemand(10).setServiceTime(10).setTimeWindow(0, 400).build()
            .addJob(0, 400).setDemand(10).setServiceTime(10).setTimeWindow(0, 400).build()
            .build();

        ExtendedProblemDefinition epd = new ExtendedProblemDefinition("1", vrp, true, false);
        VehicleRoutingAlgorithm vra = garridoRiff.createAlgorithm(epd);
        vra.setMaxIterations(100);

        runOnce(vra)
            .eachRunHasNoUnassignedJobs()
            .eachRouteCountIsEqualTo(2);
    }
}
