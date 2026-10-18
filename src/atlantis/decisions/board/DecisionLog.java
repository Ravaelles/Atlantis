package atlantis.decisions.board;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * One JSON row per ISSUED decision, flushed to
 * {@code bwapi-data/write/DECISIONS.log}. Engine-free (the rows arrive as
 * strings) and project-import-free (plain {@code java.io}), so this pins
 * in milliseconds and can never rot a boundary rule.
 *
 * <p>Refusals are deliberately absent: a refused question is silent by
 * contract, and the refusing gates already report through the
 * WORKER_GATE / ORDER_NOT_READY twins. This file answers "what did the
 * bot do and why", not "what did it consider".</p>
 */
public class DecisionLog {

    public static final String OUTPUT_PATH = "bwapi-data/write/DECISIONS.log";

    private static final List<String> rows = new ArrayList<>();
    private static int lastFrame = -1;

    /**
     * Set on the first flush of this JVM run: the file is append-only by
     * transport, but each run owns its file, so the first flush truncates
     * and only later flushes append (NEXT #150 - same fix as EventLog).
     * Statics reset per process; tests reset via {@link #clearForTests}.
     */
    private static boolean firstFlushDone = false;

    private DecisionLog() {
    }

    public static void record(ADecision decision) {
        if (decision == null) return;
        if (!wouldLog(decision)) return;
        if (decision.frame() < lastFrame) rows.clear();
        lastFrame = decision.frame();
        rows.add(toJson(decision));
        flush();
    }

    /**
     * Owner 2026-10-18: importance lives in code, not in post-filtering.
     * Buildings (and research/expansions) with their reasons are the
     * signal - a hundred worker trains per game are noise, and their
     * debugging already lives in the TRAIN_ATTEMPT twin. So only
     * non-train decisions reach the file.
     */
    public static boolean wouldLog(ADecision decision) {
        return decision.kind() != ADecision.Kind.TRAIN_WORKER
            && decision.kind() != ADecision.Kind.TRAIN_ARMY;
    }

    public static void flush() {
        if (rows.isEmpty()) return;
        try {
            if (!firstFlushDone) {
                new java.io.File(OUTPUT_PATH).delete();
                firstFlushDone = true;
            }
            FileWriter writer = new FileWriter(OUTPUT_PATH, true);
            for (String row : rows) {
                writer.write(row + "\n");
            }
            writer.close();
        } catch (IOException ignored) {
        }
        rows.clear();
    }

    static String toJson(ADecision decision) {
        StringBuilder out = new StringBuilder("{");
        out.append("\"frame\":").append(decision.frame());
        out.append(",\"kind\":\"").append(decision.kind().name()).append("\"");
        out.append(",\"subject\":\"").append(quote(decision.subject())).append("\"");
        out.append(",\"reason\":\"").append(quote(decision.reason())).append("\"");
        out.append(",\"source\":\"").append(decision.source().name()).append("\"");
        out.append(",\"supply\":[").append(decision.supplyUsed())
            .append(",").append(decision.supplyTotal()).append("]");
        out.append(",\"bank\":[").append(decision.minerals())
            .append(",").append(decision.gas()).append("]");
        out.append(",\"oid\":").append(decision.orderId() == null ? "null" : decision.orderId());
        out.append(",\"children\":[");
        boolean first = true;
        for (ADecision child : decision.children()) {
            if (!first) out.append(",");
            first = false;
            out.append(toJson(child));
        }
        return out.append("]}").toString();
    }

    public static void clearForTests() {
        rows.clear();
        lastFrame = -1;
        firstFlushDone = false;
    }

    private static String quote(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
