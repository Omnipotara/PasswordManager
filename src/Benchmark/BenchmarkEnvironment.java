package Benchmark;

import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public final class BenchmarkEnvironment {

    private BenchmarkEnvironment() {
    }

    public static Map<String, String> collect(String runId, BenchmarkConfig config) {
        Runtime runtime = Runtime.getRuntime();
        Map<String, String> values = new LinkedHashMap<>();
        values.put("run_id", runId);
        values.put("started_at_utc", Instant.now().toString());
        values.put("os_name", System.getProperty("os.name"));
        values.put("os_version", System.getProperty("os.version"));
        values.put("os_architecture", System.getProperty("os.arch"));
        values.put("processor", environmentOrUnknown("PROCESSOR_IDENTIFIER"));
        values.put("available_processors", String.valueOf(runtime.availableProcessors()));
        values.put("physical_memory_bytes", String.valueOf(totalPhysicalMemoryBytes()));
        values.put("jvm_max_heap_bytes", String.valueOf(runtime.maxMemory()));
        values.put("java_version", System.getProperty("java.version"));
        values.put("java_vendor", System.getProperty("java.vendor"));
        values.put("jvm_name", System.getProperty("java.vm.name"));
        values.put("jvm_version", System.getProperty("java.vm.version"));
        values.put("timer", "System.nanoTime");
        values.put("libraries", "jBCrypt source; Password4j 1.8.4; Bouncy Castle 1.84; JDK JCA/JCE");
        values.put("hash_warmup_iterations", String.valueOf(config.getHashWarmupIterations()));
        values.put("hash_measured_iterations", String.valueOf(config.getHashMeasuredIterations()));
        values.put("hash_operations_per_sample", "1");
        values.put("hash_memory_measurement", "Estimated JVM heap peak delta sampled every 1 ms after an explicit GC request");
        values.put("encryption_warmup_operations", String.valueOf(config.getEncryptionWarmupOperations()));
        values.put("encryption_measured_samples", String.valueOf(config.getEncryptionMeasuredSamples()));
        values.put("encryption_operations_per_sample", String.valueOf(config.getEncryptionOperationsPerSample()));
        values.put("encryption_input_sizes_bytes", Arrays.toString(config.getEncryptionInputSizes()));
        values.put("encryption_key_derivation", "PBKDF2-HMAC-SHA256 performed once outside timed encryption operations");
        values.put("encryption_memory_measurement", "Not measured; operations are batched for timing stability");
        values.put("algorithm_order", "Round-robin rotation between measured samples");
        return values;
    }

    private static long totalPhysicalMemoryBytes() {
        java.lang.management.OperatingSystemMXBean bean = ManagementFactory.getOperatingSystemMXBean();
        if (bean instanceof com.sun.management.OperatingSystemMXBean) {
            return ((com.sun.management.OperatingSystemMXBean) bean).getTotalMemorySize();
        }
        return -1;
    }

    private static String environmentOrUnknown(String name) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? "unknown" : value;
    }
}
