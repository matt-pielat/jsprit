package pl.pielat.util.logging;

public class Multilogger extends Logger
{
    private Logger[] loggers;

    public Multilogger(Logger... loggers)
    {
        this.loggers = loggers;
    }


    @Override
    public void log(String format, Object... args)
    {
        for (Logger l : loggers)
        {
            l.log(format, args);
        }
    }

    @Override
    public void log(Exception ex)
    {
        for (Logger l : loggers)
        {
            l.log(ex);
        }
    }
}
