package tools;

import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

public class ParallelLogAnalyzer {
    public static void main(String[] args) {
        List<Path> logFiles = List.of(
                Paths.get("log1.txt"),
                Paths.get("log2.txt"),
                Paths.get("log3.txt")
                // Weitere Logdateien hinzufügen
        );

        int numThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        List<Future<Map<String, Object>>> futures = new ArrayList<>();

        for (Path logFile : logFiles) {
            futures.add(executorService.submit(new LogAnalyzerTask(logFile)));
        }

        Map<String, Integer> totalLogCounts = new HashMap<>();
        List<String> allErrorAndWarnLines = new ArrayList<>();
        Map<String, Integer> totalErrorTypes = new HashMap<>();

        for (Future<Map<String, Object>> future : futures) {
            try {
                Map<String, Object> result = future.get();

                Map<String, Integer> logCounts = (Map<String, Integer>) result.get("logCounts");
                List<String> errorLines = (List<String>) result.get("errorLines");
                Map<String, Integer> errorTypes = (Map<String, Integer>) result.get("errorTypes");

                logCounts.forEach((key, value) -> totalLogCounts.merge(key, value, Integer::sum));
                allErrorAndWarnLines.addAll(errorLines);
                errorTypes.forEach((key, value) -> totalErrorTypes.merge(key, value, Integer::sum));
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        executorService.shutdown();

        System.out.println("Gesamtzusammenfassung der LogLevel:");
        totalLogCounts.forEach((key, value) -> System.out.println(key + ": " + value));

        System.out.println("\nGesammelte Fehler- und Warnmeldungen:");
        allErrorAndWarnLines.stream().limit(20).forEach(System.out::println);

        System.out.println("\nFehlertyp-Analyse:");
        totalErrorTypes.forEach((key, value) -> System.out.println(key + ": " + value));
    }
}
