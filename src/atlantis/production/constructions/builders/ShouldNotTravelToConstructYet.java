package atlantis.production.constructions.builders;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.production.constructions.Construction;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.OrderStatus;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.v2.EconomyModel;
import atlantis.production.v2.ResourceCost;
import atlantis.production.v2.ResourceTimeline;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.We;

/**
 * When a builder walks, and when it waits - answered by simulation, not by
 * a static margin (owner: PurpleWave's Bank, ported, not reinvented).
 *
 * <p>The question is never "can we afford it now" but "will the bank cover
 * it when the builder arrives": the bank now plus mining income, minus
 * every earlier-queued unpaid row (buildings and workers alike, in queue
 * order like PurpleWave's priority running balance), gives the earliest
 * affordable frame; the walk takes distance over the builder's speed.
 * Walking now is right when the money lands no later than the boots.
 * The walk then overlaps earning, and the wait - when there is one -
 * happens AT the site, never midway: the move targets the site, only
 * the build order waits for the bank.</p>
 *
 * <p>Scope, on purpose narrow: supply 50 and under with a 240-frame (10 s)
 * horizon. Past 50 waiting is cheap and the arithmetic stops - the legacy
 * static margin below owns that half, untouched.</p>
 */
public class ShouldNotTravelToConstructYet {

    /**
     * Frames ahead the forecast looks (10 s): beyond this, hold.
     */
    public static final int FORECAST_HORIZON_FRAMES = 240;

    /**
     * Above this supply the simulation stops and the legacy margin decides.
     */
    public static final int FORECAST_MAX_SUPPLY = 50;

    /**
     * Arrival slack (frames): the money may land this late past the boots
     * and the walk still goes now. Covers estimate error, not strategy.
     * PurpleWave answers the same uncertainty with a x1.2-1.35 travel
     * multiplier plus a 24-48f hysteresis (builderTravelFrames); on our
     * scope (near-base walks, straight-line estimate) 24 frames play the
     * same role, so there is one knob, not two.
     */
    public static int FORECAST_ARRIVAL_SLACK_FRAMES = 24;

    public static boolean check(AUnitType building, double distance) {
        if (We.zerg()) return false;

        if (AGame.timeSeconds() < 300) {
            int baseBonus = building.isBase() ? 80 : 0;
            int mineralMargin = mineralMargin(building, distance);

            // Owner 2026-10-16: the first Pylon warped with ~130 banked
            // against its 100 price - arrival with a stockpile. The walk
            // discount above still lands past affordability, so the first
            // Pylon leaves 24 minerals earlier: a briefly idle builder at
            // the site is cheap, a supply-blocked base is not. Only while
            // no Pylon stands - later Pylons keep the old threshold.
            int firstPylonBonus = building.isPylon() && Count.pylons() == 0 ? 24 : 0;

            return !A.canAfford(
                building.mineralPrice() - mineralMargin - (int) (distance * 1.3) - baseBonus - firstPylonBonus,
                building.gasPrice() - 16 - (int) distance
            );
        }

        return false;
    }

    /**
     * Forecast-timed dispatch for one travelling builder: hold only when
     * the money lands past the boots (or past the horizon). Public as a
     * seam: the timing pins in a stub state without running the frame.
     */
    public static boolean check(Construction construction, AUnit builder, double distance) {
        AUnitType building = construction.buildingType();
        if (We.zerg()) return false;
        if (building == null) return false;

        if (A.supplyUsed() > FORECAST_MAX_SUPPLY) {
            return check(building, distance);
        }

        double tilesPerFrame = tilesPerFrame(builder);
        if (tilesPerFrame <= 0) {
            return check(building, distance);
        }

        int now = A.now();
        ResourceTimeline timeline = new ResourceTimeline(
            now, FORECAST_HORIZON_FRAMES, A.minerals(), A.gas(), 200, 200);
        timeline.addMiningIncome(
            now,
            EconomyModel.mineralRate(
                Select.ourWorkersMiningMinerals(false).count(), Math.max(1, Count.bases())),
            gasWorkers() * EconomyModel.GAS_PER_WORKER_FRAME);

        int[] earlier = earlierUnpaid(construction.productionOrder());
        ResourceCost own = ResourceCost.of(building.mineralPrice(), building.gasPrice(), 0);
        int affordableAt = -1;
        for (int frame = now; frame < now + FORECAST_HORIZON_FRAMES; frame++) {
            if (timeline.mineralsAt(frame) - earlier[0] >= own.minerals()
                && timeline.gasAt(frame) - earlier[1] >= own.gas()) {
                affordableAt = frame;
                break;
            }
        }
        if (affordableAt < 0) return true;

        int arrivalAt = now + (int) (distance / tilesPerFrame);

        return affordableAt > arrivalAt + FORECAST_ARRIVAL_SLACK_FRAMES;
    }

    private static double tilesPerFrame(AUnit builder) {
        if (builder == null) return -1;

        return builder.maxSpeed() / 32.0;
    }

    private static int gasWorkers() {
        int gasWorkers = 0;
        for (AUnit worker : Select.ourWorkers().list()) {
            if (worker.isGatheringGas() || worker.isCarryingGas()) gasWorkers++;
        }
        return gasWorkers;
    }

    /**
     * PurpleWave's running balance over the legacy queue: every earlier
     * unfinished, non-ignored row spends immediately on the forecast
     * (full price, tail-deducted). Queue order is priority order - rows
     * after the evaluated one do not deduct from it, so a far-future
     * At-29 never blocks an At-16. Already-paid warps (live building
     * unit) spend nothing: their minerals left the bank at warp start.
     * Conservative on purpose: an early dispatch never spends a later
     * row's minerals.
     */
    /**
     * PurpleWave's running balance over the legacy queue, as a lump sum:
     * every earlier unfinished, non-ignored row's full price still has to
     * be earned before this one. Queue order is priority order - rows
     * after the evaluated one do not count, so a far-future At-29 never
     * blocks an At-16. Already-paid warps (live building unit) count
     * nothing: their minerals left the bank at warp start. The balance
     * itself is never mutated (V2's solvency assert owns allocate()),
     * only read frame by frame against these totals.
     */
    private static int[] earlierUnpaid(ProductionOrder self) {
        int minerals = 0;
        int gas = 0;
        if (Queue.get() == null) return new int[]{minerals, gas};
        // Queue order is priority order: stop at the evaluated row. An
        // orderless construction (self not queued) counts everything -
        // position unknown, hold rather than overspend.
        for (ProductionOrder order : Queue.get().allOrders().list()) {
            if (order == null) continue;
            if (order == self) break;
            if (order.isStatus(OrderStatus.FINISHED) || order.shouldIgnore()) continue;
            if (order.construction() != null
                && order.construction().buildingUnit() != null
                && order.construction().buildingUnit().isAlive()) continue;
            minerals += order.mineralPrice();
            gas += order.gasPrice();
        }
        return new int[]{minerals, gas};
    }

    /**
     * Test seam for the queue-position-ordered earlier-demand model
     * (NEXT #156): what the travel forecast believes earlier rows still
     * need. Orderless self counts everything (no position, hold rather
     * than overspend); queued self counts only the rows ahead of it.
     */
    public static int[] earlierUnpaidForTests(ProductionOrder self) {
        return earlierUnpaid(self);
    }

    private static int mineralMargin(AUnitType building, double distance) {
        return (distance <= 15 ? 16 : 32)
            + (building.isCyberneticsCore() ? 8 : 0);
    }
}
