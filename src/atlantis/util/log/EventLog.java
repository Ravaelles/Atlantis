package atlantis.util.log;

import atlantis.util.AFile;

import java.util.*;

/**
 * Structured diagnostic events, buffered and written as JSON lines.
 *
 * <p>One row per event: {@code {"frame":F,"time":"m:ss","key":"WORKER_GATE",
 * "reason":"CantAfford",...}}. Keys are {@link LogKey} families, parameters
 * carry the variance - so a thousand refusals that differ in two numbers are
 * a filter, not a grep. The console lines stay byte-identical next to these
 * calls; this file is the queryable twin, not a replacement.</p>
 *
 * <p>Engine-free on purpose: frame and time arrive as primitives (this
 * package may not import the game layer), so rendering pins in unit tests.
 * Flush is periodic, not end-only: an interrupted game never reaches
 * {@code OnGameEnd}, and that is exactly when the rows are wanted. Buffer
 * clears on delivery, so memory stays bounded by the flush interval.</p>
 */
public class EventLog {

    public static final String OUTPUT_PATH = "bwapi-data/write/EVENTS.jsonl";

    /**
     * ~30 game-seconds between flushes: fresh enough to survive a kill.
     */
    public static final int FLUSH_EVERY_FRAMES = 720;

    private static final List<Row> buffer = new ArrayList<>();
    /**
     * Paths already truncated by this JVM run. The files are append-only
     * by transport, but each run owns its file: the first flush of a JVM
     * run truncates, later flushes append. Statics reset per process, so
     * one game can never inherit another game's rows no matter which
     * launcher started it (NEXT #150: 7 MB EVENTS.jsonl spanning 6 Wine
     * games made per-game attribution impossible).
     */
    private static final Set<String> truncatedPaths = new HashSet<>();
    private static int lastFrame = -1;
    private static int lastFlushFrame = -1;

    private EventLog() {
    }

    public static Builder key(LogKey key) {
        return new Builder(key);
    }

    private static void append(LogKey key, int frame, String time, LinkedHashMap<String, Object> params) {
        if (frame < lastFrame) {
            buffer.clear();
            lastFlushFrame = -1;
        }
        lastFrame = frame;
        buffer.add(new Row(key, frame, time, key.level(), new LinkedHashMap<>(params)));
        if (flushDue(frame, lastFlushFrame)) flush();
    }

    /**
     * How many buffered rows carry this key (tuning scoreboard reads hits off the twins).
     */
    public static int countByKey(LogKey key) {
        int count = 0;
        for (Row row : buffer) {
            if (row.key == key) count++;
        }
        return count;
    }

    /**
     * How many buffered rows carry this key with a param equal to the given value.
     */
    public static int countByKeyParam(LogKey key, String param, Object value) {
        int count = 0;
        for (Row row : buffer) {
            if (row.key == key && value.equals(row.params.get(param))) count++;
        }
        return count;
    }

    public static boolean flushDue(int frame, int lastFlush) {
        return frame - lastFlush >= FLUSH_EVERY_FRAMES;
    }

    public static void flush() {
        flushTo(OUTPUT_PATH);
    }

    public static void flushTo(String path) {
        if (buffer.isEmpty()) return;

        if (!truncatedPaths.contains(path)) {
            AFile.removeFile(path);
            truncatedPaths.add(path);
        }
        AFile.appendToFile(path, toJsonLines());
        buffer.clear();
        lastFlushFrame = lastFrame;
    }

    public static String toJsonLines() {
        StringBuilder out = new StringBuilder();
        for (Row row : buffer) {
            out.append("{\"frame\":").append(row.frame)
                .append(",\"time\":\"").append(escape(row.time)).append("\"")
                .append(",\"key\":\"").append(row.key.name()).append("\"")
                .append(",\"lvl\":\"").append(row.level.code()).append("\"");
            for (Map.Entry<String, Object> param : row.params.entrySet()) {
                out.append(",\"").append(escape(param.getKey())).append("\":");
                Object value = param.getValue();
                if (value instanceof String) out.append("\"").append(escape((String) value)).append("\"");
                else out.append(value);
            }
            out.append("}\n");
        }
        return out.toString();
    }

    public static void clearForTests() {
        buffer.clear();
        lastFrame = -1;
        lastFlushFrame = -1;
        truncatedPaths.clear();
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    /**
     * Frame and time come from the caller: this package cannot read the clock.
     */
    public static final class Builder {
        private final LogKey key;
        private final LinkedHashMap<String, Object> params = new LinkedHashMap<>();

        private Builder(LogKey key) {
            this.key = key;
        }

        public Builder param(String name, String value) {
            params.put(name, value == null ? "-" : value);

            return this;
        }

        public Builder param(String name, int value) {
            params.put(name, value);

            return this;
        }

        /**
         * Nullable ids (construction, order): present as a number, absent
         * as JSON null - the chain twin that joins REQUESTED / PREPARING /
         * STARTED / CANCEL / ledger rows by cid+oid.
         */
        public Builder param(String name, Integer value) {
            params.put(name, value);

            return this;
        }

        public Builder param(String name, boolean value) {
            params.put(name, value);

            return this;
        }

        public void log(int frame, String time) {
            EventLog.append(key, frame, time, params);
        }
    }

    private static final class Row {
        private final LogKey key;
        private final int frame;
        private final String time;
        private final LogLevel level;
        private final LinkedHashMap<String, Object> params;

        private Row(LogKey key, int frame, String time, LogLevel level, LinkedHashMap<String, Object> params) {
            this.key = key;
            this.frame = frame;
            this.time = time;
            this.level = level;
            this.params = params;
        }
    }
}
