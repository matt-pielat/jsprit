package pl.pielat.util;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.util.Coordinate;
import org.junit.Assert;
import org.junit.Test;
import pl.pielat.toFix.FileBasedTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class UtilsTests extends FileBasedTest
{
    @Test
    public void ccwRadialSort1()
    {
        Coordinate pivot = Coordinate.newInstance(0, 0);

        List<Service> expected = Arrays.asList(
            makeJob(1, 0),
            makeJob(2, 0),
            makeJob(1,1),
            makeJob(0, 1),
            makeJob(-1, 1),
            makeJob(-1, 0),
            makeJob(-1, -1),
            makeJob(-2, -2),
            makeJob(0, -1),
            makeJob(1, -1)
        );

        List<Service> actual = new ArrayList<>(expected);
        Collections.shuffle(actual);
        Collections.sort(actual, new RadialJobComparator(pivot));

        for (int i = 0; i < expected.size(); i++)
            Assert.assertSame(actual.get(i), expected.get(i));
    }

    @Test
    public void ccwRadialSort2()
    {
        Coordinate pivot = Coordinate.newInstance(0.07, 0.03);

        List<Service> expected = Arrays.asList(
            makeJob(2,1),
            makeJob(1, 2),
            makeJob(-1, 2),
            makeJob(-2, 1),
            makeJob(-2, -1),
            makeJob(-1, -2),
            makeJob(1, -2),
            makeJob(2, -1)
        );

        List<Service> actual = new ArrayList<>(expected);
        Collections.shuffle(actual);
        Collections.sort(actual, new RadialJobComparator(pivot));

        for (int i = 0; i < expected.size(); i++)
            Assert.assertSame(actual.get(i), expected.get(i));
    }

    private Service makeJob(double x, double y)
    {
       Location location = Location.newInstance(x, y);
       return Service.Builder.newInstance("")
           .setLocation(location)
           .build();
    }
}
