package pl.pielat.util.problemParsing;

import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import pl.pielat.algorithm.ExtendedProblemDefinition;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class VrpDirectoryTraverser implements Iterator<ExtendedProblemDefinition>
{
    public enum FileType
    {
        SOLOMON,
        TSPLIB95
    }

    private VrpFileParser parser;
    private Queue<File> files;
    private ExtendedProblemDefinition nextItem;

    public VrpDirectoryTraverser(String problemsDirectory, VrpFileParser parser)
    {
        File directory = new File(problemsDirectory);

        File[] fileArray = directory.listFiles();
        if (fileArray == null)
            throw new IllegalArgumentException("Path does not denote a directory.");

        files = new LinkedList<>(Arrays.asList(fileArray));
        this.parser = parser;
    }

    private boolean readNext()
    {
        while (!files.isEmpty())
        {
            File file = files.poll();
            VehicleRoutingProblem vrp;
            boolean timeWindows, transportAsymmetry;

            try
            {
                vrp = parser.parse(file.getAbsolutePath());
                timeWindows = parser.timeWindowsDetected();
                transportAsymmetry = parser.transportAsymmetryDetected();
            }
            catch (VrpParseException e)
            {
                continue;
            }
            catch (FileNotFoundException e)
            {
                throw new RuntimeException(e);
            }

            nextItem = new ExtendedProblemDefinition(file.getName(), vrp, timeWindows, transportAsymmetry);
            return true;
        }
        return false;
    }

    @Override
    public boolean hasNext()
    {
        return nextItem != null || readNext();
    }

    @Override
    public ExtendedProblemDefinition next()
    {
        if (!hasNext())
            throw new NoSuchElementException();

        ExtendedProblemDefinition item = nextItem;
        nextItem = null;
        return item;
    }

    @Override
    public void remove()
    {
        throw new UnsupportedOperationException();
    }
}
