package atlantis.game.util;

import atlantis.util.AFile;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * One row per own building the moment it appears in the game (warp/morph/
 * completion), with the supply then standing against the supply the build
 * order row (or the dynamic order) asked for.
 *
 * <p>Written through on every record, not once at game end: an interrupted
 * game never reaches {@code OnGameEnd}, and that is exactly when this file
 * is wanted. The file is a plain JSON array, rewritten whole - buildings
 * are dozens per game, so the cost is noise.</p>
 *
 * <p>Engine-free on purpose: every value arrives as a primitive, so this
 * pins in milliseconds and no {@code atlantis.*} import can ever rot a
 * boundary rule. The only seam to the engine is the thin call in
 * {@code OnOurUnitCreated}, after the order fallback ran.</p>
 */
public class BuildingLedger {

    public static final String OUTPUT_PATH = "bwapi-data/write/BUILDINGS.json";

    private static final Map<Integer, Row> rows = new LinkedHashMap<>();
    private static int lastFrame = -1;

    private BuildingLedger() {
    }

    /**
     * A warped/morphed/completed building: type, frame, {@code A.minSec()},
     * supply and bank at that moment, and what the linked order asked for.
     *
     * @param expectedSupply the order's {@code minSupply} ("At 8" of the
     *                       build-order row, or the supply the dynamic order
     *                       was created at), null when no order is linked.
     * @param constructionId the construction this building finished, null
     *                       when the warp matched nothing (the chain twin).
     * @param orderId        the linked order's id, null when orderless.
     */
    public static void recordBuilding(int unitId, String type, int tx, int ty, int frame, String time,
                                      int supplyUsed, int supplyTotal, int minerals, int gas,
                                      int reservedMinerals, int reservedGas,
                                      Integer expectedSupply, String order,
                                      Integer constructionId, Integer orderId) {
        record("building", unitId, type, tx, ty, frame, time, supplyUsed, supplyTotal, minerals, gas,
            reservedMinerals, reservedGas, expectedSupply, order, constructionId, orderId);
    }

    /**
     * A combat unit the build order asked for (Zealot, Dragoon, ...): same
     * columns as buildings, so demand and delivery read from one list.
     * Workers never land here; orderless spawns neither (no demand shown).
     */
    public static void recordUnit(int unitId, String type, int tx, int ty, int frame, String time,
                                  int supplyUsed, int supplyTotal, int minerals, int gas,
                                  int reservedMinerals, int reservedGas,
                                  Integer expectedSupply, String order,
                                  Integer constructionId, Integer orderId) {
        record("unit", unitId, type, tx, ty, frame, time, supplyUsed, supplyTotal, minerals, gas,
            reservedMinerals, reservedGas, expectedSupply, order, constructionId, orderId);
    }

    private static void record(String kind, int unitId, String type, int tx, int ty, int frame, String time,
                               int supplyUsed, int supplyTotal, int minerals, int gas,
                               int reservedMinerals, int reservedGas,
                               Integer expectedSupply, String order,
                               Integer constructionId, Integer orderId) {
        if (frame < lastFrame) rows.clear();
        lastFrame = frame;
        if (rows.containsKey(unitId)) return;

        rows.put(unitId, new Row(kind, type, tx, ty, frame, time, supplyUsed, supplyTotal, minerals, gas,
            reservedMinerals, reservedGas, expectedSupply, order, constructionId, orderId));
    }

    public static void flush() {
        AFile.saveToFile(OUTPUT_PATH, toJson(), true);
    }

    /**
     * Postgame digest seam: how many own buildings warped (any kind row
     * with kind "building"). Pure over the in-memory rows, so the digest
     * never re-reads the file it just wrote.
     */
    public static int buildingWarps() {
        int total = 0;
        for (Row row : rows.values()) {
            if ("building".equals(row.kind)) total++;
        }

        return total;
    }

    /**
     * Postgame digest seam: "Type@[x,y]" per warped building, in warp
     * order. Kept for the per-row listing; the duplicate verdict lives
     * in {@link #sameTileDuplicates()}, which only counts a repeat when
     * the earlier warp completed (a cancelled warp re-served is a heal,
     * not a double-mint).
     */
    public static java.util.List<String> buildingTypeTiles() {
        java.util.List<String> tiles = new java.util.ArrayList<>();
        for (Row row : rows.values()) {
            if ("building".equals(row.kind)) {
                tiles.add(row.type + "@[" + row.tx + "," + row.ty + "]");
            }
        }

        return tiles;
    }

    /**
     * Same-tile warp repeats, completion-gated: a repeat counts only
     * when an earlier warp of the same type on the same tile completed.
     * Cancelled-then-rebuilt rows (the common live shape) do not read
     * as double mints. Pure over the in-memory rows.
     */
    public static int sameTileDuplicates() {
        java.util.Set<String> completedTiles = new java.util.HashSet<>();
        int dups = 0;
        for (Row row : rows.values()) {
            if (!"building".equals(row.kind)) continue;

            String key = row.type + "@[" + row.tx + "," + row.ty + "]";
            if (completedTiles.contains(key)) dups++;
            if (row.completed) completedTiles.add(key);
        }

        return dups;
    }

    /**
     * Marks the warp finished (called on unit completion, never on warp
     * start - the ledger records appearance, this records survival).
     * Matches by type+tile, oldest uncompleted first: ids are engine
     * details the verdict must not depend on. Unknown tile: no-op.
     */
    public static void markCompleted(String type, int tx, int ty) {
        if (type == null) return;

        for (Row row : rows.values()) {
            if (!"building".equals(row.kind) || row.completed) continue;
            if (type.equals(row.type) && tx == row.tx && ty == row.ty) {
                row.completed = true;

                return;
            }
        }
    }

    /**
     * The warp ledger belongs at the tail of the debug stream too, so one
     * file carries diagnostics plus what warped, when and where. Java-side
     * (not the runner script) so IDE runs land it as well - stderr is the
     * debug stream on every launch path. A leaf reporting its own rows;
     * OnGameEnd only decides the moment.
     */
    public static void printTailToStderr() {
        System.err.println();
        System.err.println("===== BUILDINGS.json (ledger at game end) =====");
        System.err.print(toJson());
    }

    public static String toJson() {
        StringBuilder out = new StringBuilder("[\n");
        boolean first = true;
        for (Row row : rows.values()) {
            if (!first) out.append(",\n");
            first = false;
            out.append("  {\"kind\":\"").append(row.kind).append("\"")
                .append(",\"type\":\"").append(quote(row.type)).append("\"")
                .append(",\"tile\":[").append(row.tx).append(",").append(row.ty).append("]")
                .append(",\"frame\":").append(row.frame)
                .append(",\"time\":\"").append(quote(row.time)).append("\"")
                .append(",\"supplyUsed\":").append(row.supplyUsed)
                .append(",\"supplyTotal\":").append(row.supplyTotal)
                .append(",\"minerals\":").append(row.minerals)
                .append(",\"gas\":").append(row.gas)
                .append(",\"reservedMinerals\":").append(row.reservedMinerals)
                .append(",\"reservedGas\":").append(row.reservedGas)
                .append(",\"expectedSupply\":").append(row.expectedSupply == null ? "null" : row.expectedSupply)
                .append(",\"order\":").append(row.order == null ? "null" : "\"" + quote(row.order) + "\"")
                .append(",\"construction\":").append(row.constructionId == null ? "null" : row.constructionId)
                .append(",\"orderId\":").append(row.orderId == null ? "null" : row.orderId)
                .append(",\"completed\":").append(row.completed)
                .append("}");
        }
        return out.append(first ? "]" : "\n]").append("\n").toString();
    }

    public static void clearForTests() {
        rows.clear();
        lastFrame = -1;
    }

    private static String quote(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static final class Row {
        private final String kind;
        private final String type;
        private final int tx;
        private final int ty;
        private boolean completed;
        private final int frame;
        private final String time;
        private final int supplyUsed;
        private final int supplyTotal;
        private final int minerals;
        private final int gas;
        private final int reservedMinerals;
        private final int reservedGas;
        private final Integer expectedSupply;
        private final String order;
        private final Integer constructionId;
        private final Integer orderId;

        private Row(String kind, String type, int tx, int ty, int frame, String time, int supplyUsed, int supplyTotal, int minerals,
                    int gas, int reservedMinerals, int reservedGas,
                    Integer expectedSupply, String order, Integer constructionId, Integer orderId) {
            this.kind = kind;
            this.type = type;
            this.tx = tx;
            this.ty = ty;
            this.frame = frame;
            this.time = time;
            this.supplyUsed = supplyUsed;
            this.supplyTotal = supplyTotal;
            this.minerals = minerals;
            this.gas = gas;
            this.reservedMinerals = reservedMinerals;
            this.reservedGas = reservedGas;
            this.expectedSupply = expectedSupply;
            this.order = order;
            this.constructionId = constructionId;
            this.orderId = orderId;
        }
    }
}
