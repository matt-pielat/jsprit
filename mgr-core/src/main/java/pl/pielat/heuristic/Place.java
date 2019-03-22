package pl.pielat.heuristic;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;

public class Place
{
    public final Location location;
    public final double windowStart;
    public final double windowEnd; // the latest timeInMs of arrival (NOT departure)

    public Place(Location location, TimeWindow timeWindow)
    {
        this.location = location;
        windowStart = timeWindow.getStart();
        windowEnd = timeWindow.getEnd();
    }

    public Place(Location location)
    {
        this(location, TimeWindow.newInstance(0.0, Double.MAX_VALUE));
    }

    Place(Place other)
    {
        location = other.location;
        windowStart = other.windowStart;
        windowEnd = other.windowEnd;
    }
}
