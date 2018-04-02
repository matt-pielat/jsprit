package pl.pielat.util.problemParsing;

import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;

import java.io.FileNotFoundException;

public interface VrpFileParser
{
    VehicleRoutingProblem parse(String fileName) throws VrpParseException, FileNotFoundException;
}
