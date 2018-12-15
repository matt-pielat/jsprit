package pl.pielat.benchmark;

import com.beust.jcommander.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ProgramArgs
{
    public static class FileConverter implements IStringConverter<File>
    {
        @Override
        public File convert(String s)
        {
            return new File(s);
        }
    }

    public static class ExistingDirectoryValidator implements IParameterValidator
    {
        @Override
        public void validate(String name, String value) throws ParameterException
        {
            Path path = Paths.get(value);

            if (!Files.exists(path) && !Files.isDirectory(path))
                throw new ParameterException("Parameter " + name + " points to a non-existing directory");
        }
    }

    public static class NotRegularFileValidator implements IParameterValidator
    {
        @Override
        public void validate(String name, String value) throws ParameterException
        {
            Path path = Paths.get(value);

            if (Files.exists(path) && Files.isRegularFile(path))
                throw new ParameterException("Parameter " + name + " points to a regular file");
        }
    }

    public static class PositiveNumberValidator implements IParameterValidator
    {
        @Override
        public void validate(String name, String value) throws ParameterException
        {
            long v = Long.parseLong(value);
            if (v <= 0)
                throw new ParameterException("Parameter" + name + " is not positive");
        }
    }

    @Parameter(
        names = {"--inputDir", "-i"},
        description = "Input (problem) directory",
        converter = FileConverter.class,
        validateWith = ExistingDirectoryValidator.class,
        required = true)
    public File problemDirectory;

    @Parameter(
        names = {"--timeWindows", "-tw"},
        description = "Input directory contains VRPTW problem files")
    public boolean timeWindows;

    @Parameter(
        names = {"--jspritOutDir", "-j"},
        description = "jsprit output directory",
        converter = FileConverter.class,
        validateWith = NotRegularFileValidator.class)
    public File jspritOutputDirectory;

    @Parameter(
        names = {"--garridoRiffOutDir", "-gr"},
        description = "Garrido-Riff output directory",
        converter = FileConverter.class,
        validateWith = NotRegularFileValidator.class)
    public File garridoRiffOutputDirectory;

    @Parameter(
        names = {"--logDir", "-l"},
        description = "Log files directory",
        converter = FileConverter.class,
        validateWith = NotRegularFileValidator.class,
        required = true)
    public File logDirectory;

    @Parameter(
        names = {"--timePerRun", "-t"},
        description = "Time limit per problem run in milliseconds",
        validateWith = PositiveNumberValidator.class,
        required = true)
    public long timePerRunInMs;

    @Parameter(
        names = {"--runsPerProblem", "-r"},
        description = "Runs per problem instance",
        validateWith = PositiveNumberValidator.class,
        required = true)
    public int runsPerProblem;

    @Parameter(
        names = {"--populationSize", "-pops"},
        description = "Population size in Garrido-Riff algorithm",
        validateWith = PositiveNumberValidator.class)
    public int populationSize;

    @Parameter(
        names = {"--offspringSize", "-offs"},
        description = "Offspring size in Garrido-Riff algorithm",
        validateWith = PositiveNumberValidator.class)
    public int offspringSize;

    @Parameter(
        names = {"--chromosomeSize", "-chrs"},
        description = "Starting chromosome size in Garrido-Riff algorithm",
        validateWith = PositiveNumberValidator.class)
    public int chromosomeSize;

    public static ProgramArgs parse(String[] rawArgs) throws ParameterException
    {
        ProgramArgs args = new ProgramArgs();

        JCommander.newBuilder()
            .addObject(args)
            .build()
            .parse(rawArgs);

        if (args.jspritOutputDirectory == null && args.garridoRiffOutputDirectory == null)
            throw new ParameterException("No algorithm was chosen");

        if (args.offspringSize > args.populationSize)
            throw new ParameterException("Offspring size can not be greater than population size");

        return args;
    }
}
