package pl.pielat.algorithm;

import com.graphhopper.jsprit.core.problem.job.Delivery;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class MgrRoute implements Iterable<Delivery>
{
    private ArrayList<Delivery> jobList;

    public MgrRoute()
    {
        jobList = new ArrayList<>();
    }

    public MgrRoute(Delivery job)
    {
        jobList = new ArrayList<>();
        jobList.add(job);
    }

    public MgrRoute(int initialCapacity)
    {
        jobList = new ArrayList<>(initialCapacity);
    }

    public MgrRoute(MgrRoute other)
    {
        jobList = new ArrayList<>(other.jobList);
    }

    public MgrRoute(List<Delivery> jobs)
    {
        jobList = new ArrayList<>(jobs);
    }

    public Delivery getFirst()
    {
        return jobList.get(0);
    }

    public Delivery getLast()
    {
        return jobList.get(jobList.size() - 1);
    }

    public Delivery removeLast() { return jobList.remove(jobList.size() - 1);}

    public Delivery removeAt(int index)
    {
        return jobList.remove(index);
    }

    public MgrRoute removeSubroute(int index, int length)
    {
        MgrRoute subroute = new MgrRoute(length);
        for (int i = 0; i < length; i++)
            subroute.addToEnd(removeAt(index));
        return subroute;
    }

    public void replace(MgrRoute other)
    {
        jobList = other.jobList;
    }

    public int length()
    {
        return jobList.size();
    }

    public void addToFront(MgrRoute other)
    {
        jobList.addAll(0, other.jobList);
    }

    public void addToEnd(MgrRoute other)
    {
        jobList.addAll(other.jobList);
    }

    public void addToFront(Delivery delivery) { addAt(delivery, 0);}

    public void addToEnd(Delivery delivery)
    {
        jobList.add(delivery);
    }

    public void addAt(Delivery delivery, int index)
    {
        jobList.add(index, delivery);
    }

    public void addAt(MgrRoute route, int index)
    {
        jobList.addAll(index, route.jobList);
    }

    public void reverse()
    {
        Collections.reverse(jobList);
    }

    public Delivery get(int index)
    {
        return jobList.get(index);
    }

    public VehicleRoute toVehicleRoute(Vehicle vehicle)
    {
        VehicleRoute.Builder builder = VehicleRoute.Builder
            .newInstance(vehicle);

        for (Delivery d : jobList)
            builder.addDelivery(d);

        return builder.build();
    }

    @Override
    public Iterator<Delivery> iterator()
    {
        return jobList.iterator();
    }
}
