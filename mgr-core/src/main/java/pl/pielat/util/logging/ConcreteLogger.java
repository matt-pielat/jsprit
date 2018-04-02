package pl.pielat.util.logging;

import java.io.PrintWriter;

public class ConcreteLogger extends Logger
{
    private PrintWriter writer;

    public ConcreteLogger(PrintWriter writer)
    {
        this.writer = writer;
    }

    public void log(String format, Object... args)
    {
        //TODO log date

        writer.format(format, args);
        writer.flush();
    }

    public void log(Exception ex)
    {
        ex.printStackTrace(writer);
        writer.flush();
    }
}
