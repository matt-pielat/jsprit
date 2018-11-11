package pl.pielat.algorithm;

import java.util.*;

public abstract class TabuRandomizer<T>
{
    private Random random;
    private ArrayList<Integer> remainingItemIndices;

    public TabuRandomizer(Random random)
    {
        this.random = random;
        remainingItemIndices = new ArrayList<>(getSetSize());
    }

    public T getRandomInstance(boolean ignoreTabu)
    {
        if (ignoreTabu)
        {
            int index = random.nextInt(getSetSize());
            return getItemByIndex(index);
        }
        else
        {
            if (remainingItemIndices.isEmpty())
            {
                for (int i = 0; i < getSetSize(); i++)
                    remainingItemIndices.add(i);
            }
            int index = remainingItemIndices.get(random.nextInt(remainingItemIndices.size()));
            return getItemByIndex(index);
        }
    }

    protected abstract int getSetSize();

    protected abstract T getItemByIndex(int index);
}
