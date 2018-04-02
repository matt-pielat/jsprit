package pl.pielat.util.logging;

public abstract class Logger
{
    public abstract void log(String format, Object... args);

    public abstract void log(Exception ex);
}
