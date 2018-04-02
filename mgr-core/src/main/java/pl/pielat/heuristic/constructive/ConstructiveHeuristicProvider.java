package pl.pielat.heuristic.constructive;

import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import pl.pielat.heuristic.BaseHeuristicProvider;
import pl.pielat.heuristic.constructive.concrete.ClarkeWrightHeuristic;
import pl.pielat.heuristic.constructive.concrete.KilbyAlgorithm;
import pl.pielat.heuristic.constructive.concrete.MoleJamesonHeuristic;
import pl.pielat.heuristic.constructive.concrete.SweepAlgorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ConstructiveHeuristicProvider extends BaseHeuristicProvider<ConstructiveHeuristic>
{
    public ConstructiveHeuristicProvider(VehicleRoutingProblem vrp, Random random)
    {
        super(vrp, random);
    }

    @Override
    protected List<String> getAllIds()
    {
        List<String> ids = new ArrayList<String>(4);
        ids.add("clarke&wright");
        ids.add("mole&jameson");
        ids.add("sweep");
        ids.add("kilby");
        return ids;
    }

    @Override
    protected ConstructiveHeuristic getInstanceById(String id) throws Exception
    {
        ConstructiveHeuristic instance;
        switch (id)
        {
            case "clarke&wright":
                instance = new ClarkeWrightHeuristic(vrp);
                break;
            case "mole&jameson":
                instance = new MoleJamesonHeuristic(vrp);
                break;
            case "sweep":
                instance = new SweepAlgorithm(vrp);
                break;
            case "kilby":
                instance = new KilbyAlgorithm(vrp);
                break;
            default:
                throw new Exception("Unknown id.");
        }
        return instance;
    }
}
