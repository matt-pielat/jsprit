package pl.pielat.util.logging;

public class DummyLogger extends Logger
{
    @Override
    public void log(String format, Object... args)
    {
        // do nothing
    }

    @Override
    public void log(Exception ex)
    {
        // do nothing
    }
}
