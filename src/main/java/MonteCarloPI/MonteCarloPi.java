package MonteCarloPI;

import java.util.Random;
import java.util.concurrent.*;

public class MonteCarloPi {

    static final long NUM_POINTS = 50_000_000L;
    static final int NUM_THREADS = Runtime.getRuntime().availableProcessors();

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        // Without Threads
        System.out.println("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("Single threaded calculation started: ");
        long startTime = System.nanoTime();
        double piWithoutThreads = estimatePiWithoutThreads(NUM_POINTS);
        long endTime = System.nanoTime();
        System.out.println("Monte Carlo Pi Approximation (single thread): " + piWithoutThreads);
        System.out.println("Time taken (single thread): " + (endTime - startTime) / 1_000_000 + " ms");
        System.out.println("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        // With Threads
        System.out.printf("Multi threaded calculation started: (your device has %d logical threads)\n", NUM_THREADS);
        startTime = System.nanoTime();
        double piWithThreads = estimatePiWithThreads(NUM_POINTS, NUM_THREADS);
        endTime = System.nanoTime();
        System.out.println("Monte Carlo Pi Approximation (multi-threaded): " + piWithThreads);
        System.out.println("Time taken (multi-threaded): " + (endTime - startTime) / 1_000_000 + " ms");
        System.out.println("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");

    }

    public static double estimatePiWithoutThreads(long numPoints) {
        Random random = new Random();
        long insideCircle = 0;

        for (long i = 0; i < numPoints; i++) {
            double x = 2 * random.nextDouble() - 1;
            double y = 2 * random.nextDouble() - 1;
            if (x * x + y * y <= 1) {
                insideCircle++;
            }
        }

        return 4.0 * insideCircle / numPoints;
    }

    public static double estimatePiWithThreads(long numPoints, int numThreads) throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        long pointsPerThread = numPoints / numThreads;
        Future<Long>[] results = new Future[numThreads];

        for (int i = 0; i < numThreads; i++) {
            results[i] = executor.submit(() -> {
                Random random = new Random();
                long inside = 0;
                for (long j = 0; j < pointsPerThread; j++) {
                    double x = 2 * random.nextDouble() - 1;
                    double y = 2 * random.nextDouble() - 1;
                    if (x * x + y * y <= 1) {
                        inside++;
                    }
                }
                return inside;
            });
        }

        long totalInside = 0;
        for (Future<Long> result : results) {
            totalInside += result.get();
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);

        return 4.0 * totalInside / numPoints;
    }
}
