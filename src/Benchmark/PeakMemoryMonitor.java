package Benchmark;

/**
 * Samples JVM heap usage during one slow operation. The result is an estimate,
 * not total process or native memory consumption.
 */
public final class PeakMemoryMonitor implements AutoCloseable {

    private final Runtime runtime = Runtime.getRuntime();
    private final long baselineBytes;
    private final Thread samplerThread;
    private volatile boolean running = true;
    private volatile long peakBytes;

    private PeakMemoryMonitor() {
        stabilizeHeap();
        baselineBytes = usedHeapBytes();
        peakBytes = baselineBytes;
        samplerThread = new Thread(this::sampleUntilStopped, "benchmark-memory-sampler");
        samplerThread.setDaemon(true);
        samplerThread.start();
    }

    public static PeakMemoryMonitor start() {
        return new PeakMemoryMonitor();
    }

    private static void stabilizeHeap() {
        System.gc();
        try {
            Thread.sleep(20);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private void sampleUntilStopped() {
        while (running) {
            peakBytes = Math.max(peakBytes, usedHeapBytes());
            try {
                Thread.sleep(1);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return;
            }
        }
        peakBytes = Math.max(peakBytes, usedHeapBytes());
    }

    private long usedHeapBytes() {
        return runtime.totalMemory() - runtime.freeMemory();
    }

    public long getEstimatedPeakDeltaBytes() {
        return Math.max(0, peakBytes - baselineBytes);
    }

    @Override
    public void close() {
        running = false;
        try {
            samplerThread.join(1000);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}
