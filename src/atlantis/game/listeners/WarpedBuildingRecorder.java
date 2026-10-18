package atlantis.game.listeners;

import atlantis.game.A;
import atlantis.game.util.BuildingLedger;
import atlantis.production.orders.production.queue.ReservedResources;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnit;

/**
 * Single extraction point for the buildings ledger. The engine has two entry
 * points for "our building now exists" ({@code OnOurUnitCreated} for warps
 * and completions, {@code OnUnitMorph} for morphs) and both funnel here, so
 * the row shape cannot drift between them. The ledger dedupes by unit id.
 */
public class WarpedBuildingRecorder {

    /**
     * Last warp per building type (frame + tile): two same-type warps
     * landing within {@link #CLOSE_WARP_FRAMES} of each other are a
     * duplicate signal, not proof - a pipeline pair also lands close.
     * Static like the game clock: one JVM plays one game, tests reset
     * via {@link #clearForTests}.
     */
    private static final java.util.Map<String, int[]> lastWarpByType = new java.util.HashMap<>();

    /** Owner 2026-10-19: Pylons land 10-15 s apart; closer warps of one
      type name themselves here so the pair is attributable. */
    private static final int CLOSE_WARP_FRAMES = 240;

    public static void record(AUnit unit) {
        if (unit == null || !unit.isABuilding()) return;

        noteCloseWarp(unit);
        ProductionOrder order = unit.productionOrder();
        atlantis.production.constructions.Construction construction = unit.construction();
        bwapi.TilePosition tile = unit.tilePosition();
        BuildingLedger.recordBuilding(
            unit.id(), unit.type().name(), tile.getX(), tile.getY(), A.now(), A.minSec(),
            A.supplyUsed(), A.supplyTotal(), A.minerals(), A.gas(),
            ReservedResources.minerals(), ReservedResources.gas(),
            order == null ? null : order.minSupply(),
            order == null ? null : order.toString(),
            construction == null ? null : construction.id(),
            order == null ? null : order.id()
        );
        BuildingLedger.flush();
    }

    /**
     * Same-type warps landing within {@link #CLOSE_WARP_FRAMES} frames of
     * each other name themselves: at Pylon cadence (a warp every 10-15 s)
     * that proximity is a duplicate signal, and without the twin the two
     * rows read as two healthy warps. Pipeline overlap also lands close,
     * so this is a signal (HIGH), not a verdict - the prevention lives in
     * the unstarted-Pylon guard, this names what slipped through.
     */
    private static void noteCloseWarp(AUnit unit) {
        String type = unit.type().name();
        int[] prev = lastWarpByType.get(type);
        bwapi.TilePosition tile = unit.tilePosition();
        int now = A.now();
        if (prev != null && now - prev[0] < CLOSE_WARP_FRAMES) {
            atlantis.util.log.EventLog.key(atlantis.util.log.LogKey.CLOSE_WARPS)
                .param("type", type)
                .param("gap", now - prev[0])
                .param("prevTile", "[" + prev[1] + "," + prev[2] + "]")
                .param("tile", tile == null ? "-"
                    : "[" + tile.getX() + "," + tile.getY() + "]")
                .log(now, A.minSec());
        }
        lastWarpByType.put(type, new int[] {
            now,
            tile == null ? -1 : tile.getX(),
            tile == null ? -1 : tile.getY() });
    }

    public static void clearForTests() {
        lastWarpByType.clear();
    }

    /**
     * A combat unit the build order asked for. Orderless spawns (no demand
     * shown: starting units, Scarabs) stay out; workers never reach here.
     */
    public static void recordUnit(AUnit unit) {
        if (unit == null || unit.isABuilding() || !unit.isCombatUnit()) return;

        ProductionOrder order = unit.productionOrder();
        if (order == null) return;

        bwapi.TilePosition tile = unit.tilePosition();
        BuildingLedger.recordUnit(
            unit.id(), unit.type().name(), tile.getX(), tile.getY(), A.now(), A.minSec(),
            A.supplyUsed(), A.supplyTotal(), A.minerals(), A.gas(),
            ReservedResources.minerals(), ReservedResources.gas(),
            order.minSupply(), order.toString(), null, order.id()
        );
        BuildingLedger.flush();
    }
}
