package pl.pielat.algorithm;

import java.util.*;

public abstract class TabuRandomizer<T>
{
    protected abstract List<String> getAllIds();

    protected abstract T getInstanceById(String id) throws Exception;

    private List<String> validIds;
    private Random random;
    private int setSize;

    public TabuRandomizer(Random random)
    {
        this.random = random;

        setSize = getAllIds().size();
        validIds = new ArrayList<>(setSize);
    }

    public T getRandomInstance(boolean ignoreTabu)
    {
        String id;
        if (ignoreTabu)
        {
            id = getAllIds().get(random.nextInt(setSize));
        }
        else
        {
            if (validIds.isEmpty())
                validIds.addAll(getAllIds());
            id = validIds.remove(random.nextInt(validIds.size()));
        }

        try
        {
            return getInstanceById(id);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }
}
