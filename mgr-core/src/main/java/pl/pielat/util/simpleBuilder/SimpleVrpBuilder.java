package pl.pielat.util.simpleBuilder;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Delivery;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.util.Coordinate;

public class SimpleVrpBuilder
{
    public class JobBuilder
    {
        private int demand = 0;
        private TimeWindow timeWindow;
        private double serviceTime = -1;
        private Location location;

        private JobBuilder(double x, double y)
        {
            location = Location.newInstance(x, y);
        }

        public JobBuilder setDemand(int demand)
        {
            this.demand = demand;
            return this;
        }

        public JobBuilder setTimeWindow(double start, double end)
        {
            timeWindow = new TimeWindow(start, end);
            return this;
        }

        public JobBuilder setServiceTime(double serviceTime)
        {
            this.serviceTime = serviceTime;
            return this;
        }

        public SimpleVrpBuilder build()
        {
            String id = "job" + jobCounter++;

            Service.Builder<Delivery> jobBuilder = Delivery.Builder.newInstance(id)
                .setLocation(location)
                .addSizeDimension(0, demand);

            if (timeWindow != null)
                jobBuilder.addTimeWindow(timeWindow);

            if (serviceTime >= 0)
                jobBuilder.setServiceTime(serviceTime);

            if (demand > 0)
                builder.addJob(jobBuilder.build());

            return SimpleVrpBuilder.this;
        }
    }

    private final VehicleRoutingProblem.Builder builder;

    private int jobCounter = 0;

    private Coordinate depotCoordinate;
    private TimeWindow schedulingHorizon;
    private int vehicleCapacity;

    public SimpleVrpBuilder()
    {
        builder = VehicleRoutingProblem.Builder.newInstance();
        depotCoordinate = Coordinate.newInstance(0, 0);
    }

    public SimpleVrpBuilder setDepotLocation(double x, double y)
    {
        Location depot = Location.Builder.newInstance()
            .setCoordinate(Coordinate.newInstance(x, y))
            .setId("depotLocation")
            .build();

        return this;
    }

    public SimpleVrpBuilder setSchedulingHorizon(double start, double end)
    {
        schedulingHorizon = new TimeWindow(start, end);
        return this;
    }

    public SimpleVrpBuilder setVehicleCapacity(int capacity)
    {
        vehicleCapacity = capacity;
        return this;
    }

    public JobBuilder addJob(double x, double y)
    {
        return new JobBuilder(x, y);
    }

    public VehicleRoutingProblem build()
    {
        Location depot = Location.Builder.newInstance()
            .setCoordinate(depotCoordinate)
            .setId("depotLocation")
            .build();

        VehicleType vehicleType = VehicleTypeImpl.Builder.newInstance("vehicleType")
            .addCapacityDimension(0, vehicleCapacity)
            .build();

        VehicleImpl.Builder vehicleBuilder = VehicleImpl.Builder.newInstance("vehicle")
            .setStartLocation(depot)
            .setReturnToDepot(true)
            .setType(vehicleType);

        if (schedulingHorizon != null)
        {
            vehicleBuilder.setEarliestStart(schedulingHorizon.getStart());
            vehicleBuilder.setLatestArrival(schedulingHorizon.getEnd());
        }

        builder.addVehicle(vehicleBuilder.build());

        return builder.build();
    }
}
