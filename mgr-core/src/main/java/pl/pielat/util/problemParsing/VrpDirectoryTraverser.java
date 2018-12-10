package pl.pielat.util.problemParsing;

import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class VrpDirectoryTraverser implements Iterator<VrpDirectoryTraverser.Item>
{
    public enum FileType
    {
        SOLOMON,
        TSPLIB95
    }

    public class Item
    {
        public final String fileName;
        public final VehicleRoutingProblem vrp;
        public final boolean timeWindows;
        public final boolean transportAsymmetry;

        private Item(String fileName, VehicleRoutingProblem vrp, boolean timeWindows, boolean transportAsymmetry)
        {
            this.fileName = fileName;
            this.vrp = vrp;
            this.timeWindows = timeWindows;
            this.transportAsymmetry = transportAsymmetry;
        }
    }

    private VrpFileParser parser;
    private Queue<File> files;
    private Item nextItem;

    public VrpDirectoryTraverser(String problemsDirectory, FileType fileType)
    {
        File directory = new File(problemsDirectory);

        File[] fileArray = directory.listFiles();
        if (fileArray == null)
            throw new IllegalArgumentException("Path does not denote a directory.");

        switch (fileType)
        {
            case SOLOMON:
                parser = new SolomonFileReader();
                break;
            case TSPLIB95:
                parser = new Tsplib95FileReader();
                break;
            default:
                throw new IllegalArgumentException("File type is not supported.");
        }

        files = new LinkedList<>(Arrays.asList(fileArray));
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

            nextItem = new Item(file.getName(), vrp, timeWindows, transportAsymmetry);
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
    public Item next()
    {
        if (!hasNext())
            throw new NoSuchElementException();

        Item item = nextItem;
        nextItem = null;
        return item;
    }

    @Override
    public void remove()
    {
        throw new UnsupportedOperationException();
    }
}
