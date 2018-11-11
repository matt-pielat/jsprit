package pl.pielat.heuristic;

import com.graphhopper.jsprit.core.problem.job.Service;

import java.util.Objects;

public class Job extends Place
{
    public final int id;
    public final int demand;
    public final double serviceTime;

    public Job(Service service, int id)
    {
        super(service.getLocation(), service.getTimeWindow());

        this.id = id;
        demand = service.getSize().get(0);
        serviceTime = service.getServiceDuration();
    }

    Job(Job other)
    {
        super(other);

        id = other.id;
        demand = other.demand;
        serviceTime = other.serviceTime;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Job other = (Job)o;
        return id == other.id;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id);
    }
}
