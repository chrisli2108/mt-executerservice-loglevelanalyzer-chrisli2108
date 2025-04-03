package tools;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.Callable;

public class LogAnalyzerTask implements Callable<Map<String, Object>> {
    private final Path logFile;

    public LogAnalyzerTask(Path logFile) {
        this.logFile = logFile;
    }

    @Override
    public Map<String, Object> call() throws Exception {
        Map<String, Integer> logLevelCounts = new HashMap<>();
        List<String> errorAndWarnLines = new ArrayList<>();
        Map<String, Integer> errorTypeCounts = new HashMap<>();

        try (BufferedReader reader = Files.newBufferedReader(logFile)) {
            String line;
            while ((line = reader.readLine()) != null) {
                for (String level : new String[]{"TRACE", "DEBUG", "INFO", "WARN", "ERROR"}) {
                    if (line.contains(level)) {
                        logLevelCounts.put(level, logLevelCounts.getOrDefault(level, 0) + 1);
                        if (level.equals("WARN") || level.equals("ERROR")) {
                            errorAndWarnLines.add(line);
                            extractErrorTypes(line, errorTypeCounts);
                        }
                        break;
                    }
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("logCounts", logLevelCounts);
        result.put("errorLines", errorAndWarnLines);
        result.put("errorTypes", errorTypeCounts);
        return result;
    }

    private void extractErrorTypes(String line, Map<String, Integer> errorTypeCounts) {
        String[] errorKeywords = {"NullPointerException", "FileNotFoundException", "SQLException"};
        for (String keyword : errorKeywords) {
            if (line.contains(keyword)) {
                errorTypeCounts.put(keyword, errorTypeCounts.getOrDefault(keyword, 0) + 1);
            }
        }
    }
}
