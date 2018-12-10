package pl.pielat.util.problemParsing;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Delivery;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.util.Coordinate;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class SolomonFileReader implements VrpFileParser
{
    private int vehicleCount = -1;
    private int vehicleCapacity = -1;

    private Coordinate depotLocation;
    private double operationStartTime;
    private double operationEndTime;

    private List<Coordinate> clientLocations;
    private List<Integer> clientDemands;
    private List<TimeWindow> serviceTimeWindows;
    private List<Double> serviceTimes;

    private BufferedReader reader;

    public VehicleRoutingProblem parse(String filename) throws VrpParseException, FileNotFoundException
    {
        reader = new BufferedReader(new FileReader(filename));

        try
        {
            reader.readLine(); // skip first line
            while (readNextSection());
        }
        catch (IOException e)
        {
            throw new VrpParseException(e);
        }

        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();

        Location depotLocation = Location.Builder.newInstance()
            .setCoordinate(this.depotLocation)
            .setId(Integer.toString(0))
            .build();
        VehicleTypeImpl vehicleType = VehicleTypeImpl.Builder.newInstance("vehicleType")
            .addCapacityDimension(0, vehicleCapacity)
            .build();
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("vehicle")
            .setType(vehicleType)
            .setStartLocation(depotLocation)
            .setEarliestStart(operationStartTime)
            .setLatestArrival(operationEndTime)
            .setReturnToDepot(true)
            .build();
        builder.addVehicle(vehicle);

        Iterator<Coordinate> clientLocations = this.clientLocations.iterator();
        Iterator<Integer> clientDemands = this.clientDemands.iterator();
        Iterator<TimeWindow> serviceTimeWindows = this.serviceTimeWindows.iterator();
        Iterator<Double> serviceTimes = this.serviceTimes.iterator();

        for (int i = 1; clientLocations.hasNext(); i++)
        {
            Location deliveryLocation = Location.Builder.newInstance()
                .setCoordinate(clientLocations.next())
                .setId(Integer.toString(i))
                .build();
            Delivery delivery = Delivery.Builder.newInstance(Integer.toString(i))
                .setLocation(deliveryLocation)
                .addSizeDimension(0, clientDemands.next())
                .addTimeWindow(serviceTimeWindows.next())
                .setServiceTime(serviceTimes.next())
                .build();
            builder.addJob(delivery);
        }

        return builder.build();
    }

    @Override
    public boolean transportAsymmetryDetected()
    {
        return false;
    }

    @Override
    public boolean timeWindowsDetected()
    {
        return true;
    }

    private boolean readNextSection() throws IOException, VrpParseException
    {
        while (true)
        {
            String line = reader.readLine();
            if (line == null)
                return false;

            line = line.trim().toUpperCase();
            if (line.isEmpty())
                continue;

            switch (line)
            {
                case "VEHICLE":
                    if (!validateVehicleSectionHeader())
                        throw new VrpParseException("VEHICLE section header has incorrect format");
                    readVehicleSection();
                    break;
                case "CUSTOMER":
                    if (!validateCustomerSectionHeader())
                        throw new VrpParseException("CUSTOMER section header has incorrect format");
                    readCustomerSection();
                    break;
                default:
                    throw new VrpParseException();
            }

        }

    }

    private boolean validateVehicleSectionHeader() throws IOException
    {
        String line = reader.readLine();
        String[] tokens = line.trim().split("\\s+");

        if (tokens.length < 2)
            return false;

        if (!tokens[0].toUpperCase().equals("NUMBER"))
            return false;
        if (!tokens[1].toUpperCase().equals("CAPACITY"))
            return false;

        return true;
    }

    private void readVehicleSection() throws IOException
    {
        String line = reader.readLine();
        String[] tokens = line.trim().split("\\s+");

        vehicleCount = Integer.parseInt(tokens[0]);
        vehicleCapacity = Integer.parseInt(tokens[1]);
    }

    private boolean validateCustomerSectionHeader() throws IOException
    {
        String line = reader.readLine();
        String[] tokens = line.trim().split("\\s+");

        String[] expectedTokens = new String[] {
            "CUST", "NO.", "XCOORD.", "YCOORD.", "DEMAND",
            "READY", "TIME", "DUE", "DATE", "SERVICE", "TIME"
        };

        if (tokens.length < expectedTokens.length)
            return false;

        for (int i = 0; i < expectedTokens.length; i++)
        {
            String token = tokens[i].toUpperCase();
            if (!token.equals(expectedTokens[i]))
                return false;
        }

        return true;
    }

    private void readCustomerSection() throws IOException, VrpParseException
    {
        String line = reader.readLine();
        if (!line.trim().isEmpty())
            throw new VrpParseException("Empty first line is expected in CUSTOMER section");

        // Depot
        line = reader.readLine();
        String[] tokens = line.trim().split("\\s+");

        depotLocation = Coordinate.newInstance(
            Double.parseDouble(tokens[1]),
            Double.parseDouble(tokens[2]));
        operationStartTime = Double.parseDouble(tokens[4]);
        operationEndTime = Double.parseDouble(tokens[5]);

        clientLocations = new LinkedList<>();
        clientDemands = new LinkedList<>();
        serviceTimeWindows = new LinkedList<>();
        serviceTimes = new LinkedList<>();

        while (true)
        {
            line = reader.readLine();

            if (line == null)
                return;

            line = line.trim();
            if (line.isEmpty())
                return;

            tokens = line.split("\\s+");

            Coordinate coords = Coordinate.newInstance(
                Double.parseDouble(tokens[1]),
                Double.parseDouble(tokens[2]));
            clientLocations.add(coords);

            int demand = Integer.parseInt(tokens[3]);
            clientDemands.add(demand);

            TimeWindow serviceWindow = TimeWindow.newInstance(
                Double.parseDouble(tokens[4]),
                Double.parseDouble(tokens[5]));
            serviceTimeWindows.add(serviceWindow);

            double serviceTime = Double.parseDouble(tokens[6]);
            serviceTimes.add(serviceTime);
        }
    }
}
