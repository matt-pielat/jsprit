package pl.pielat.util;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.util.Coordinate;
import org.junit.Assert;
import org.junit.Test;
import pl.pielat.heuristic.Place;
import pl.pielat.toFix.FileBasedTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class UtilsTests
{
    @Test
    public void ccwRadialSort1()
    {
        Coordinate pivot = Coordinate.newInstance(0, 0);

        List<Place> expected = Arrays.asList(
            makePlace(1, 0),
            makePlace(2, 1),
            makePlace(1,1),
            makePlace(0, 1),
            makePlace(-1, 1),
            makePlace(-1, 0),
            makePlace(-1, -1),
            makePlace(-1, -2),
            makePlace(0, -1),
            makePlace(1, -1)
        );

        List<Place> actual = new ArrayList<>(expected);
        Collections.shuffle(actual);
        Collections.sort(actual, new RadialPlaceSweepComparator(pivot));

        for (int i = 0; i < expected.size(); i++)
            Assert.assertSame(actual.get(i), expected.get(i));
    }

    @Test
    public void ccwRadialSort2()
    {
        Coordinate pivot = Coordinate.newInstance(0.07, 0.03);

        List<Place> expected = Arrays.asList(
            makePlace(2,1),
            makePlace(1, 2),
            makePlace(-1, 2),
            makePlace(-2, 1),
            makePlace(-2, -1),
            makePlace(-1, -2),
            makePlace(1, -2),
            makePlace(2, -1)
        );

        List<Place> actual = new ArrayList<>(expected);
        Collections.shuffle(actual);
        Collections.sort(actual, new RadialPlaceSweepComparator(pivot));

        for (int i = 0; i < expected.size(); i++)
            Assert.assertSame(actual.get(i), expected.get(i));
    }

    private Place makePlace(double x, double y)
    {
        Location location = Location.newInstance(x, y);
        return new Place(location);
    }
}
