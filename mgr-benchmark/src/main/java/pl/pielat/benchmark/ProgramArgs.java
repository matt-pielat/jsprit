package pl.pielat.benchmark;

import com.beust.jcommander.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ProgramArgs
{
    private class FileConverter implements IStringConverter<File>
    {
        @Override
        public File convert(String s)
        {
            return new File(s);
        }
    }

    private class ExistingDirectoryValidator implements IParameterValidator
    {
        @Override
        public void validate(String name, String value) throws ParameterException
        {
            Path path = Paths.get(value);

            if (!Files.exists(path) && !Files.isDirectory(path))
                throw new ParameterException("Parameter " + name + " points to a non-existing directory");
        }
    }

    private class NotRegularFileValidator implements IParameterValidator
    {
        @Override
        public void validate(String name, String value) throws ParameterException
        {
            Path path = Paths.get(value);

            if (Files.exists(path) && Files.isRegularFile(path))
                throw new ParameterException("Parameter " + name + " points to a regular file");
        }
    }

    private class PositiveNumberValidator implements IParameterValidator
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
        names = {"--jsprit", "-j"},
        description = "Use default jsprit algoritm")
    public boolean useJsprit;

    @Parameter(
        names = {"--garridoRiff", "-gr"},
        description = "Use Garrido-Riff algorithm")
    public boolean useGarridoRiff;

    @Parameter(
        names = {"--timeWindows", "-tw"},
        description = "Input directory contains VRPTW problem files")
    public boolean timeWindows;

    @Parameter(
        names = {"--inputDir", "-i"},
        description = "Input (problem) directory",
        converter = FileConverter.class,
        validateWith = ExistingDirectoryValidator.class,
        required = true)
    public File problemDirectory;

    @Parameter(
        names = {"--outputDir", "-o"},
        description = "Output (solution) directory",
        converter = FileConverter.class,
        validateWith = NotRegularFileValidator.class,
        required = true)
    public File solutionDirectory;

    @Parameter(
        names = {"--logDir", "-l"},
        description = "Log files directory",
        converter = FileConverter.class,
        validateWith = NotRegularFileValidator.class,
        required = true)
    public File logDirectory;

    @Parameter(
        names = {"--timeLimit", "-t"},
        description = "Time limit per problem run in milliseconds",
        validateWith = PositiveNumberValidator.class,
        required = true)
    public long timeThresholdInMs;

    @Parameter(
        names = {"--runs", "-r"},
        description = "Runs per problem instance",
        validateWith = PositiveNumberValidator.class,
        required = true)
    public int runsPerProblem;

    public static ProgramArgs parse(String[] rawArgs) throws ParameterException
    {
        ProgramArgs args = new ProgramArgs();

        JCommander.newBuilder()
            .addObject(args)
            .build()
            .parse(rawArgs);

        if (!args.useJsprit && !args.useGarridoRiff)
            throw new ParameterException("No algorithm was chosen");

        return args;
    }
}
