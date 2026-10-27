package atlantis.production.v2.engine;

import atlantis.config.AtlantisRaceConfig;
import atlantis.game.A;
import atlantis.production.v2.ProductionRacePolicy;
import atlantis.production.v2.ProducerFacilityRegistry;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionOrderStatus;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.v2.CommittedWork;
import atlantis.production.v2.EconomyModel;
import atlantis.production.v2.UnitProducible;
import atlantis.production.v2.ResourceTimeline;
import atlantis.production.v2.goals.BuildOrderGoals;
import atlantis.production.v2.goals.DynamicGoals;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.game.player.Enemy;
import atlantis.information.tech.ATech;
import atlantis.units.select.Have;
import atlantis.util.We;

import java.util.List;

/**
 * Everything the v2 engine needs to know about the live game, in one place
 * (M4/M5 wiring of _AI/redesign/01_PRODUCTION.md).
 *
 * <p>
 * The scheduler, the timeline and the dispatcher are pure: they know nothing
 * about BWAPI, and that stays true. This class is the opposite - it is the only
 * bridge, and it is deliberately dumb. It gathers the frame's numbers and invents
 * nothing:
 * <ul>
 * <li>what we have right now (minerals, gas, supply) and what we are mining
 * per frame;</li>
 * <li>which facilities exist and when each frees up (including the unit
 * currently in its build queue);</li>
 * <li>how much supply is already committed to units in production, for the
 * build-order rows;</li>
 * <li>a snapshot of the few numbers the dynamic goal generators read.</li>
 * </ul>
 * </p>
 *
 * <p>
 * Keeping the game access in this one class is what makes the rest of the layer
 * testable, and it is also what makes the eventual cutover reviewable: if the
 * plan is wrong, this class is where the numbers came from.
 * </p>
 *
 * <p>
 * Gathering and goal generation are two responsibilities, so the pure goal
 * sources are delegated to rather than inlined here: this class only extracts the
 * numbers and hands them over. That keeps the race/type mapping (which Producible
 * a goal names) out of the live bridge and testable without a game - ADR 0007's
 * direction, applied only as far as the existing types allow.
 * </p>
 */
public final class GameStateSnapshot {

    /**
     * Frames of planning horizon. Stardust predicts 2,000-4,500 depending on
     * the stage (01_PRODUCTION.md §Technical Blueprint); a long horizon makes
     * the timeline expensive and a short one truncates the plan, so the two
     * values below follow the same split.
     */
    private static final int HORIZON_EARLY = 2000;
    private static final int HORIZON_LATE = 4500;

    private final int frame;
    private final int horizon;
    private final ProductionRacePolicy racePolicy;

    /** Ambient race (tests, legacy callers): same source the old selectors read. */
    public GameStateSnapshot() {
        this(ProductionRacePolicy.ambient());
    }

    public GameStateSnapshot(ProductionRacePolicy racePolicy) {
        this.racePolicy = racePolicy;
        this.frame = A.now();
        this.horizon = Count.workers() < 20 && Count.bases() < 2 ? HORIZON_EARLY : HORIZON_LATE;
    }

    /**
     * A fresh timeline for this frame, in absolute frames from now: current
     * stocks, income of the workers mining now, plus the work already committed
     * (unpaid constructions, providers and workers being built).
     */
    public ResourceTimeline buildTimeline() {
        int supplyTotal = A.supplyTotal();
        int supplyFree = Math.max(0, supplyTotal - A.supplyUsed());
        ResourceTimeline timeline = new ResourceTimeline(frame, horizon, Math.max(0, A.minerals()),
                Math.max(0, A.gas()), supplyFree, supplyTotal);

        int mineralWorkers = Select.ourWorkersMiningMinerals(false).count();
        int gasWorkers = 0;
        for (AUnit worker : Select.ourWorkers().list()) {
            if (worker.isGatheringGas() || worker.isCarryingGas()) gasWorkers++;
        }
        timeline.addMiningIncome(frame,
                EconomyModel.mineralRate(mineralWorkers, Math.max(1, Count.bases())),
                gasWorkers * EconomyModel.GAS_PER_WORKER_FRAME);

        applyCommittedWork(timeline);

        return timeline;
    }

    private void applyCommittedWork(ResourceTimeline timeline) {
        for (Construction construction : ConstructionRequests.constructions) {
            AUnitType type = construction.buildingType();
            if (type == null || construction.hasStarted()) continue;
            int paidAt = CommittedWork.reserveUnpaid(timeline, UnitProducible.of(type).cost());
            if (paidAt >= 0)
                CommittedWork.providerCompletesAt(timeline, paidAt + type.totalTrainTime(),
                        UnitProducible.of(type).supplyProvided());
        }

        for (AUnit unit : Select.ourWithUnfinished().list()) {
            if (!unit.isCompleted()) {
                CommittedWork.providerCompletesAt(timeline, frame + Math.max(0, unit.getRemainingBuildTime()),
                        UnitProducible.of(unit.type()).supplyProvided());
                if (unit.type().isWorker())
                    CommittedWork.workerCompletesAt(timeline, frame + Math.max(0, unit.getRemainingBuildTime()));
            }
            if (unit.isCompleted() && unit.type().isBase() && !unit.hasNothingInQueue()) {
                CommittedWork.workerCompletesAt(timeline, frame + Math.max(0, unit.remainingTrainTime()));
            }
        }
    }

    public ProducerFacilityRegistry facilityRegistry() {
        return new LiveFacilityRegistry(frame);
    }

    public atlantis.production.v2.ExistingItems existingItems() {
        return new LiveExistingItems(frame);
    }

    /** The dynamic goal generators for this frame. */
    public List<atlantis.production.v2.ProductionGoal> dynamicGoals() {
        return dynamicGoals(false);
    }

    /**
     * @param buildOrderOwnsProvider the build order still holds an unproduced
     *                               provider row: the preventive supply fire
     *                               defers to it (emergency still fires).
     */
    public List<atlantis.production.v2.ProductionGoal> dynamicGoals(boolean buildOrderOwnsProvider) {
        // Workers EXISTING PLUS those already in production. Using the existing
        // count alone is what made the worker goal ask for another Probe every
        // frame: the goal layer asked "do we have fewer than the cap", the answer
        // stayed yes until the Probe hatched, and the scheduler issued one per
        // frame (measured 2026-10-08: Probe@2, @332, @635, ... on the OpenBW run,
        // and 2800+ Pylon issues before that). With the unfinished count the demand
        // disappears the moment the first one is ordered - which is what "we have
        // enough workers" actually means.
        int workers = Count.workers();
        int workersWithUnfinished = Count.ofTypeWithUnfinished(workerType());

        DynamicGoals.GameSnapshot snapshot = new DynamicGoals.GameSnapshot(
                workersWithUnfinished,
                Math.max(1, Count.bases()),
                A.supplyUsed(),
                Math.max(0, A.supplyTotal() - A.supplyUsed()),
                A.supplyTotal(),
                A.minerals(),
                workers < 12,
                Count.ofTypeWithUnfinished(armyType()),
                Count.basesWithUnfinished(),
                cannonsAtMain(),
                Enemy.zerg(),
                Enemy.protoss(),
                Count.ofType(AUnitType.Zerg_Mutalisk),
                A.gas(),
                We.protoss() && Have.cyberneticsCore(),
                We.protoss() ? Count.dragoons() : 0,
                Count.ofType(racePolicy.gasBuildingType()),
                We.protoss() && ATech.isResearched(bwapi.UpgradeType.Singularity_Charge),
                atlantis.information.generic.Army.strength());

        // Anchor the fortification goals to the main base. -1 (no base yet) leaves
        // them off, which is right: there is nothing to fortify before a Nexus.
        AUnit main = Select.main();
        if (main != null) snapshot = snapshot.atBase(main.tx(), main.ty());

        // Supply providers already on the way, so the supply goal is not re-emitted
        // every frame (see DynamicGoals.supplyGoal).
        snapshot.supplyProvidersComing = supplyProvidersComing();
        // Fortification cannons need a standing Forge (legacy gate,
        // ProtossSecureBasesCommander): without it a Cannon goal from frame
        // 0 plans an orphan Forge ahead of the Gateway.
        snapshot.hasForge = We.protoss() && Have.forge();
        twinSuppressedSupply(snapshot);

        return DynamicGoals.contribute(snapshot, racePolicy, buildOrderOwnsProvider);
    }

    /**
     * Names the providers a tight supply goal is waiting on: id, tile, hp and
     * whether the engine still calls them uncompleted. A provider coming for
     * minutes (cancelled warp, phantom entry) suppresses the supply goal
     * forever with zero rows anywhere - this is the row that names it.
     * Throttled: suppression is a state, not an event.
     */
    private void twinSuppressedSupply(DynamicGoals.GameSnapshot snapshot) {
        atlantis.production.SupplyBlockedTwin.sample(
            racePolicy.supplyProviderType(), snapshot.supplyFree, snapshot.supplyProvidersComing);
    }

    /** Photon Cannons already standing at the main base. */
    private static int cannonsAtMain() {
        AUnit main = Select.main();
        if (main == null) return 0;

        return Select.ourWithUnfinished(AUnitType.Protoss_Photon_Cannon)
                .inRadius(20, main).count();
    }

    private AUnitType armyType() {
        return racePolicy.armyUnitType();
    }

    /**
     * Supply providers (Pylons/Depots/Overlords) already ordered and not yet
     * finished - existing plus in production plus in queue, minus what is done.
     * A Pylon adds supply only at completion, so this is the count that says "we
     * have enough supply coming".
     *
     * <p>Units alone are not enough: a NOT_STARTED or STARTING construction
     * has no unit yet (builder finding the site, or walking to it), so a
     * unit-only count reads zero and the goal re-fires every frame until
     * the first warp - the scheduler then issues a second Pylon behind
     * the walking builder (measured V2 LIVE: Pylon@4234 + Pylon@4341 107f
     * apart into the same block). Constructions without a finished unit
     * count here; overdue cancels clear the stuck ones, so a genuinely
     * dead site unblocks by itself instead of suppressing forever.
     * PREPARING picks stay out (someone's aimed future, not live demand).</p>
     */
    private int supplyProvidersComing() {
        AUnitType provider = supplyProviderType();

        return supplyProvidersComing(provider);
    }

    /**
     * Package-visible as a seam: the whole coming count in one
     * testable place, so no term can silently drop out of the sum.
     */
    public static int supplyProvidersComing(AUnitType provider) {
        if (provider == null) return 0;

        return Math.max(0, Count.ofTypeWithUnfinished(provider) - Count.ofType(provider)
            + unstartedProvidersComing(provider)
            + unmaterializedInProgressComing(provider));
    }

    /**
     * Package-visible as a seam: unstarted providers count as coming
     * (PurpleWave rule, owner 2026-10-19). NOT_STARTED and STARTING have
     * no unit yet, so the unit-only count reads zero while a builder is
     * already committed - the goal re-fires and the scheduler issues a
     * second provider behind the walking builder. PREPARING picks stay
     * out (aimed future, not live demand); IN_PROGRESS with a linked
     * building is unit-covered (see unmaterializedInProgressComing for
     * the registration lag).
     */
    public static int unstartedProvidersComing(AUnitType provider) {
        if (provider == null) return 0;

        return ConstructionRequests.countNotStartedOfType(provider);
    }

    /**
     * The registration-lag half of the same rule: IN_PROGRESS whose
     * building has not linked yet. The advance fires on the order echo
     * while the server-side warp registers a few frames later on OpenBW -
     * in that window the unit term sees nothing (no unit) and the
     * unstarted term sees nothing (already IN_PROGRESS), so the goal
     * re-fires (measured V2 LIVE: Pylon@5991 while the @5734-stream
     * survivor had no unit yet, 78f-apart warps). Once the building
     * links, the unit term covers it - never double-counted. Dropped
     * warps demote to NOT_STARTED and move to the unstarted term.
     */
    public static int unmaterializedInProgressComing(AUnitType provider) {
        if (provider == null) return 0;

        int total = 0;
        for (Construction construction : ConstructionRequests.constructions) {
            if (construction == null || !provider.equals(construction.buildingType())) continue;
            if (construction.status() != ConstructionOrderStatus.IN_PROGRESS) continue;
            if (construction.buildingUnit() != null) continue;
            total++;
        }
        return total;
    }

    private AUnitType supplyProviderType() {
        return racePolicy.supplyProviderType();
    }

    /** The worker type for this race, for the "workers we have or will have" count. */
    private AUnitType workerType() {
        return racePolicy.workerType();
    }

    /**
     * The pull-forward rules (01_PRODUCTION.md step 3): supply and gas are wanted
     * a little before they are needed, so the plan does not stall the moment the
     * economy could have supported the next step.
     */
    public List<atlantis.production.v2.ProductionGoal> pullForwardGoals(
            atlantis.production.v2.ResourceTimeline timeline) {
        int desiredRefineries = Math.max(1, Math.min(2, Count.bases()));

        return atlantis.production.v2.goals.PullForwardGoals.contribute(
            timeline,
            new atlantis.production.v2.goals.PullForwardGoals.Snapshot(
                Math.max(0, A.supplyTotal() - A.supplyUsed()),
                A.minerals(),
                Select.ourWorkersMiningMinerals(false).count(),
                Count.ofTypeWithUnfinished(AtlantisRaceConfig.GAS_BUILDING),
                desiredRefineries,
                A.supplyUsed()),
            racePolicy);
    }

    /** The build-order goals for this frame: only rows the game has not produced yet. */
    public List<atlantis.production.v2.ProductionGoal> buildOrderGoals(
            List<atlantis.production.v2.goals.BuildOrderRow> rows) {
        return BuildOrderGoals.from(rows, new LiveBuildOrderProgress());
    }

    public int frame() {
        return frame;
    }

    public int horizon() {
        return horizon;
    }
}
