package pl.pielat.util.problemParsing;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Delivery;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.util.Coordinate;
import com.graphhopper.jsprit.core.util.FastVehicleRoutingTransportCostsMatrix;
import com.graphhopper.jsprit.core.util.VehicleRoutingTransportCostsMatrix;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Tsplib95FileReader implements VrpFileParser
{
    enum ProblemType{
        CVRP,
        Unsupported,
        Unknown,
    }

    enum EdgeWeightType{
        Explicit,
        Euc_2D,
        Unsupported,
        Unknown,
    }

    enum EdgeWeightFormat{
        FullMatrix,
        Unsupported,
        Unknown,
    }

    private BufferedReader reader;

    private String name = null;
    private String comment = null;
    private int dimension = -1;
    private int capacity = -1;
    private ProblemType problemType = ProblemType.Unknown;
    private EdgeWeightType edgeWeightType = EdgeWeightType.Unknown;
    private EdgeWeightFormat edgeWeightFormat = EdgeWeightFormat.Unknown;
    private Coordinate[] nodeCoords = null;
    private double[][] edgeWeightMatrix = null;
    private int[] demands = null;
    private int depotIndex = -1;

    public VehicleRoutingProblem parse(String filename) throws FileNotFoundException, VrpParseException
    {
        reader = new BufferedReader(new FileReader(filename));
        try
        {
            while (readNextSection());
        }
        catch (IOException e)
        {
            throw new VrpParseException(e);
        }

        // Building problem instance
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();

        if (problemType != ProblemType.CVRP)
            throw new VrpParseException("Problem type is unknown/unsupported");
        if (edgeWeightType == EdgeWeightType.Unsupported)
            throw new VrpParseException("Edge weight type is unsupported");
        if (dimension == -1)
            throw new VrpParseException("Dimension is unknown");
        if (capacity == -1)
            throw new VrpParseException("Capacity is unknown");
        if (depotIndex == -1)
            throw new VrpParseException("Depot index is unknown");

        Location depotLocation = Location.Builder.newInstance()
            .setCoordinate(nodeCoords[depotIndex])
            .setIndex(depotIndex)
            .build();
        VehicleTypeImpl vehicleType = VehicleTypeImpl.Builder.newInstance("vehicleType")
            .addCapacityDimension(0, capacity)
            .build();
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("vehicle")
            .setType(vehicleType)
            .setStartLocation(depotLocation)
            .setReturnToDepot(true)
            .build();
        builder.addVehicle(vehicle);

        if (edgeWeightType == EdgeWeightType.Explicit)
        {
            FastVehicleRoutingTransportCostsMatrix.Builder matrixBuilder =
                FastVehicleRoutingTransportCostsMatrix.Builder.newInstance(dimension, false);

            for (int i = 0; i < dimension; i++)
            {
                for (int j = 0; j < dimension; j++)
                {
                    double weight = edgeWeightMatrix[i][j];
                    matrixBuilder.addTransportDistance(i, j, weight);
                }
            }
            builder.setRoutingCost(matrixBuilder.build());
        }

        for (int i = 0; i < dimension; i++)
        {
            if (i == depotIndex)
                continue;

            Location deliveryLocation = Location.Builder.newInstance()
                .setCoordinate(nodeCoords[i])
                .setIndex(i)
                .build();
            Delivery delivery = Delivery.Builder.newInstance(Integer.toString(i + 1))
                .setLocation(deliveryLocation)
                .addSizeDimension(0, demands[i])
                .build();
            builder.addJob(delivery);
        }

        return builder.build();
    }

    private boolean readNextSection() throws IOException, VrpParseException
    {
        String line = reader.readLine();
        if (line == null)
            return false;

        line = line.trim();
        String section = getSection(line);

        switch (section)
        {
            case "NAME":
                name = getStringScalarValue(line).trim();
                break;
            case "COMMENT":
                comment = getStringScalarValue(line).trim();
                break;
            case "DIMENSION":
                dimension = getIntScalarValue(line);
                break;
            case "CAPACITY":
                capacity = getIntScalarValue(line);
                break;
            case "TYPE":
                switch (getStringScalarValue(line).toUpperCase())
                {
                    case "CVRP":
                        problemType = ProblemType.CVRP;
                        break;
                    default:
                        problemType = ProblemType.Unsupported;
                        break;
                }
                break;
            case "EDGE_WEIGHT_TYPE":
                switch (getStringScalarValue(line).toUpperCase())
                {
                    case "EXPLICIT":
                        edgeWeightType = EdgeWeightType.Explicit;
                        break;
                    case "EUC_2D":
                        edgeWeightType = EdgeWeightType.Euc_2D;
                        break;
                    default:
                        edgeWeightType = EdgeWeightType.Unsupported;
                        break;
                }
                break;
            case "EDGE_WEIGHT_FORMAT":
                switch (getStringScalarValue(line).toUpperCase())
                {
                    case "FULL_MATRIX":
                        edgeWeightFormat = EdgeWeightFormat.FullMatrix;
                        break;
                    default:
                        edgeWeightFormat = EdgeWeightFormat.Unsupported;
                        break;
                }
                break;
            case "NODE_COORD_SECTION":
                readNodeCoordSection();
                break;
            case "EDGE_WEIGHT_SECTION":
                readEdgeWeightSection();
                break;
            case "DEMAND_SECTION":
                readDemandSection();
                break;
            case "DEPOT_SECTION":
                readDepotSection();
                break;
        }
        return true;
    }

    private static String getSection(String line)
    {
        String[] tokens = line.trim().split("\\s+");
        String section = tokens[0].replace(":", "");
        return section.toUpperCase();
    }

    private static int getIntScalarValue(String line)
    {
        String[] tokens = line.split(":");
        return Integer.parseInt(tokens[1].trim());
    }

    private static String getStringScalarValue(String line)
    {
        String[] tokens = line.split(":");
        return tokens[1].trim();
    }

    private void readNodeCoordSection() throws VrpParseException, IOException
    {
        if (dimension == -1)
            throw new VrpParseException("Problem dimension is unknown");

        nodeCoords = new Coordinate[dimension];
        for (int i = 0; i < dimension; i++)
        {
            String line = reader.readLine();
            String[] tokens = line.trim().split("\\s+");

            int nodeNumber = Integer.parseInt(tokens[0]);
            double latitude = Double.parseDouble(tokens[1]);
            double longitude = Double.parseDouble(tokens[2]);
            Coordinate coords = Coordinate.newInstance(latitude, longitude);

            if (i + 1 != nodeNumber)
                throw new VrpParseException("Unsupported node numbering");

            nodeCoords[i] = coords;
        }
    }

    private void readEdgeWeightSection() throws IOException, VrpParseException
    {
        if (dimension == -1)
            throw new VrpParseException("Problem dimension is unknown");
        if (edgeWeightType != EdgeWeightType.Explicit)
            throw new VrpParseException("Edge weight type is not explicit");
        if (edgeWeightFormat != EdgeWeightFormat.FullMatrix)
            throw new VrpParseException("Edge weight format is unknown/unsupported");

        edgeWeightMatrix = new double[dimension][dimension];
        for (int i = 0; i < dimension; i++)
        {
            String line = reader.readLine();
            String[] tokens = line.trim().split("\\s+");

            for (int j = 0; j < dimension; j++)
            {
                double weight = Double.parseDouble(tokens[j]);
                edgeWeightMatrix[i][j] = weight;
            }
        }
    }

    private void readDemandSection() throws IOException, VrpParseException
    {
        if (dimension == -1)
            throw new VrpParseException("Problem dimension is unknown");

        demands = new int[dimension];
        for (int i = 0; i < dimension; i++)
        {
            String line = reader.readLine();
            String[] tokens = line.trim().split("\\s+");

            int nodeNumber = Integer.parseInt(tokens[0]);
            int demand = Integer.parseInt(tokens[1]);

            if (i + 1 != nodeNumber)
                throw new VrpParseException("Unsupported node numbering");

            demands[nodeNumber - 1] = demand;
        }
    }

    private void readDepotSection() throws VrpParseException, IOException
    {
        if (dimension == -1)
            throw new VrpParseException("Problem dimension is unknown");

        for (int i = 0; i < dimension; i++)
        {
            String line = reader.readLine();
            int nodeNumber = Integer.parseInt(line.trim());

            if (nodeNumber == -1)
                break;
            if (nodeNumber == 0 || nodeNumber > dimension)
                throw new VrpParseException("Unsupported node numbering");
            if (i > 0)
                throw new VrpParseException("More than one depot index found");
            depotIndex = nodeNumber - 1;
        }
    }
}
