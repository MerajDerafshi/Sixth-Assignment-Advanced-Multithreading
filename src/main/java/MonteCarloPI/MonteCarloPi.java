package MonteCarloPI;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class MonteCarloPi {

    static final int NUM_THREADS = Runtime.getRuntime().availableProcessors();
    static final long[] POINT_COUNTS = {10_000_000L, 20_000_000L, 30_000_000L, 40_000_000L, 50_000_000L};

    public static void main(String[] args) throws InterruptedException, ExecutionException, IOException {
        List<String[]> csvResults = new ArrayList<>();
        csvResults.add(new String[]{"NumPoints", "Pi_SingleThread", "Time_SingleThread(ms)", "Pi_MultiThread", "Time_MultiThread(ms)"});

        JSONArray jsonArray = new JSONArray();

        for (long numPoints : POINT_COUNTS) {
            System.out.printf("\n===== Running with %,d points =====\n", numPoints);

            // Single-threaded
            long start = System.nanoTime();
            double pi1 = estimatePiWithoutThreads(numPoints);
            long time1 = (System.nanoTime() - start) / 1_000_000;

            // Multi-threaded
            start = System.nanoTime();
            double pi2 = estimatePiWithThreads(numPoints, NUM_THREADS);
            long time2 = (System.nanoTime() - start) / 1_000_000;

            // Add to CSV
            csvResults.add(new String[]{
                    String.valueOf(numPoints),
                    String.format("%.6f", pi1),
                    String.valueOf(time1),
                    String.format("%.6f", pi2),
                    String.valueOf(time2)
            });

            // Add to JSON
            JSONObject record = new JSONObject();
            record.put("NumPoints", numPoints);
            record.put("Pi_SingleThread", pi1);
            record.put("Time_SingleThread_ms", time1);
            record.put("Pi_MultiThread", pi2);
            record.put("Time_MultiThread_ms", time2);
            jsonArray.put(record);
        }

        // Export CSV
        try (FileWriter writer = new FileWriter("montecarlo_pi_benchmark.csv")) {
            for (String[] row : csvResults) {
                writer.write(String.join(",", row));
                writer.write("\n");
            }
            System.out.println("\n✅ CSV exported to: montecarlo_pi_benchmark.csv");
        }

        // Export JSON
        try (FileWriter jsonWriter = new FileWriter("montecarlo_pi_benchmark.json")) {
            jsonWriter.write(jsonArray.toString(4)); // pretty print
            System.out.println("✅ JSON exported to: montecarlo_pi_benchmark.json");
        }
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
