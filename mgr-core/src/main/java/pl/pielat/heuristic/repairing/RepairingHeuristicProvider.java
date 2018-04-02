package pl.pielat.heuristic.repairing;

import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import pl.pielat.heuristic.BaseHeuristicProvider;
import pl.pielat.heuristic.repairing.concrete.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RepairingHeuristicProvider extends BaseHeuristicProvider<RepairingHeuristic>
{
    public RepairingHeuristicProvider(VehicleRoutingProblem vrp, Random random)
    {
        super(vrp, random);
    }

    @Override
    protected List<String> getAllIds()
    {
        List<String> ids = new ArrayList<String>(4);
        ids.add("2opt");
        ids.add("3opt");
        ids.add("orOpt");
        ids.add("stringCross");
        ids.add("stringRelocation");
        ids.add("stringExchange");
        return ids;
    }

    @Override
    protected RepairingHeuristic getInstanceById(String id) throws Exception
    {
        RepairingHeuristic instance;
        switch (id)
        {
            case "2opt":
                instance = new LambdaOptTwo(vrp);
                break;
            case "3opt":
                instance = new LambdaOptThree(vrp);
                break;
            case "orOpt":
                instance = new OrOpt(vrp);
                break;
            case "stringCross":
                instance = new StringCross(vrp);
                break;
            case "stringRelocation":
                instance = new StringRelocation(vrp);
                break;
            case "stringExchange":
                instance = new StringExchange(vrp);
                break;
            default:
                throw new Exception("Unknown id.");
        }
        return instance;
    }
}
