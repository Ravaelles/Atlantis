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
     */
    public static void recordBuilding(int unitId, String type, int tx, int ty, int frame, String time,
                                      int supplyUsed, int supplyTotal, int minerals, int gas,
                                      int reservedMinerals, int reservedGas,
                                      Integer expectedSupply, String order) {
        record("building", unitId, type, tx, ty, frame, time, supplyUsed, supplyTotal, minerals, gas,
            reservedMinerals, reservedGas, expectedSupply, order);
    }

    /**
     * A combat unit the build order asked for (Zealot, Dragoon, ...): same
     * columns as buildings, so demand and delivery read from one list.
     * Workers never land here; orderless spawns neither (no demand shown).
     */
    public static void recordUnit(int unitId, String type, int tx, int ty, int frame, String time,
                                  int supplyUsed, int supplyTotal, int minerals, int gas,
                                  int reservedMinerals, int reservedGas,
                                  Integer expectedSupply, String order) {
        record("unit", unitId, type, tx, ty, frame, time, supplyUsed, supplyTotal, minerals, gas,
            reservedMinerals, reservedGas, expectedSupply, order);
    }

    private static void record(String kind, int unitId, String type, int tx, int ty, int frame, String time,
                               int supplyUsed, int supplyTotal, int minerals, int gas,
                               int reservedMinerals, int reservedGas,
                               Integer expectedSupply, String order) {
        if (frame < lastFrame) rows.clear();
        lastFrame = frame;
        if (rows.containsKey(unitId)) return;
        rows.put(unitId, new Row(kind, type, tx, ty, frame, time, supplyUsed, supplyTotal, minerals, gas,
            reservedMinerals, reservedGas, expectedSupply, order));
    }

    public static void flush() {
        AFile.saveToFile(OUTPUT_PATH, toJson(), true);
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

        private Row(String kind, String type, int tx, int ty, int frame, String time, int supplyUsed, int supplyTotal, int minerals,
                    int gas, int reservedMinerals, int reservedGas,
                    Integer expectedSupply, String order) {
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
        }
    }
}
