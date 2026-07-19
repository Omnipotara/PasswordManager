package Benchmark;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Reproducible benchmark settings. The quick profile is intended for smoke
 * testing, while the default profile produces results for analysis.
 */
public final class BenchmarkConfig {

    private static final int[] DEFAULT_INPUT_SIZES = {16, 64, 256, 1024};

    private final Path outputDirectory;
    private final int hashWarmupIterations;
    private final int hashMeasuredIterations;
    private final int encryptionWarmupOperations;
    private final int encryptionMeasuredSamples;
    private final int encryptionOperationsPerSample;
    private final int[] encryptionInputSizes;

    private BenchmarkConfig(
            Path outputDirectory,
            int hashWarmupIterations,
            int hashMeasuredIterations,
            int encryptionWarmupOperations,
            int encryptionMeasuredSamples,
            int encryptionOperationsPerSample,
            int[] encryptionInputSizes) {
        this.outputDirectory = outputDirectory;
        this.hashWarmupIterations = positive(hashWarmupIterations, "hashWarmupIterations");
        this.hashMeasuredIterations = positive(hashMeasuredIterations, "hashMeasuredIterations");
        this.encryptionWarmupOperations = positive(encryptionWarmupOperations, "encryptionWarmupOperations");
        this.encryptionMeasuredSamples = positive(encryptionMeasuredSamples, "encryptionMeasuredSamples");
        this.encryptionOperationsPerSample = positive(encryptionOperationsPerSample, "encryptionOperationsPerSample");
        this.encryptionInputSizes = validateInputSizes(encryptionInputSizes);
    }

    public static BenchmarkConfig fromArgs(String[] args) {
        boolean quick = Arrays.asList(args).contains("--quick");

        Path output = Paths.get("benchmark-results");
        int hashWarmups = quick ? 1 : 3;
        int hashIterations = quick ? 1 : 7;
        int encryptionWarmups = quick ? 50 : 2000;
        int encryptionSamples = quick ? 2 : 15;
        int encryptionOperations = quick ? 100 : 2000;
        int[] inputSizes = DEFAULT_INPUT_SIZES.clone();

        for (String arg : args) {
            if (arg.equals("--quick")) {
                continue;
            }
            if (arg.startsWith("--output=")) {
                output = Paths.get(valueOf(arg));
            } else if (arg.startsWith("--hash-warmups=")) {
                hashWarmups = intValueOf(arg);
            } else if (arg.startsWith("--hash-iterations=")) {
                hashIterations = intValueOf(arg);
            } else if (arg.startsWith("--encryption-warmups=")) {
                encryptionWarmups = intValueOf(arg);
            } else if (arg.startsWith("--encryption-samples=")) {
                encryptionSamples = intValueOf(arg);
            } else if (arg.startsWith("--encryption-operations=")) {
                encryptionOperations = intValueOf(arg);
            } else if (arg.startsWith("--input-sizes=")) {
                inputSizes = parseInputSizes(valueOf(arg));
            } else {
                throw new IllegalArgumentException("Unknown benchmark argument: " + arg);
            }
        }

        return new BenchmarkConfig(
                output,
                hashWarmups,
                hashIterations,
                encryptionWarmups,
                encryptionSamples,
                encryptionOperations,
                inputSizes
        );
    }

    private static String valueOf(String argument) {
        int separator = argument.indexOf('=');
        String value = separator < 0 ? "" : argument.substring(separator + 1).trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("Missing value for benchmark argument: " + argument);
        }
        return value;
    }

    private static int intValueOf(String argument) {
        try {
            return Integer.parseInt(valueOf(argument));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Benchmark argument must be an integer: " + argument, ex);
        }
    }

    private static int[] parseInputSizes(String value) {
        String[] parts = value.split(",");
        int[] sizes = new int[parts.length];
        try {
            for (int index = 0; index < parts.length; index++) {
                sizes[index] = Integer.parseInt(parts[index].trim());
            }
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Input sizes must be comma-separated integers: " + value, ex);
        }
        return sizes;
    }

    private static int positive(int value, String name) {
        if (value <= 0) {
            throw new IllegalArgumentException(name + " must be greater than zero.");
        }
        return value;
    }

    private static int[] validateInputSizes(int[] values) {
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("At least one encryption input size is required.");
        }
        int[] copy = values.clone();
        for (int value : copy) {
            positive(value, "encryptionInputSize");
        }
        Arrays.sort(copy);
        return copy;
    }

    public Path getOutputDirectory() {
        return outputDirectory;
    }

    public int getHashWarmupIterations() {
        return hashWarmupIterations;
    }

    public int getHashMeasuredIterations() {
        return hashMeasuredIterations;
    }

    public int getEncryptionWarmupOperations() {
        return encryptionWarmupOperations;
    }

    public int getEncryptionMeasuredSamples() {
        return encryptionMeasuredSamples;
    }

    public int getEncryptionOperationsPerSample() {
        return encryptionOperationsPerSample;
    }

    public int[] getEncryptionInputSizes() {
        return encryptionInputSizes.clone();
    }
}
