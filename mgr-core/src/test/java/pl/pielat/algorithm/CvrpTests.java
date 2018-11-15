package pl.pielat.algorithm;

import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import org.junit.Test;
import pl.pielat.util.simpleBuilder.SimpleVrpBuilder;

public class CvrpTests extends SimpleTestsBase
{
    //TODO update split assertions to match method names
    private GarridoRiff garridoRiff = new GarridoRiff();

    @Test
    public void fourRoutesAndOptimalCostFound()
    {
        VehicleRoutingProblem vrp = new SimpleVrpBuilder()
            .setDepotLocation(0, 0)
            .setVehicleCapacity(500)
            .addJob(100, 0).setDemand(500).build()
            .addJob(0, 100).setDemand(500).build()
            .addJob(-100, 0).setDemand(500).build()
            .addJob(0, -100).setDemand(500).build()
            .build();

        VehicleRoutingAlgorithm vra = garridoRiff.createAlgorithm(vrp, false, false);
        vra.setMaxIterations(100);

        runOnce(vra)
            .eachRunHasNoUnassignedJobs()
            .eachCostIsEqualTo(800, 1);
    }

    @Test
    public void fourRoutesAndOptimalCostFound2()
    {
        VehicleRoutingProblem vrp = new SimpleVrpBuilder()
            .setDepotLocation(0, 0)
            .setVehicleCapacity(150)
            .addJob(100, 0).setDemand(50).build()
            .addJob(200, 0).setDemand(50).build()
            .addJob(300, 0).setDemand(50).build()
            .addJob(0, 100).setDemand(50).build()
            .addJob(0, 200).setDemand(50).build()
            .addJob(0, 300).setDemand(50).build()
            .addJob(-100, 0).setDemand(50).build()
            .addJob(-200, 0).setDemand(50).build()
            .addJob(-300, 0).setDemand(50).build()
            .addJob(0, -100).setDemand(50).build()
            .addJob(0, -200).setDemand(50).build()
            .addJob(0, -300).setDemand(50).build()
            .build();

        VehicleRoutingAlgorithm vra = garridoRiff.createAlgorithm(vrp, false, false);
        vra.setMaxIterations(100);

        runOnce(vra)
            .eachRunHasNoUnassignedJobs()
            .eachRouteCountIsEqualTo(4)
            .eachCostIsEqualTo(2400, 1);
    }

    @Test
    public void oneRouteAndOptimalCostFound()
    {
        VehicleRoutingProblem vrp = new SimpleVrpBuilder()
            .setDepotLocation(0, 0)
            .setVehicleCapacity(100)
            .addJob(-100, -100).setDemand(10).build()
            .addJob(-100, 100).setDemand(10).build()
            .addJob(100, -100).setDemand(10).build()
            .addJob(100, 100).setDemand(10).build()
            .build();

        VehicleRoutingAlgorithm vra = garridoRiff.createAlgorithm(vrp, false, false);
        vra.setMaxIterations(100);

        double expectedCost = 3 * 200 + 2 * Math.sqrt(100 * 100 + 100 * 100);
        runOnce(vra)
            .eachRunHasNoUnassignedJobs()
            .eachCostIsEqualTo(expectedCost, 1);
    }
}
