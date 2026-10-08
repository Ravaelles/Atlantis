package atlantis.production.v2.engine;

import atlantis.config.AtlantisRaceConfig;
import atlantis.game.A;
import atlantis.production.v2.ProducerFacility;
import atlantis.production.v2.ProducerFacilityRegistry;
import atlantis.production.v2.ResourceTimeline;
import atlantis.production.v2.goals.BuildOrderGoals;
import atlantis.production.v2.goals.DynamicGoals;
import atlantis.production.v2.goals.PullForwardGoals;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;

import java.util.ArrayList;
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

    /**
     * Minerals a single worker mines per frame, measured from the engine's own
     * saturation behaviour (~0.045 is one trip per ~1.5 s of 8 minerals).
     * Stardust uses the same constant-worker-rate model and documents the same
     * fidelity limit: BWAPI exposes no native forward gathering simulation.
     */
    private static final double MINERALS_PER_WORKER_FRAME = 0.045;
    private static final double GAS_PER_WORKER_FRAME = 0.07;

    private static final int WORKER_TRAIN_FRAMES = 300;

    private final int frame;
    private final int horizon;

    public GameStateSnapshot() {
        this.frame = A.now();
        this.horizon = Count.workers() < 20 && Count.bases() < 2 ? HORIZON_EARLY : HORIZON_LATE;
    }

    /** A blank timeline for the plan of this frame, seeded with current stocks. */
    public ResourceTimeline buildTimeline() {
        int minerals = A.minerals();
        int gas = A.gas();
        int supplyFree = Math.max(0, A.supplyTotal() - A.supplyUsed());

        ResourceTimeline timeline = new ResourceTimeline(horizon, minerals, gas, supplyFree);

        // Income is counted from the workers actively mining, not from all
        // workers: a worker walking to a build site does not mine, which is
        // exactly the cost the redesign insists on seeing.
        int miningWorkers = Select.ourWorkersMiningMinerals(false).count();
        int gasWorkers = 0;
        for (AUnit worker : Select.ourWorkers().list()) {
            if (worker.isGatheringGas() || worker.isCarryingGas()) gasWorkers++;
        }

        timeline.addMiningIncome(0,
                miningWorkers * MINERALS_PER_WORKER_FRAME,
                gasWorkers * GAS_PER_WORKER_FRAME);

        return timeline;
    }

    /**
     * Facilities that exist now, with the frame each frees up. A facility
     * training something is busy for the remainder of that unit's build time -
     * the same "occupyUntil" the scheduler models for planned buildings.
     */
    public ProducerFacilityRegistry facilityRegistry() {
        return new ProducerFacilityRegistry() {
            @Override
            public List<ProducerFacility> facilitiesOf(String typeId) {
                AUnitType type = AUnitType.getByName(typeId);
                List<ProducerFacility> facilities = new ArrayList<>();
                if (type == null)
                    return facilities;

                for (AUnit facility : Select.ourWithUnfinished().ofType(type).list()) {
                    facilities.add(new ProducerFacility(typeId, availableFromFrame(facility)));
                }
                return facilities;
            }
        };
    }

    /**
     * "Do we already have one?" answered from the live game - finished or under
     * construction, so the scheduler never plans a second Nexus for its workers.
     */
    public atlantis.production.v2.ExistingItems existingItems() {
        return new atlantis.production.v2.ExistingItems() {
            @Override
            public boolean have(atlantis.production.v2.Producible item) {
                AUnitType type = AUnitType.getByName(item.id());
                if (type == null) return false;

                // Finished or being built: either way a new one is not a
                // prerequisite to plan.
                return Select.ourWithUnfinished().ofType(type).notEmpty();
            }
        };
    }

    private int availableFromFrame(AUnit facility) {
        if (!facility.isCompleted()) {
            return Math.max(0, facility.getRemainingBuildTime());
        }
        if (facility.hasNothingInQueue())
            return 0;

        // Busy: it frees up when the last unit in its queue is done.
        return Math.max(0, facility.remainingTrainTime());
    }

    /** The dynamic goal generators for this frame. */
    public List<atlantis.production.v2.ProductionGoal> dynamicGoals() {
        return DynamicGoals.contribute(new DynamicGoals.GameSnapshot(
                Count.workers(),
                Math.max(1, Count.bases()),
                A.supplyUsed(),
                Math.max(0, A.supplyTotal() - A.supplyUsed()),
                A.supplyTotal(),
                A.minerals(),
                Count.workers() < 12));
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
                Count.ofTypeWithUnfinished(AtlantisRaceConfig.GAS_BUILDING),
                desiredRefineries));
    }

    /** The build-order goals for this frame, with a live supply projection. */
    public List<atlantis.production.v2.ProductionGoal> buildOrderGoals(
            List<atlantis.production.orders.production.queue.order.ProductionOrder> rows) {
        return BuildOrderGoals.from(rows, new BuildOrderGoals.SupplyProjection() {
            @Override
            public int supplyWithWhatIsProduced() {
                return A.supplyUsed() + supplyInProduction();
            }
        });
    }

    /**
     * Supply already committed to units being built or trained but not yet
     * counted in {@code supplyUsed}. It is the "plus what is produced" half of
     * a build-order row.
     */
    public int supplyInProduction() {
        int total = 0;

        for (AUnit building : Select.ourWithUnfinished().list()) {
            if (!building.isCompleted()) {
                total += Math.max(0, building.type().supplyNeeded());
            }
            for (AUnitType queued : building.trainingQueue()) {
                total += Math.max(0, queued.supplyNeeded());
            }
        }
        return total;
    }

    /** Frames a fresh worker needs before it can mine; used for income staging. */
    public static int workerTrainFrames() {
        return WORKER_TRAIN_FRAMES;
    }

    public int frame() {
        return frame;
    }

    public int horizon() {
        return horizon;
    }
}
