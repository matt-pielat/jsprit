package pl.pielat.util.problemParsing;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Delivery;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.util.Coordinate;
import com.graphhopper.jsprit.core.util.FastVehicleRoutingTransportCostsMatrix;
import scala.Int;

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
        LowerRow,
        Unsupported,
        Unknown,
    }

    private BufferedReader reader;

    private String name;
    private String comment;
    private int dimension;
    private int capacity;
    private ProblemType problemType;
    private EdgeWeightType edgeWeightType;
    private EdgeWeightFormat edgeWeightFormat;
    private Coordinate[] nodeCoords;
    private double[][] edgeWeightMatrix;
    private int[] demands;
    private int depotIndex;

    private boolean lastParseSucceeded = false;
    private boolean transportAsymmetryDetected;

    private void resetInternalState()
    {
        name = null;
        comment = null;
        dimension = -1;
        capacity = -1;
        problemType = ProblemType.Unknown;
        edgeWeightType = EdgeWeightType.Unknown;
        edgeWeightFormat = EdgeWeightFormat.Unknown;
        nodeCoords = null;
        edgeWeightMatrix = null;
        demands = null;
        depotIndex = -1;

        lastParseSucceeded = false;
        transportAsymmetryDetected = false;
    }

    public VehicleRoutingProblem parse(String filename) throws FileNotFoundException, VrpParseException
    {
        resetInternalState();

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
        if (edgeWeightType == EdgeWeightType.Unknown)
            throw new VrpParseException("Edge weight type is unknown");
        if (dimension == -1)
            throw new VrpParseException("Dimension is unknown");
        if (capacity == -1)
            throw new VrpParseException("Capacity is unknown");
        if (depotIndex == -1)
            throw new VrpParseException("Depot index is unknown");

        Location.Builder depotLocationBuilder = Location.Builder.newInstance()
            .setId(Integer.toString(depotIndex))
            .setIndex(depotIndex);
        if (nodeCoords != null)
            depotLocationBuilder.setCoordinate(nodeCoords[depotIndex]);
        VehicleTypeImpl vehicleType = VehicleTypeImpl.Builder.newInstance("vehicleType")
            .addCapacityDimension(0, capacity)
            .build();
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("vehicle")
            .setType(vehicleType)
            .setStartLocation(depotLocationBuilder.build())
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

                    if (weight != edgeWeightMatrix[j][i])
                        transportAsymmetryDetected = true;
                }
            }
            builder.setRoutingCost(matrixBuilder.build());
        }

        for (int i = 0; i < dimension; i++)
        {
            if (i == depotIndex)
                continue;

            Location.Builder deliveryLocationBuilder = Location.Builder.newInstance()
                .setId(Integer.toString(i))
                .setIndex(i);
            if (nodeCoords != null)
                deliveryLocationBuilder.setCoordinate(nodeCoords[i]);

            Delivery delivery = Delivery.Builder.newInstance(Integer.toString(i))
                .setLocation(deliveryLocationBuilder.build())
                .addSizeDimension(0, demands[i])
                .build();
            builder.addJob(delivery);
        }

        lastParseSucceeded = true;
        return builder.build();
    }

    @Override
    public boolean transportAsymmetryDetected() throws VrpParseException
    {
        if (!lastParseSucceeded)
            throw new VrpParseException();
        return transportAsymmetryDetected;
    }

    @Override
    public boolean timeWindowsDetected() throws VrpParseException
    {
        if (!lastParseSucceeded)
            throw new VrpParseException();
        return false;
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
                    case "LOWER_ROW":
                        edgeWeightFormat = EdgeWeightFormat.LowerRow;
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
                readEdgeWeightSection(edgeWeightFormat);
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

    private void readEdgeWeightSection(EdgeWeightFormat edgeWeightFormat) throws VrpParseException, IOException
    {
        if (dimension == -1)
            throw new VrpParseException("Problem dimension is unknown");
        if (edgeWeightType != EdgeWeightType.Explicit)
            throw new VrpParseException("Edge weight type is not explicit");

        switch (edgeWeightFormat)
        {
            case FullMatrix:
                readFullMatrix();
                break;
            case LowerRow:
                readLowerRow();
                break;
            default:
                throw new VrpParseException("Unsupported edge weight format.");
        }
    }

    private void readFullMatrix() throws IOException, VrpParseException
    {
        int i = 0;
        int j = 0;

        edgeWeightMatrix = new double[dimension][dimension];

        while (i < dimension)
        {
            String line = reader.readLine();
            String[] tokens = line.trim().split("\\s+");

            for (String token : tokens)
            {
                double weight = Double.parseDouble(token);
                edgeWeightMatrix[i][j] = weight;

                j++;
                if (j == dimension)
                {
                    i++;
                    j = 0;
                }
            }
        }

        if (j > 0)
            throw new VrpParseException("More values than expected.");
    }

    private void readLowerRow() throws IOException, VrpParseException
    {
        int i = 1;
        int j = 0;

        edgeWeightMatrix = new double[dimension][dimension];

        while (i < dimension)
        {
            String line = reader.readLine();
            String[] tokens = line.trim().split("\\s+");

            for (String token : tokens)
            {
                double weight = Double.parseDouble(token);
                edgeWeightMatrix[i][j] = edgeWeightMatrix[j][i] = weight;

                j++;
                if (j == i)
                {
                    i++;
                    j = 0;
                }
            }
        }

        if (j > 0)
            throw new VrpParseException("More values than expected.");
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
