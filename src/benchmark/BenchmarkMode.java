package benchmark;

import atlantis.Atlantis;
import atlantis.config.ActiveMap;
import atlantis.debug.painter.APainter;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.game.GameSpeed;
import atlantis.map.AMap;

import java.io.FileWriter;
import java.io.IOException;

public class BenchmarkMode {
    private static final String BENCHMARK_CSV = "benchmark_results.csv";
    private static final String BENCHMARK_STATE_FILE = "_benchmarkState";

    public static boolean detectBenchmarkMode(String[] args) {
        boolean isBenchmarkMode = false;

        for (String arg : args) {
            if (arg.equals("--benchmark")) {
                isBenchmarkMode = true;
            }
        }

        // If running in benchmark mode, override the map
        if (isBenchmarkMode) {
            initBenchmarkIfFirstRun();
        }

        return isBenchmarkMode;
    }

    private static void initBenchmarkIfFirstRun() {
        if (!A.fileExists(BENCHMARK_STATE_FILE)) {
            // First run - initialize state file
            try {
                // Create state file to track this is an ongoing benchmark session
                try (FileWriter writer = new FileWriter(BENCHMARK_STATE_FILE)) {
                    writer.write("1"); // Just create the file
                }
            } catch (IOException e) {
                System.err.println("Failed to initialize benchmark: " + e.getMessage());
            }
        }
    }

    public static void onGameEnd(boolean winner) {
        BenchmarkResult result = createBenchmarkResult(winner);
        saveResultToCsv(result);
    }

    public static void onGameStarted() {
        APainter.disablePainting();
        GameSpeed.changeSpeedTo(0);
        GameSpeed.changeFrameSkipTo(120);
    }

    private static void saveResultToCsv(BenchmarkResult result) {
        try (FileWriter writer = new FileWriter(BENCHMARK_CSV, true)) {
            writer.write(result.toCsv() + "\n");
        } catch (IOException e) {
            System.err.println("Failed to write benchmark result: " + e.getMessage());
        }
    }

    // No need to check if this is the last map since the batch file handles opening the CSV

    private static BenchmarkResult createBenchmarkResult(boolean winner) {
        return new BenchmarkResult(winner);
    }
}
