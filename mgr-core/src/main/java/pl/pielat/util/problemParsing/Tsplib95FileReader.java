package pl.pielat.util.problemParsing;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Delivery;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.util.Coordinate;
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

    private BufferedReader _reader;

    private String _name = null;
    private String _comment = null;
    private int _dimension = -1;
    private int _capacity = -1;
    private ProblemType _problemType = ProblemType.Unknown;
    private EdgeWeightType _edgeWeightType = EdgeWeightType.Unknown;
    private EdgeWeightFormat _edgeWeightFormat = EdgeWeightFormat.Unknown;
    private Coordinate[] _nodeCoords = null;
    private double[][] _edgeWeightMatrix = null;
    private int[] _demands = null;
    private int _depotIndex = -1;

    public VehicleRoutingProblem parse(String filename) throws FileNotFoundException, VrpParseException
    {
        _reader = new BufferedReader(new FileReader(filename));
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

        if (_problemType != ProblemType.CVRP)
            throw new VrpParseException("Problem type is unknown/unsupported");
        if (_edgeWeightType == EdgeWeightType.Unsupported)
            throw new VrpParseException("Edge weight type is unsupported");
        if (_dimension == -1)
            throw new VrpParseException("Dimension is unknown");
        if (_capacity == -1)
            throw new VrpParseException("Capacity is unknown");
        if (_depotIndex == -1)
            throw new VrpParseException("Depot index is unknown");

        Location depotLocation = Location.Builder.newInstance()
            .setCoordinate(_nodeCoords[_depotIndex])
            .setId(Integer.toString(_depotIndex + 1))
            .build();
        VehicleTypeImpl vehicleType = VehicleTypeImpl.Builder.newInstance("vehicleType")
            .addCapacityDimension(0, _capacity)
            .build();
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("vehicle")
            .setType(vehicleType)
            .setStartLocation(depotLocation)
            .setReturnToDepot(true)
            .build();
        builder.addVehicle(vehicle);

        if (_edgeWeightType == EdgeWeightType.Explicit)
        {
            VehicleRoutingTransportCostsMatrix.Builder matrixBuilder =
                VehicleRoutingTransportCostsMatrix.Builder.newInstance(false);

            for (int i = 0; i < _dimension; i++)
            {
                for (int j = 0; j < _dimension; j++)
                {
                    String from = Integer.toString(i + 1);
                    String to = Integer.toString(j + 1);
                    double weight = _edgeWeightMatrix[i][j];

                    matrixBuilder.addTransportDistance(from, to, weight);
                    matrixBuilder.addTransportTime(from, to, weight);
                }
            }
            builder.setRoutingCost(matrixBuilder.build());
        }

        for (int i = 0; i < _dimension; i++)
        {
            if (i == _depotIndex)
                continue;

            Location deliveryLocation = Location.Builder.newInstance()
                .setCoordinate(_nodeCoords[i])
                .setId(Integer.toString(i + 1))
                .build();
            Delivery delivery = Delivery.Builder.newInstance(Integer.toString(i + 1))
                .setLocation(deliveryLocation)
                .addSizeDimension(0, _demands[i])
                .build();
            builder.addJob(delivery);
        }

        return builder.build();
    }

    private boolean readNextSection() throws IOException, VrpParseException
    {
        String line = _reader.readLine();
        if (line == null)
            return false;

        line = line.trim();
        String section = getSection(line);

        switch (section)
        {
            case "NAME":
                _name = getStringScalarValue(line).trim();
                break;
            case "COMMENT":
                _comment = getStringScalarValue(line).trim();
                break;
            case "DIMENSION":
                _dimension = getIntScalarValue(line);
                break;
            case "CAPACITY":
                _capacity = getIntScalarValue(line);
                break;
            case "TYPE":
                switch (getStringScalarValue(line).toUpperCase())
                {
                    case "CVRP":
                        _problemType = ProblemType.CVRP;
                        break;
                    default:
                        _problemType = ProblemType.Unsupported;
                        break;
                }
                break;
            case "EDGE_WEIGHT_TYPE":
                switch (getStringScalarValue(line).toUpperCase())
                {
                    case "EXPLICIT":
                        _edgeWeightType = EdgeWeightType.Explicit;
                        break;
                    case "EUC_2D":
                        _edgeWeightType = EdgeWeightType.Euc_2D;
                        break;
                    default:
                        _edgeWeightType = EdgeWeightType.Unsupported;
                        break;
                }
                break;
            case "EDGE_WEIGHT_FORMAT":
                switch (getStringScalarValue(line).toUpperCase())
                {
                    case "FULL_MATRIX":
                        _edgeWeightFormat = EdgeWeightFormat.FullMatrix;
                        break;
                    default:
                        _edgeWeightFormat = EdgeWeightFormat.Unsupported;
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
        if (_dimension == -1)
            throw new VrpParseException("Problem dimension is unknown");

        _nodeCoords = new Coordinate[_dimension];
        for (int i = 0; i < _dimension; i++)
        {
            String line = _reader.readLine();
            String[] tokens = line.trim().split("\\s+");

            int nodeNumber = Integer.parseInt(tokens[0]);
            double latitude = Double.parseDouble(tokens[1]);
            double longitude = Double.parseDouble(tokens[2]);
            Coordinate coords = Coordinate.newInstance(latitude, longitude);

            if (i + 1 != nodeNumber)
                throw new VrpParseException("Unsupported node numbering");

            _nodeCoords[nodeNumber - 1] = coords;
        }
    }

    private void readEdgeWeightSection() throws IOException, VrpParseException
    {
        if (_dimension == -1)
            throw new VrpParseException("Problem dimension is unknown");
        if (_edgeWeightType != EdgeWeightType.Explicit)
            throw new VrpParseException("Edge weight type is not explicit");
        if (_edgeWeightFormat != EdgeWeightFormat.FullMatrix)
            throw new VrpParseException("Edge weight format is unknown/unsupported");

        _edgeWeightMatrix = new double[_dimension][_dimension];
        for (int i = 0; i < _dimension; i++)
        {
            String line = _reader.readLine();
            String[] tokens = line.trim().split("\\s+");

            for (int j = 0; j < _dimension; j++)
            {
                double weight = Double.parseDouble(tokens[j]);
                _edgeWeightMatrix[i][j] = weight;
            }
        }
    }

    private void readDemandSection() throws IOException, VrpParseException
    {
        if (_dimension == -1)
            throw new VrpParseException("Problem dimension is unknown");

        _demands = new int[_dimension];
        for (int i = 0; i < _dimension; i++)
        {
            String line = _reader.readLine();
            String[] tokens = line.trim().split("\\s+");

            int nodeNumber = Integer.parseInt(tokens[0]);
            int demand = Integer.parseInt(tokens[1]);

            if (i + 1 != nodeNumber)
                throw new VrpParseException("Unsupported node numbering");

            _demands[nodeNumber - 1] = demand;
        }
    }

    private void readDepotSection() throws VrpParseException, IOException
    {
        if (_dimension == -1)
            throw new VrpParseException("Problem dimension is unknown");

        for (int i = 0; i < _dimension; i++)
        {
            String line = _reader.readLine();
            int nodeNumber = Integer.parseInt(line.trim());

            if (nodeNumber == -1)
                break;
            if (nodeNumber == 0 || nodeNumber > _dimension)
                throw new VrpParseException("Unsupported node numbering");
            if (i > 0)
                throw new VrpParseException("More than one depot index found");
            _depotIndex = nodeNumber - 1;
        }
    }
}
