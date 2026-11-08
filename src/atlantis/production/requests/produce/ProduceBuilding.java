package atlantis.production.requests.produce;

import atlantis.game.A;
import atlantis.map.position.HasPosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.util.We;
import atlantis.util.log.EventLog;
import atlantis.util.log.LogKey;

public class ProduceBuilding {

    /**
     * cids that already have a STARTED row (ids never repeat, no reset needed).
     */
    private static final java.util.Set<Integer> startedCids = new java.util.HashSet<>();

    /**
     * Test/sim-game seam: STARTED-row dedup is per game (live ids never
     * repeat, so no reset is needed there), but sims share one JVM and
     * construction ids can repeat across them - without this a later
     * sim's warps lose their STARTED rows to an earlier sim's cids.
     * Called from {@code SimDriver.close()}, next to the other
     * end-of-game resets.
     */
    public static void clearStartedForTests() {
        startedCids.clear();
    }

    /**
     * Single-door concurrency cap for Pylons (Wine game: three concurrent
     * Pylon warps out of one supply block at 25 - BO#8, its dynamic mirror
     * and BO#11, each firing alone correctly and tripling together). Below
     * 40 supply used at most one Pylon may be unfinished: a second order
     * waits for the warp (owner: 10+ free supply above 25 from overlapping
     * BO + dynamic Pylons; the goal is never more than 8 free supply
     * there, which in practice means one Pylon at a time). At 40+ the
     * pipeline pair stays: overlapping builds is how late-game supply
     * keeps up. The cap counts warping + not-started + adopted-but-unitless
     * rows, so a row whose need is already being built waits instead of
     * re-siting. Stuck sites do
     * not deadlock this: overdue constructions are cancelled elsewhere
     * and the next row proceeds after the cancel.
     *
     * <p>An IN_PROGRESS row with no linked building counts too (live
     * 2026-11-07: adopted then builder lost, second Pylon minted behind
     * it at 24/25 with 218 bank). A rich bank (owner rule: above 300
     * minerals) still allows the pair: Pylon + Core stay affordable
     * together, so the cap gates concurrency, never the demand.
     */
    public static boolean tooManyPylonsInFlight() {
        int inFlight = ConstructionRequests.countNotFinishedOfType(AUnitType.Protoss_Pylon)
            + ConstructionRequests.countInProgressWithoutUnitOfType(AUnitType.Protoss_Pylon);
        if (atlantis.game.A.supplyUsed() < 40) {
            return inFlight >= 1 && !atlantis.game.A.hasMinerals(300);
        }

        return inFlight >= 2;
    }

    /**
     * True when an unfinished building of this type is already warping for
     * exactly this order - the order's site became a building, and no
     * second site may follow it. Sibling rows link different order
     * instances, so they never match here.
     */
    public static boolean orderHasWarpingBuilding(ProductionOrder order, AUnitType type) {
        if (order == null) return false;

        for (atlantis.units.AUnit unit : atlantis.units.select.Select.ourUnfinished()
            .ofType(type).list()) {
            if (order.equals(unit.productionOrder())) return true;
        }
        return false;
    }

    /**
     * Bank already spoken for by earlier-queued unfinished buildings:
     * the sum of their still-unpaid prices. A warping building has
     * already paid (its minerals left the bank at warp start), so it
     * holds no reserve - counting it would serialize every later
     * building behind a full warp-in (measured UMS 7th_rav: Pylon
     * waiting on a warping Core at bank 288). Own construction and
     * orderless ones excluded (no sequence to compare). Pure over the
     * live list, pinned in stub.
     */
    public static int earlierUnfinishedMinerals(ProductionOrder order) {
        return earlierUnfinishedCost(order, true);
    }

    public static int earlierUnfinishedGas(ProductionOrder order) {
        return earlierUnfinishedCost(order, false);
    }

    private static boolean isAlreadyPaidFor(Construction construction) {
        AUnit building = construction.buildingUnit();

        return building != null && building.isAlive();
    }

    private static int earlierUnfinishedCost(ProductionOrder order, boolean minerals) {
        if (order == null) return 0;

        int sum = 0;
        for (Construction other : ConstructionRequests.notFinished()) {
            ProductionOrder otherOrder = other.productionOrder();
            if (otherOrder == null || otherOrder == order) continue;
            if (otherOrder.id() >= order.id()) continue;
            if (other.buildingType() == null) continue;
            if (isAlreadyPaidFor(other)) continue;
            sum += minerals ? other.buildingType().mineralPrice() : other.buildingType().gasPrice();
        }
        return sum;
    }

    public static String demandContext(String line, int freeSupply, int unfinishedOfType,
                                       String prevLink, String path) {
        return line + " [free=" + freeSupply + " unfinished=" + unfinishedOfType
            + " prev=" + prevLink + " path=" + path + "]";
    }


    /**
     * Power before workers: sending a builder for a power-needing building
     * while no Pylon stands spends its price and then holds the worker on
     * unpowered ground while the bank refills for the Pylon. Wait until
     * the bank covers both (Pylon price + own price). Pylons themselves
     * never need power (their needsPower is false, so no first-Pylon
     * deadlock); warping counts as not built, per the owner's rule.
     */
    public static boolean powerProviderNotFunded(AUnitType type) {
        if (!We.protoss() || !type.needsPower()) return false;
        if (Count.ofType(AUnitType.Protoss_Pylon) > 0) return false;

        return !A.hasMinerals(
            AUnitType.Protoss_Pylon.mineralPrice() + type.mineralPrice());
    }

    /**
     * One STARTED row per construction, whoever observes the start: the
     * adopt path names the site, the warp-observe path names the warp
     * tile for legacy-minted warps that never adopted (NEXT #84: a
     * warping Gateway proved by the ledger with zero STARTED rows).
     * True when this call emitted the row.
     */
    public static boolean noteStarted(
        Construction construction, AUnit builder, HasPosition tile, ProductionOrder order
    ) {
        if (construction == null || !startedCids.add(construction.id())) return false;

        EventLog.key(LogKey.BUILDING_STARTED)
            .param("type", construction.buildingType().name())
            .param("cid", construction.id())
            .param("oid", order == null ? null : order.id())
            .param("tile", tile == null ? "-"
                : "[" + tile.tx() + "," + tile.ty() + "]")
            .param("builder", builder == null ? null : builder.id())
            .param("supplyUsed", A.supplyUsed())
            .param("expectedSupply", order == null ? null : order.minSupply())
            .param("supplyTotal", A.supplyTotal())
            .param("minerals", A.minerals())
            .log(A.now(), A.minSec());

        return true;
    }

}
