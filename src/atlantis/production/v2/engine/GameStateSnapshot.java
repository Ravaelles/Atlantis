package atlantis.production.v2.engine;

import atlantis.config.AtlantisRaceConfig;
import atlantis.game.A;
import atlantis.production.v2.ProductionGoal;
import atlantis.production.v2.ProducerFacilityRegistry;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.v2.CommittedWork;
import atlantis.production.v2.EconomyModel;
import atlantis.production.v2.UnitProducible;
import atlantis.production.v2.ResourceTimeline;
import atlantis.production.v2.goals.BuildOrderGoals;
import atlantis.production.v2.goals.DynamicGoals;
import atlantis.production.v2.goals.PullForwardGoals;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.game.player.Enemy;
import atlantis.util.We;

import java.util.List;

/**
 * Everything the v2 engine needs to know about the live game, in one place
 * (M4/M5 wiring of _AI/redesign/01_PRODUCTION.md).
 *
 * <p>
 * The scheduler, the timeline and the dispatcher are pure: they know nothing
 * about BWAPI, and that stays true. This class is the opposite - it is the only
 * bridge, and it is deliberately dumb. It answers four questions and invents
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

    public GameStateSnapshot() {
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
                Count.ofType(AUnitType.Zerg_Mutalisk)
        );

        // Anchor the fortification goals to the main base. -1 (no base yet) leaves
        // them off, which is right: there is nothing to fortify before a Nexus.
        AUnit main = Select.main();
        if (main != null) snapshot = snapshot.atBase(main.tx(), main.ty());

        // Supply providers already on the way, so the supply goal is not re-emitted
        // every frame (see DynamicGoals.supplyGoal).
        snapshot.supplyProvidersComing = supplyProvidersComing();

        return DynamicGoals.contribute(snapshot);
    }

    /** Photon Cannons already standing at the main base. */
    private static int cannonsAtMain() {
        AUnit main = Select.main();
        if (main == null) return 0;

        return Select.ourWithUnfinished(AUnitType.Protoss_Photon_Cannon)
                .inRadius(20, main).count();
    }

    private static AUnitType armyType() {
        if (We.protoss()) return AUnitType.Protoss_Zealot;
        if (We.terran()) return AUnitType.Terran_Marine;
        if (We.zerg()) return AUnitType.Zerg_Zergling;
        return AtlantisRaceConfig.WORKER;
    }

    /**
     * Supply providers (Pylons/Depots/Overlords) already ordered and not yet
     * finished - existing plus in production plus in queue, minus what is done.
     * A Pylon adds supply only at completion, so this is the count that says "we
     * have enough supply coming".
     */
    private static int supplyProvidersComing() {
        AUnitType provider = supplyProviderType();
        if (provider == null) return 0;

        return Math.max(0, Count.ofTypeWithUnfinished(provider) - Count.ofType(provider));
    }

    private static AUnitType supplyProviderType() {
        if (We.protoss()) return AUnitType.Protoss_Pylon;
        if (We.terran()) return AUnitType.Terran_Supply_Depot;
        if (We.zerg()) return AUnitType.Zerg_Overlord;
        return null;
    }

    /** The worker type for this race, for the "workers we have or will have" count. */
    private static AUnitType workerType() {
        return AtlantisRaceConfig.WORKER;
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
                desiredRefineries));
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
