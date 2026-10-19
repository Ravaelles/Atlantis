package atlantis.debug.tweaker;

import atlantis.Atlantis;
import atlantis.config.env.Env;
import atlantis.game.A;

import atlantis.util.AFile;
import atlantis.util.log.EventLog;
import atlantis.util.log.LogKey;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.stream.Stream;

public class ParamTweakerEvaluator {

    private static final String DELIMITER = ";";

    /** Fixed-name copy of the last simulation's row only (overwritten
     * every game, never appended) - the dated files stay the archive. */
    private static final String LAST_FILE = "param_tweaker/__tweaker.csv";

    public static void updateOnEnd(boolean winner) {
        saveScoreToFile(winner);
    }

    // =========================================================

    private static void saveScoreToFile(boolean winner) {
        String file = "param_tweaker/" + dateString() + ".csv";

        if (Env.isFirstRun()) {
            AFile.removeFile(file);
        }
        java.io.File parent = new java.io.File(file).getParentFile();
        if (parent != null) parent.mkdirs();

        // Whole-game totals come from the flushed file, never the buffer:
        // the periodic 720-frame flush clears the buffer, so buffer counts
        // at game end are only the post-flush remnant (measured: 7 counted
        // vs 33 filed SHOT rows). Flush first so the remnant lands too.
        EventLog.flush();
        int[] totals = totalsFromFile();
        int hits = totals != null ? totals[0] : EventLog.countByKey(LogKey.HIT);
        int shots = totals != null ? totals[1] : EventLog.countByKeyParam(LogKey.SHOT, "result", "shot-fired");

        String row = resultString(winner, hits, shots);
        AFile.writeToFileWithHeader(file, row, fileHeader());

        // Fixed-name copy of the last simulation only (owner: always
        // know the latest result without dating files). Overwritten
        // every game, never appended.
        AFile.removeFile(LAST_FILE);
        AFile.writeToFileWithHeader(LAST_FILE, row, fileHeader());
    }

    private static String[] fileHeader() {
        String[] base = {
                "Date",
                "W/L",
                "Seconds",
                "Killed",
                "Lost",
                "HitsTaken",
                "ShotsFired",
                "Score"
        };
        return Stream.concat(Arrays.stream(base), Arrays.stream(paramNames())).toArray(String[]::new);
    }

    private static String[] paramNames() {
        ArrayList<String> list = new ArrayList<>();
        ParamTweaker tweaker = ParamTweaker.get();
        for (Param param : tweaker.params()) {
            list.add(param.name());
        }

        String[] stringArray = list.toArray(new String[0]);
        return stringArray;
    }

    private static String paramsString() {
        StringBuilder string = new StringBuilder();

        ParamTweaker tweaker = ParamTweaker.get();
        for (Param param : tweaker.params()) {
            if (string.length() > 0) {
                string.append(DELIMITER);
            }
            string.append(param.getterValue());
        }

        return string.toString();
    }

    /**
     * Whole-game HIT / fired-SHOT totals read back from the flushed
     * EVENTS file (null when the file is absent, e.g. unit tests - the
     * caller falls back to buffer counts). Substring match, not JSON:
     * prod ships no JSON parser and the lines are machine-written by
     * {@code EventLog.toJsonLines} - the quoted key/result tokens only
     * occur in their own fields.
     */
    private static int[] totalsFromFile() {
        java.io.File events = new java.io.File(EventLog.OUTPUT_PATH);
        if (!events.exists()) return null;
        java.util.ArrayList<String> lines = AFile.readTextFileToList(EventLog.OUTPUT_PATH);
        int hits = 0;
        int shots = 0;
        for (String line : lines) {
            if (line.contains("\"key\":\"HIT\"")) hits++;
            else if (line.contains("\"key\":\"SHOT\"") && line.contains("\"result\":\"shot-fired\"")) shots++;
        }
        return new int[] { hits, shots };
    }

    private static String resultString(boolean winner, int hits, int shots) {
        return timeString() + DELIMITER
                + (winner ? "W" : "L") + DELIMITER
                + A.seconds() + DELIMITER
                + Atlantis.KILLED + DELIMITER
                + Atlantis.LOST + DELIMITER
                + hits + DELIMITER
                + shots + DELIMITER
                + score(winner) + DELIMITER
                + paramsString();
    }

    private static int score(boolean winner) {
        int score = winner ? 0 : -500;

        score += A.resourcesBalance();

        score += Atlantis.KILLED * 10;
        score -= Atlantis.LOST * 40;

        score -= A.seconds();

        return score;
    }

    private static String dateString() {
        Date date = new Date();
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }

    private static String timeString() {
        Date date = new Date();
        return new SimpleDateFormat("HH:mm").format(date);
    }

}
