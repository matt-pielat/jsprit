package pl.pielat.program;

import com.beust.jcommander.*;
import pl.pielat.algorithm.AlgorithmType;
import pl.pielat.util.problemParsing.FileFormatType;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class BenchmarkRunnerArgs
{
    @Parameter(
        names = {"--problemPath"},
        description = "Path to the VRP instance",
        converter = FileConverter.class,
        validateWith = ExistingFileValidator.class,
        required = true)
    public File problemFile;

    @Parameter(
        names = {"--logPath"},
        description = "Path of the file to write logs to",
        converter = FileConverter.class,
        validateWith = NotDirectoryValidator.class)
    public File logFile;

    @Parameter(
        names = {"--diagnosticLogPath"},
        description = "Path of the file to write diagnostic data to",
        converter = FileConverter.class,
        validateWith = NotDirectoryValidator.class)
    public File diagnosticLogFile;

    @Parameter(
        names = {"--solutionPath"},
        description = "Path of the output solution file",
        converter = FileConverter.class,
        validateWith = NotDirectoryValidator.class,
        required = true)
    public File solutionFile;

    @Parameter(
        names = {"--problemFormat"},
        description = "Format of the VRP file",
        converter = FileFormatTypeConverter.class,
        validateWith = FileFormatTypeValidator.class,
        required = true)
    public FileFormatType problemFileFormat;

    @Parameter(
        names = {"--algorithm"},
        description = "VRP algorithm used to solve the problem",
        converter = AlgorithmTypeConverter.class,
        validateWith = AlgorithmTypeValidator.class,
        required = true)
    public AlgorithmType algorithmType;

    @Parameter(
        names = {"--populationSize"},
        description = "Population size in Garrido-Riff algorithm",
        validateWith = PositiveNumberValidator.class)
    public int populationSize;

    @Parameter(
        names = {"--offspringSize"},
        description = "Offspring size in Garrido-Riff algorithm",
        validateWith = PositiveNumberValidator.class)
    public int offspringSize;

    @Parameter(
        names = {"--chromosomeSize"},
        description = "Starting chromosome size in Garrido-Riff algorithm",
        validateWith = PositiveNumberValidator.class)
    public int chromosomeSize;

    @Parameter(
        names = {"--timeLimit"},
        description = "Algorithm runtime limit in milliseconds",
        validateWith = PositiveNumberValidator.class)
    public long timeLimitInMs;

    @Parameter(
        names = {"--iterationLimit"},
        description = "Algorithm iteration limit",
        validateWith = PositiveNumberValidator.class)
    public int iterationLimit;

    @Parameter(
        names = {"--minIntermediateCostDelay"},
        description = "Minimum time in milliseconds between intermediate cost writes",
        validateWith = PositiveNumberValidator.class)
    public long minIntermediateCostDelay = -1;

    public static BenchmarkRunnerArgs parse(String[] rawArgs) throws ParameterException
    {
        BenchmarkRunnerArgs args = new BenchmarkRunnerArgs();

        JCommander.newBuilder()
            .addObject(args)
            .build()
            .parse(rawArgs);

        if (args.offspringSize > args.populationSize)
            throw new ParameterException("Offspring size can not be greater than population size");

        if (args.timeLimitInMs == 0 && args.iterationLimit == 0)
            throw new ParameterException("Either timeInMs limit or iteration limit has to be provided");

        return args;
    }

    public static class FileConverter implements IStringConverter<File>
    {
        @Override
        public File convert(String s)
        {
            return new File(s);
        }
    }

    public static class FileFormatTypeConverter implements IStringConverter<FileFormatType>
    {
        @Override
        public FileFormatType convert(String s) throws ParameterException
        {
            String l = s.toLowerCase();
            switch (l)
            {
                case "solomon":
                    return FileFormatType.Solomon;
                case "tsplib95":
                    return FileFormatType.Tsplib95;
            }
            return FileFormatType.Unknown;
        }
    }

    public static class AlgorithmTypeConverter implements IStringConverter<AlgorithmType>
    {
        @Override
        public AlgorithmType convert(String s) throws ParameterException
        {
            String l = s.toLowerCase();
            switch (l)
            {
                case "jsprit":
                    return AlgorithmType.Jsprit;
                case "garrido-riff":
                case "garridoriff":
                case "garrido":
                    return AlgorithmType.GarridoRiff;
            }
            return AlgorithmType.Unknown;
        }
    }

    public static class FileFormatTypeValidator implements IParameterValidator
    {
        @Override
        public void validate(String name, String value) throws ParameterException
        {
            FileFormatTypeConverter converter = new FileFormatTypeConverter();
            FileFormatType type = converter.convert(value);

            if (type == FileFormatType.Unknown)
                throw new ParameterException("Parameter " + name + " has incorrect VRP file format type " + value);
        }
    }

    public static class AlgorithmTypeValidator implements IParameterValidator
    {
        @Override
        public void validate(String name, String value) throws ParameterException
        {
            AlgorithmTypeConverter converter = new AlgorithmTypeConverter();
            AlgorithmType type = converter.convert(value);

            if (type == AlgorithmType.Unknown)
                throw new ParameterException("Parameter " + name + " has incorrect algorithm type " + value);
        }
    }

    public static class ExistingFileValidator implements IParameterValidator
    {
        @Override
        public void validate(String name, String value) throws ParameterException
        {
            Path path = Paths.get(value);

            if (!Files.exists(path))
                throw new ParameterException("Parameter " + name + " points to a non-existing file " + value);
            if (!Files.isRegularFile(path))
                throw new ParameterException("Parameter " + name + " points to a non-regular file " + value);
        }
    }

    public static class NotDirectoryValidator implements IParameterValidator
    {
        @Override
        public void validate(String name, String value) throws ParameterException
        {
            Path path = Paths.get(value);

            if (!Files.exists(path))
                return;
            if (!Files.isRegularFile(path))
                throw new ParameterException("Parameter " + name + " points to a non-regular file " + value);
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
}
