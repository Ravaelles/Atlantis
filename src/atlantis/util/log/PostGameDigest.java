package atlantis.util.log;

import atlantis.util.AConsole;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * End-of-game digest, aggregated in memory while the game runs and saved
 * once at the end (owner order 2026-11-04: Java, no sidecar scripts).
 *
 * <p>Two feeds, both through existing choke points so no call site changes:
 * event twins flow in from {@link EventLog#append} (key + level + params),
 * console lines from {@code AConsole.println/errPrintln} (level + text).
 * Per {@code [LEVEL] KEY} the digest keeps a total count and at most
 * {@link #MAX_SAMPLES_PER_KEY} samples - a thousand identical refusals
 * read as one grouped row with ten witnesses, never as a thousand lines
 * the reader must page through (measured: 309 untagged "Refresh X
 * position" lines plus 162 stack-trace lines in one owner-watched game,
 * all invisible to the severity tooling).</p>
 *
 * <p>Crash-safe by placement, not by speed: the owning calls live in
 * {@code GameSummary.print} (the {@code OnGameEnd} path, which has the
 * whole exit window) and {@code GameSummary.printOnShutdown} (the SIGTERM
 * hook). A SIGKILL still takes the JVM with it - nothing in-JVM survives
 * that - but the periodic twin/ledger files keep the forensic record for
 * those games. The digest file itself is one small JSON write.</p>
 *
 * <p>Same package as {@link EventLog} on purpose: the feed adds no
 * architecture edge (the boundary test pins util.log boundaries), and
 * {@code game.util} stays the only reader (it merges the construction
 * verdict, which lives above this package and is passed in as JSON).</p>
 */
public final class PostGameDigest {

    /**
     * Where the digest lands next to the twins and the ledger, so one
     * directory answers the whole game: what warped (BUILDINGS.json),
     * what happened (EVENTS.jsonl), what mattered (this file).
     */
    public static final String OUTPUT_PATH = "bwapi-data/write/POSTGAME_SUMMARY.json";

    /**
     * Samples kept per key (owner order: no more than ~10 errors of the
     * same type). Counts are unbounded - the cap touches witnesses only,
     * never totals.
     */
    public static final int MAX_SAMPLES_PER_KEY = 10;

    private static final int MAX_SAMPLE_CHARS = 160;

    private static final Map<String, Integer> counts = new TreeMap<>();
    private static final Map<String, Integer> levelTotals = new TreeMap<>();
    private static final Map<String, List<String>> samples = new TreeMap<>();

    /**
     * While the digest prints itself through {@link AConsole}, its own
     * lines must not feed back into the counts (single-threaded game
     * loop, so a plain static is enough; tests reset it).
     */
    private static boolean emitting = false;

    private PostGameDigest() {
    }

    /**
     * One twin row landed. Called from {@link EventLog#append} - the
     * single choke every twin passes through, so no call site changes.
     */
    public static void note(LogKey key, LogLevel level, String sample) {
        if (emitting || key == null || level == null) return;

        add("[" + level.code() + "] " + key.name(), level, sample);
    }

    /**
     * One console line landed. Called from {@code AConsole} - untagged
     * helpers already resolve to a level there, so what arrives is
     * classified. MED/LOW lines count only (chatter volume without
     * witnesses); HIGH/CRIT keep samples, they are the attributable
     * friction.
     */
    public static void noteConsole(LogLevel level, String text) {
        if (emitting || level == null) return;

        String key = "[" + level.code() + "] CONSOLE";
        String sample = (level.atLeast(LogLevel.HIGH) && text != null) ? text : null;
        add(key, level, sample);
    }

    private static void add(String key, LogLevel level, String sample) {
        counts.put(key, counts.getOrDefault(key, 0) + 1);
        levelTotals.put(level.code(), levelTotals.getOrDefault(level.code(), 0) + 1);

        if (sample == null) return;

        List<String> kept = samples.get(key);
        if (kept == null) {
            kept = new ArrayList<>(MAX_SAMPLES_PER_KEY);
            samples.put(key, kept);
        }
        if (kept.size() < MAX_SAMPLES_PER_KEY) {
            kept.add(sample.length() > MAX_SAMPLE_CHARS ? sample.substring(0, MAX_SAMPLE_CHARS) : sample);
        }
    }

    /**
     * How many rows landed under this grouped key. Test seam (pins the
     * grouping without reading the file back).
     */
    public static int countFor(String groupedKey) {
        return counts.getOrDefault(groupedKey, 0);
    }

    /**
     * Samples kept under this grouped key (at most {@link #MAX_SAMPLES_PER_KEY}).
     */
    public static List<String> samplesFor(String groupedKey) {
        List<String> kept = samples.get(groupedKey);

        return kept == null ? new ArrayList<String>() : new ArrayList<>(kept);
    }

    /**
     * The whole digest as compact JSON: level totals, per-key counts,
     * capped samples, plus the construction verdict the caller merged
     * in (warps/off-plan/same-tile dups, computed above this package).
     */
    public static String toJson(String constructionJson) {
        StringBuilder out = new StringBuilder("{\"errors\":{");
        boolean first = true;
        for (String level : new String[]{"HIGH", "MED", "CRIT", "LOW"}) {
            if (!first) out.append(",");
            first = false;
            out.append("\"").append(level).append("\":")
                .append(levelTotals.getOrDefault(level, 0));
        }
        // MEDIUM twins arrive as MED - never as a second bucket.
        out.append("},\"byKey\":{");
        first = true;
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            if (!first) out.append(",");
            first = false;
            out.append("\"").append(escape(entry.getKey())).append("\":").append(entry.getValue());
        }
        out.append("},\"samples\":{");
        first = true;
        for (Map.Entry<String, List<String>> entry : samples.entrySet()) {
            if (!first) out.append(",");
            first = false;
            out.append("\"").append(escape(entry.getKey())).append("\":[");
            boolean innerFirst = true;
            for (String sample : entry.getValue()) {
                if (!innerFirst) out.append(",");
                innerFirst = false;
                out.append("\"").append(escape(sample)).append("\"");
            }
            out.append("]");
        }
        out.append("},\"construction\":");
        out.append(constructionJson == null || constructionJson.isEmpty() ? "{}" : constructionJson);
        out.append("}");

        return out.toString();
    }

    /**
     * The file the agent reads after every game: one JSON object, no
     * paging. Overwrites (same single-file shape as the ledger).
     */
    public static void writeTo(String path, String constructionJson) {
        atlantis.util.AFile.saveToFile(path, toJson(constructionJson) + "\n", true);
    }

    /**
     * The console twin of the file: one header with the totals plus one
     * count line per non-empty key and the kept samples for HIGH/CRIT
     * keys only. Bounded by construction: keys with rows only, at most
     * ten samples each. Never throws: exit paths call this.
     */
    public static void printSummary(int warps, int offPlan, int sameTileDups) {
        try {
            emitting = true;
            int high = levelTotals.getOrDefault("HIGH", 0);
            int med = levelTotals.getOrDefault("MED", 0);
            int crit = levelTotals.getOrDefault("CRIT", 0);
            int low = levelTotals.getOrDefault("LOW", 0);
            String status = (high > 0 || crit > 0 || offPlan > 0 || sameTileDups > 0) ? "issues" : "ok";
            AConsole.println(LogLevel.MEDIUM,
                "===== POSTGAME DIGEST: HIGH=" + high + " MED=" + med
                    + " CRIT=" + crit + " LOW=" + low
                    + " warps=" + warps + " offPlan=" + offPlan
                    + " sameTileDups=" + sameTileDups + " status=" + status + " =====");
            for (Map.Entry<String, Integer> entry : counts.entrySet()) {
                AConsole.println(LogLevel.MEDIUM,
                    "DIGEST " + entry.getKey() + " count=" + entry.getValue());
                if (entry.getKey().startsWith("[HIGH]") || entry.getKey().startsWith("[CRIT]")) {
                    List<String> kept = samples.get(entry.getKey());
                    if (kept != null) {
                        for (String sample : kept) {
                            AConsole.println(LogLevel.MEDIUM, "DIGEST SAMPLE " + entry.getKey() + " " + sample);
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        } finally {
            emitting = false;
        }
    }

    public static void clearForTests() {
        counts.clear();
        levelTotals.clear();
        samples.clear();
        emitting = false;
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    /**
     * Compact witness rendering for twin rows (test seam: pins the
     * shape once, the callers pass params through).
     */
    static String sampleOf(String keyName, Map<String, Object> params) {
        String text = keyName + " " + (params == null ? "" : params.toString());

        return text.length() > MAX_SAMPLE_CHARS ? text.substring(0, MAX_SAMPLE_CHARS) : text;
    }

    /**
     * Test seam: proves the digest's own console section never feeds
     * back into the counts.
     */
    static boolean isEmittingForTest() {
        return emitting;
    }
}
