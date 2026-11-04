package atlantis.production.demand;

import atlantis.config.env.Env;
import atlantis.game.A;
import atlantis.production.demand.ProductionGoal;
import atlantis.production.policy.ProductionRacePolicy;
import atlantis.production.placement.TargetPlacement;
import atlantis.production.producibles.UnitProducible;
import atlantis.units.AUnitType;
import atlantis.util.We;

import java.util.ArrayList;
import java.util.List;

/**
 * The goal generators that replace the legacy dynamic commanders (M5 of
 * _AI/redesign/01_PRODUCTION.md).
 *
 * <p>
 * The old pipeline had one commander per concern - {@code
 * AutoProduceWorkersCommander}, {@code DynamicBuildingsCommander},
 * {@code ExpansionCommander}, {@code SupplyCommander} - and each of them
 * <em>issued orders</em> directly, racing the static build order and each other
 * through a mutable queue. Here the same concerns only ever <em>declare what
 * they want</em>: a goal is a value, it carries a priority band, and the single
 * scheduler decides what the economy can actually pay for. That separation is
 * the whole change: policy states intent, the engine resolves contention.
 * </p>
 *
 * <p>
 * Every generator is a pure function of a small {@link GameSnapshot} - a
 * handful of numbers, not the game - so the whole policy layer is unit-tested
 * without StarCraft. The live snapshot is assembled by the v2 commander.
 * </p>
 */
public final class DynamicGoals {

    /**
     * Free supply below which a provider is wanted at all.
     */
    public static final int SUPPLY_WORRY_LEVEL = 4;
    /**
     * Workers target per base before the generator stops asking for more.
     */
    private static final int WORKERS_PER_BASE = 25;
    private static final int ABSOLUTE_WORKER_CAP = 80;

    private DynamicGoals() {
    }

    /**
     * All dynamic goals for this frame, priority-ordered by the caller's sort.
     * Empty when the snapshot cannot support any of them.
     */
    public static List<ProductionGoal> contribute(GameSnapshot state, ProductionRacePolicy race) {
        return contribute(state, race, false);
    }

    /**
     * @param buildOrderOwnsProvider the build order still holds an unproduced
     *                               provider row: the preventive supply fire
     *                               defers to it (emergency still fires).
     */
    public static List<ProductionGoal> contribute(
        GameSnapshot state, ProductionRacePolicy race, boolean buildOrderOwnsProvider) {
        List<ProductionGoal> goals = new ArrayList<>();

        addWorkerGoal(goals, state, race);
        addSupplyGoal(goals, state, race, buildOrderOwnsProvider);
        addArmyGoal(goals, state, race);
        addGatewayGoal(goals, state);
        addTechGoal(goals, state);
        addGasGoal(goals, state, race);
        addCoreBuildingGoal(goals, state);
        addFirstAssimilatorGoal(goals, state, race);
        addExpansionGoal(goals, state, race);
        addDetectionGoals(goals, state);
        addStargateGoal(goals, state);
        addCorsairGoal(goals, state);
        addObserverPump(goals, state);
        addWeaponsGoal(goals, state);
        addArmorGoal(goals, state);
        addCitadelGoal(goals, state);
        addLegEnhancementsGoal(goals, state);
        addArchivesGoal(goals, state);
        addHighTemplarGoal(goals, state);
        addStormGoal(goals, state);
        addDarkTemplarGoal(goals, state);
        addSupportBayGoal(goals, state);
        addReaverGoal(goals, state);
        addShuttleGoal(goals, state);
        addTribunalGoal(goals, state);
        addArbiterGoal(goals, state);
        addFortificationGoals(goals, state);

        goals.replaceAll(goal -> goal.withSource("dynamic"));

        // Owner #223: twin which detection goals the layer emits while the
        // snapshot flag holds (test-side calls stay silent - the twin is
        // a live-game witness, not a pin).
        if (!Env.isTesting()) {
            V2GoalSourceTwin.emitDetection(A.now(), goals, state.enemyHasHiddenUnits);
        }

        return goals;
    }

    /**
     * Hidden-unit crisis response (Protoss): a seen lurker or Dark Templar
     * - or the tech that predicts them - orders eyes and a shell the same
     * frame. The scheduler pulls missing prerequisites itself (a Cannon
     * goal warps its Forge, an Observatory goal its Robotics Facility),
     * so the chain reads as plain goals: Robo into Observatory into
     * Observer, a Forge unless one stands, and two cannons at the main
     * while the chain spins up. The hasForge gate below stays a
     * no-reason rule: with a reason the early Forge is the answer, not
     * the intruder (measured LIVE: a seen lurker ordered nothing because
     * the legacy queue path never drains in V2 LIVE and this branch did
     * not exist).
     */
    private static void addDetectionGoals(List<ProductionGoal> goals, GameSnapshot state) {
        if (!We.protoss()) return;
        if (!state.enemyHasHiddenUnits) return;

        goals.add(new ProductionGoal(
            UnitProducible.of(AUnitType.Protoss_Robotics_Facility),
            ProductionGoal.PRIORITY_BASEDEFENSE, 1, 0, TargetPlacement.anywhere()));
        goals.add(new ProductionGoal(
            UnitProducible.of(AUnitType.Protoss_Observatory),
            ProductionGoal.PRIORITY_BASEDEFENSE, 1, 0, TargetPlacement.anywhere()));

        if (!state.hasForge) {
            goals.add(new ProductionGoal(
                UnitProducible.of(AUnitType.Protoss_Forge),
                ProductionGoal.PRIORITY_BASEDEFENSE, 1, 0, TargetPlacement.anywhere()));
        }

        if (state.mainBaseTileX < 0) return;
        int missingCannons = Math.max(0, 2 - state.cannonsAtMain);
        for (int i = 0; i < missingCannons; i++) {
            goals.add(new ProductionGoal(
                UnitProducible.of(AUnitType.Protoss_Photon_Cannon),
                ProductionGoal.PRIORITY_BASEDEFENSE, 1, 0,
                TargetPlacement.inNeighbourhood(state.mainBaseTileX, state.mainBaseTileY)));
        }
    }

    /**
     * Stargate (Protoss): the board's matchup want, verbatim numbers.
     * Against Zerg past supply 40 with an army behind it (or 70+, or
     * mutas in the sky); against Protoss with 13+ Dragoons facing
     * carrier tech; never against Terran (the legacy branch answers
     * false - Wraiths are met by existing eyes, not a new building).
     * Standing coverage is execution-side (existingItems), so the goal
     * is a plain count-1 want - the scheduler dedups the re-fire.
     */
    private static void addStargateGoal(List<ProductionGoal> goals, GameSnapshot state) {
        if (!We.protoss()) return;
        if (!stargateWanted(state)) return;

        goals.add(new ProductionGoal(
            UnitProducible.of(AUnitType.Protoss_Stargate),
            ProductionGoal.PRIORITY_BASEDEFENSE, 1, 0, TargetPlacement.anywhere()));
    }

    private static boolean stargateWanted(GameSnapshot state) {
        if (state.enemyIsProtoss) {
            return state.dragoons > 12 && state.enemyCarrierTech;
        }
        if (!state.enemyIsZerg && !state.enemyIsProtoss) return false;

        if (state.dragoons <= 1) return false;
        if (state.armyStrength <= 115 && state.combatUnits <= 5 && state.cannonsAtMain <= 1) return false;
        if (state.supplyUsed <= 40 && state.enemyHydras >= 10) return false;
        if (state.supplyUsed <= 40) return false;
        if (state.supplyUsed >= 70) return true;
        if (state.supplyUsed >= 90 && state.armyStrength >= 95 && state.enemyLurkers <= 2) return true;

        return state.enemyMutalisks > 0;
    }

    /**
     * Corsairs (Protoss): the eyes the Stargate trains, pumped against
     * Zerg air - a standing patrol plus one per two mutas. Fires only
     * with a Stargate standing or warping (no eyes before the building),
     * and only while standing eyes fall short of the want (the pump
     * replaces losses, it never stockpiles). Vs Terran Wraiths needs an
     * enemyIsTerran signal the snapshot does not carry yet - deliberately
     * unported, not silently dropped (the legacy branch stays the record).
     */
    private static void addCorsairGoal(List<ProductionGoal> goals, GameSnapshot state) {
        if (!We.protoss()) return;
        if (!state.enemyIsZerg) return;
        if (!state.hasStargate) return;
        if (state.minerals < 200) return;

        int want = Math.max(2, state.supplyUsed / 36);
        want = Math.max(want, state.enemyMutalisks / 2 + 2);
        if (want - state.corsairs <= 0) return;

        goals.add(new ProductionGoal(
            UnitProducible.of(AUnitType.Protoss_Corsair),
            ProductionGoal.PRIORITY_MAINARMY, 1, 0, TargetPlacement.anywhere()));
    }

    /**
     * Observer pump (Protoss): eyes scale with the threat - three on the
     * hidden-unit flag (or one per 40 supply, whichever is more), four
     * against seen lurkers. Replaces the old single-Observer detection
     * goal: the count rides the standing eyes (losses replaced, never
     * stockpiled). Routine eyes without a flag (strength milestones,
     * quite-late backstop) stay unported - the snapshot reads pressure,
     * not the clock. Vs Terran the legacy want is one per 40 supply; the
     * snapshot cannot tell Terran from unknown, so the flag formula
     * answers (at most one eye over).
     */
    private static void addObserverPump(List<ProductionGoal> goals, GameSnapshot state) {
        if (!We.protoss()) return;
        if (!state.enemyHasHiddenUnits
            && !(state.enemyIsZerg && state.enemyLurkers >= 1)) return;

        int want = Math.max(1 + 2, state.supplyTotal / 40);
        if (state.enemyIsZerg && state.enemyLurkers >= 1) {
            want = Math.max(want, 4);
        }
        if (want - state.observers <= 0) return;

        goals.add(new ProductionGoal(
            UnitProducible.of(AUnitType.Protoss_Observer),
            ProductionGoal.PRIORITY_MAINARMY, 1, 0, TargetPlacement.anywhere()));
    }

    /**
     * Citadel of Adun (Protoss): the eyes get clearer before the templar
     * line opens. Verbatim gates - supply 140+ with gas 180 behind it and
     * an Observatory standing. Research coverage is execution-side, so no
     * researched flag rides the snapshot.
     */
    private static void addCitadelGoal(List<ProductionGoal> goals, GameSnapshot state) {
        if (!We.protoss()) return;
        if (state.supplyUsed < 140 || state.gas < 180) return;
        if (!state.hasObservatory) return;

        goals.add(new ProductionGoal(
            UnitProducible.of(AUnitType.Protoss_Citadel_of_Adun),
            ProductionGoal.PRIORITY_NORMAL, 1, 0, TargetPlacement.anywhere()));
    }

    /**
     * Leg Enhancements (Protoss): zealot speed once the Citadel stands,
     * past supply 90 with 4+ zealots on the field. The 30-second
     * queue-history throttle stays behind - the scheduler dedups the
     * re-fire the throttle used to eat.
     */
    private static void addLegEnhancementsGoal(List<ProductionGoal> goals, GameSnapshot state) {
        if (!We.protoss()) return;
        if (!state.hasCitadel) return;
        if (state.supplyUsed <= 90 || state.zealots <= 4) return;

        goals.add(new ProductionGoal(
            atlantis.production.producibles.UpgradeProducible.of(bwapi.UpgradeType.Leg_Enhancements),
            ProductionGoal.PRIORITY_NORMAL,
            1,
            0,
            TargetPlacement.anywhere()));
    }

    /**
     * Templar Archives (Protoss): the templar line opens vs Zerg once the
     * bank or the supply says so - 90+ supply or 230+ gas, a line that can
     * hold it (15+ combat units with 120+... 140+ strength, or 200/200 in
     * hand). Dropped from the legacy read: the mission-attack clause (no
     * mission signal rides the snapshot) and the BO transition permission
     * (BOs own the opening only; the Archives is long past it). The
     * Citadel prerequisite schedules itself.
     */
    private static void addArchivesGoal(List<ProductionGoal> goals, GameSnapshot state) {
        if (!We.protoss()) return;
        if (!state.enemyIsZerg) return;
        if (!(state.supplyUsed >= 90 || state.gas > 230)) return;
        if ((state.combatUnits <= 15 || state.armyStrength <= 140)
            && !(state.minerals >= 200 && state.gas >= 200)) return;

        goals.add(new ProductionGoal(
            UnitProducible.of(AUnitType.Protoss_Templar_Archives),
            ProductionGoal.PRIORITY_NORMAL, 1, 0, TargetPlacement.anywhere()));
    }

    /**
     * High Templar (Protoss): the storm casters the Archives trains, up to
     * a supply-tiered cap (2/3/4/6). Count-1 pump while short, same shape
     * as the Corsair and Observer pumps.
     */
    private static void addHighTemplarGoal(List<ProductionGoal> goals, GameSnapshot state) {
        if (!We.protoss()) return;
        if (!state.hasArchives) return;

        int want;
        if (state.supplyUsed <= 145) want = 2;
        else if (state.supplyUsed <= 160) want = 3;
        else if (state.supplyUsed <= 180) want = 4;
        else want = 6;
        if (want - state.highTemplar <= 0) return;

        goals.add(new ProductionGoal(
            UnitProducible.of(AUnitType.Protoss_High_Templar),
            ProductionGoal.PRIORITY_MAINARMY, 1, 0, TargetPlacement.anywhere()));
    }

    /**
     * Psionic Storm (Protoss): researched once a Templar stands to cast
     * it. Research coverage is execution-side.
     */
    private static void addStormGoal(List<ProductionGoal> goals, GameSnapshot state) {
        if (!We.protoss()) return;
        if (state.highTemplar < 1) return;

        goals.add(new ProductionGoal(
            atlantis.production.producibles.TechProducible.of(bwapi.TechType.Psionic_Storm),
            ProductionGoal.PRIORITY_NORMAL,
            1,
            0,
            TargetPlacement.anywhere()));
    }

    /**
     * Dark Templar (Protoss): the unseen blade, up to a supply-tiered cap
     * (2/3/4/5/6). A Support Bay with no Reaver yet outranks the first DT
     * (the gas builds the Reaver first); the free-Robo refinement stays
     * behind - V2 never sees facility idleness. Lifetime-10 cap becomes
     * loss replacement, the standing rule all the other pumps follow.
     */
    private static void addDarkTemplarGoal(List<ProductionGoal> goals, GameSnapshot state) {
        if (!We.protoss()) return;
        if (!state.hasArchives) return;
        if (state.hasSupportBay && state.reavers == 0) return;

        int want;
        if (state.supplyUsed <= 80) want = 2;
        else if (state.supplyUsed <= 110) want = 3;
        else if (state.supplyUsed <= 140) want = 4;
        else if (state.supplyUsed <= 160) want = 5;
        else want = 6;
        if (want - state.darkTemplar <= 0) return;

        goals.add(new ProductionGoal(
            UnitProducible.of(AUnitType.Protoss_Dark_Templar),
            ProductionGoal.PRIORITY_MAINARMY, 1, 0, TargetPlacement.anywhere()));
    }

    /**
     * Robotics Support Bay (Protoss): the reaver line opens past supply
     * 70 (80 with a 150+ line, or 500/180 banked) - Archives first vs
     * Zerg, eyes first vs lurkers, a 120/140/2 line vs Protoss. Dropped
     * with notes: the vs-Terran 90 floor (no Terran signal) and the 49
     * floor below every other gate.
     */
    private static void addSupportBayGoal(List<ProductionGoal> goals, GameSnapshot state) {
        if (!We.protoss()) return;
        if (state.enemyIsZerg && !state.hasArchives) return;
        if (state.enemyLurkers > 0 && state.observers == 0) return;
        if (state.enemyIsProtoss
            && (state.supplyUsed <= 120 || state.armyStrength <= 140 || state.observers <= 2)) return;

        boolean want = state.supplyUsed >= (state.armyStrength >= 150 ? 80 : 120)
            || (state.supplyUsed >= 70 && state.minerals >= 500 && state.gas >= 180);
        if (!want) return;

        goals.add(new ProductionGoal(
            UnitProducible.of(AUnitType.Protoss_Robotics_Support_Bay),
            ProductionGoal.PRIORITY_NORMAL, 1, 0, TargetPlacement.anywhere()));
    }

    /**
     * Reavers (Protoss): one first past 70, then one per 45 supply up to
     * 4 - plus 2 when 200/200 is banked. Eyes first vs air (no Reaver
     * into uncovered mutas). Standing and in-flight coverage is
     * execution-side, so the goal is a count-1 pump while short.
     */
    private static void addReaverGoal(List<ProductionGoal> goals, GameSnapshot state) {
        if (!We.protoss()) return;
        if (!state.hasRobo || !state.hasSupportBay) return;
        if (state.supplyUsed <= 70) return;
        if (state.enemyMutalisks > 0 && state.observers < 2 && state.corsairs < 2) return;
        if (state.observers == 0 && state.enemyHasHiddenUnits) return;

        int max = state.reavers == 0 ? 1
            : Math.min(4 + (state.minerals >= 200 && state.gas >= 200 ? 2 : 0),
                Math.max(0, 1 + state.supplyUsed / 45));
        if (max - state.reavers <= 0) return;

        goals.add(new ProductionGoal(
            UnitProducible.of(AUnitType.Protoss_Reaver),
            ProductionGoal.PRIORITY_MAINARMY, 1, 0, TargetPlacement.anywhere()));
    }

    /**
     * Shuttles (Protoss): the first the moment Reavers stand (or a 650
     * bank says so), then one per Reaver past 100 supply - never more
     * Shuttles than Reavers, never into a weak toothless line, never
     * blind into hidden units.
     */
    private static void addShuttleGoal(List<ProductionGoal> goals, GameSnapshot state) {
        if (!We.protoss()) return;
        if (!state.hasRobo) return;
        if (state.observers == 0 && state.enemyHasHiddenUnits) return;
        if (state.armyStrength <= 120 && state.combatUnits <= 10) return;

        boolean want = (state.shuttles == 0 && (state.reavers > 0 || state.minerals >= 650))
            || (state.supplyUsed >= 100 && state.shuttles > 0 && state.reavers > state.shuttles);
        if (!want) return;

        goals.add(new ProductionGoal(
            UnitProducible.of(AUnitType.Protoss_Shuttle),
            ProductionGoal.PRIORITY_MAINARMY, 1, 0, TargetPlacement.anywhere()));
    }

    /**
     * Arbiter Tribunal (Protoss): late bulk enabler past 170/180 supply
     * (unknown reads late - the Terran 120 branch needs a signal the
     * snapshot lacks), behind a standing Bay, with a line or a bank
     * behind it.
     */
    private static void addTribunalGoal(List<ProductionGoal> goals, GameSnapshot state) {
        if (!We.protoss()) return;
        if (!state.hasSupportBay) return;
        if (state.supplyUsed < (state.enemyIsZerg ? 180 : state.enemyIsProtoss ? 170 : 180)) return;
        if (state.armyStrength <= 130 && !(state.minerals >= 400 && state.gas >= 300)) return;

        goals.add(new ProductionGoal(
            UnitProducible.of(AUnitType.Protoss_Arbiter_Tribunal),
            ProductionGoal.PRIORITY_NORMAL, 1, 0, TargetPlacement.anywhere()));
    }

    /**
     * Arbiters (Protoss): two behind a standing Tribunal (five vs
     * Terran needs the same missing signal - capped at two). Losses
     * replaced, never stockpiled.
     */
    private static void addArbiterGoal(List<ProductionGoal> goals, GameSnapshot state) {
        if (!We.protoss()) return;
        if (!state.hasTribunal) return;
        if (2 - state.arbiters <= 0) return;

        goals.add(new ProductionGoal(
            UnitProducible.of(AUnitType.Protoss_Arbiter),
            ProductionGoal.PRIORITY_MAINARMY, 1, 0, TargetPlacement.anywhere()));
    }

    /**
     * Base fortification (Protoss): the cannon policy from
     * {@code atlantis.placement.policy} expressed as ordinary goals.
     *
     * <p>
     * This is where the placement rewrite and Production V2 meet, and the join is
     * deliberately one-directional: the policy says "this base wants N cannons near
     * here" and the planner resolves the constraint to a choke-aware tile (S4). No
     * tile is computed here, which is the §5.3 boundary of
     * {@code _AI/redesign/03_PLACEMENT.md} - policy emits intent, placement decides
     * where.
     * </p>
     */
    private static void addFortificationGoals(List<ProductionGoal> goals, GameSnapshot state) {
        if (!We.protoss()) return;
        if (state.mainBaseTileX < 0) return;
        // No Forge, no cannons: legacy secured bases with cannons only
        // once a Forge stood (ProtossSecureBasesCommander.applies gates on
        // Have.forge()). Without this a Cannon goal from frame 0 plans an
        // orphan Forge prerequisite that the dispatcher builds while the
        // Cannon itself never schedules - a Forge before the Gateway in
        // openings whose build order has no Forge.
        if (!state.hasForge) return;

        atlantis.placement.policy.CannonFortificationPolicy.Context context =
            new atlantis.placement.policy.CannonFortificationPolicy.Context(
                state.supplyTotal,
                state.minerals,
                state.cannonsAtMain,
                state.enemyIsZerg,
                state.enemyIsProtoss,
                state.enemyMutalisks
            );

        goals.addAll(atlantis.placement.policy.FortificationGoals.forBase(
            context, UnitProducible.of(AUnitType.Protoss_Photon_Cannon),
            state.mainBaseTileX, state.mainBaseTileY
        ));
    }

    /**
     * Continuous worker production, up to the saturation of the bases we have.
     * The old commander computed this per frame with a long list of vetoes; the
     * v2 form is the saturation rule itself, because the vetoes were really
     * about "is there money for anything more important" - and that question is
     * now answered once, by the timeline, for every goal at the same time.
     *
     * <p>
     * A warping base counts toward saturation: pre-saturating the expansion
     * is standard play, and counting only standing bases stalls the pump
     * for the whole warp (measured: 1 probe in ~1800 frames, bank to 849).
     * </p>
     */
    private static void addWorkerGoal(List<ProductionGoal> goals, GameSnapshot state, ProductionRacePolicy race) {
        AUnitType worker = race.workerType();
        if (worker == null)
            return;

        if (state.workers >= ABSOLUTE_WORKER_CAP)
            return;

        if (state.workers >= WORKERS_PER_BASE * Math.max(1, Math.max(state.bases, state.basesExisting)))
            return;

        goals.add(new ProductionGoal(
            UnitProducible.of(worker),
            ProductionGoal.PRIORITY_WORKERS,
            1,
            0,
            TargetPlacement.anywhere()));
    }

    /**
     * Supply-block prevention: a Pylon/Depot/Overlord the moment free supply
     * runs low. It is emitted as an emergency only at the edge (so it never
     * outranks real defence early), otherwise it is a normal-priority goal.
     *
     * <p>
     * While the build order still holds an unproduced provider row, the
     * PREVENTIVE fire defers to it: the row is the opening's single provider
     * author (measured: the supply goal warped the first Pylon at supply 6
     * for gate 8). The EMERGENCY fire (no free supply at all) never defers -
     * a live block is already a failed plan, and any author beats none.
     * </p>
     */
    public static ProductionGoal supplyGoal(GameSnapshot state, ProductionRacePolicy race) {
        return supplyGoal(state, race, false);
    }

    public static ProductionGoal supplyGoal(
        GameSnapshot state, ProductionRacePolicy race, boolean buildOrderOwnsProvider) {
        AUnitType supplyProvider = race.supplyProviderType();
        if (supplyProvider == null)
            return null;

        if (state.supplyFree > SUPPLY_WORRY_LEVEL)
            return null;

        if (buildOrderOwnsProvider && state.supplyFree > 0)
            return null;

        // A provider already on the way counts: supply arrives when the Pylon
        // finishes, so without this the goal is emitted every frame until then and
        // the scheduler orders a Pylon per frame (the "2800 ordered Pylons" run).
        // The same shape as the worker goal's workers-with-unfinished count.
        // Above 60 total supply two providers may fly at once (owner
        // 2026-10-19: production outruns warp completion there) - the second
        // coming is allowed, the third is not.
        if (state.supplyProvidersComing > (state.supplyTotal > 60 ? 1 : 0))
            return null;

        int priority = state.supplyFree <= 0
            ? ProductionGoal.PRIORITY_EMERGENCY
            : ProductionGoal.PRIORITY_OPENING_SUPPLY;

        // Any provider named here is a precondition, not a depot: the goal
        // only fires at 4 or fewer free (or a build-order row), so it jumps
        // the worker queue like the opening Pylon does (NEXT #52 class:
        // a DEPOTS-band Pylon waited on worker leftovers until bank 152
        // while the legacy drain dispatched at 50-100).

        return new ProductionGoal(UnitProducible.of(supplyProvider), priority, 1, 0, TargetPlacement.anywhere());
    }

    private static void addSupplyGoal(List<ProductionGoal> goals, GameSnapshot state, ProductionRacePolicy race) {
        addSupplyGoal(goals, state, race, false);
    }

    private static void addSupplyGoal(
        List<ProductionGoal> goals, GameSnapshot state, ProductionRacePolicy race,
        boolean buildOrderOwnsProvider) {
        ProductionGoal goal = supplyGoal(state, race, buildOrderOwnsProvider);
        if (goal != null) goals.add(goal);
    }

    private static boolean tooWeakToTech(GameSnapshot state) {
        return state.armyStrength < 90 && state.seconds <= 600 && state.minerals <= 700;
    }

    /**
     * Ground weapons ladder (Protoss): one level at a time past a Forge,
     * with 10+ combat units and a bank behind it (700 minerals or a third
     * base), never while too weak to tech (sub-90 strength on under 700).
     * The last stretch needs gas in hand and either 500/200 banked or 17+
     * combat units. Queue-history and already-queued guards stay behind:
     * the scheduler dedups the re-fire they used to eat. Levels come from
     * the snapshot; in-flight coverage is execution-side.
     */
    private static void addWeaponsGoal(List<ProductionGoal> goals, GameSnapshot state) {
        if (!We.protoss()) return;
        if (!state.hasForge) return;
        if (state.weaponsLevel >= 3) return;
        if (state.combatUnits <= 10) return;
        if (state.minerals < 700 && state.basesExisting <= 2) return;
        if (tooWeakToTech(state)) return;

        int gasNeed = 150 + (state.enemyIsZerg ? 80 : 0);
        if (!(state.gas >= gasNeed && state.minerals >= 290)) return;
        if (!(state.minerals >= 500 && state.gas >= 200) && state.combatUnits <= 17) return;

        goals.add(new ProductionGoal(
            atlantis.production.producibles.UpgradeProducible.of(
                bwapi.UpgradeType.Protoss_Ground_Weapons, state.weaponsLevel + 1),
            ProductionGoal.PRIORITY_NORMAL, 1, 0, TargetPlacement.anywhere()));
    }

    /**
     * Ground armor ladder (Protoss): weapons L1 first (vanilla has no
     * cross requirement, and the old L3 gate held armor to the end of the
     * tech path - dropped deliberately), then the same shape as weapons:
     * 10+ combat units, 650 minerals or 400 gas or a second base, never
     * while too weak, gas-heavy finish (270, +100 vs Protoss).
     */
    private static void addArmorGoal(List<ProductionGoal> goals, GameSnapshot state) {
        if (!We.protoss()) return;
        if (!state.hasForge) return;
        if (state.weaponsLevel < 1) return;
        if (state.combatUnits <= 10) return;
        if (state.armorLevel >= 3) return;
        if (state.minerals < 650 && state.gas < 400 && state.basesExisting < 2) return;
        if (tooWeakToTech(state)) return;

        int gasNeed = 270 + (state.enemyIsProtoss ? 100 : 0);
        if (!(state.gas >= gasNeed && state.minerals >= 550)) return;
        if (!(state.minerals >= 500 && state.gas >= 200) && state.combatUnits <= 17) return;

        goals.add(new ProductionGoal(
            atlantis.production.producibles.UpgradeProducible.of(
                bwapi.UpgradeType.Protoss_Ground_Armor, state.armorLevel + 1),
            ProductionGoal.PRIORITY_NORMAL, 1, 0, TargetPlacement.anywhere()));
    }

    /**
     * Mid-game Gateway demand: the build order owns the first two rows and
     * nothing else ever asks for a Gateway in V2 (measured live 183253:
     * 2 Gateways the whole game, bank to 7963 while Dragoon demand waited
     * on busy facilities). Fires only when rich and every Gateway is busy,
     * past the opening - capped at four, six vs Protoss without fortified
     * enemies (legacy 6-gateway rush) - never ahead of the bank, never
     * instead of the BO rows. The prioritize-over-expanding half is not
     * ported: V2 priority bands already order economy before army.
     */
    private static void addGatewayGoal(List<ProductionGoal> goals, GameSnapshot state) {
        if (!We.protoss()) return;
        if (state.supplyUsed < GATEWAY_DEMAND_SUPPLY) return;

        int cap = state.enemyIsProtoss && state.enemyCombatBuildings == 0 ? 6 : 4;
        if (state.gatewaysExisting < 2 || state.gatewaysExisting >= cap) return;

        // Third Gateway (owner order 2026-11-04): no early third - only on
        // a saturated completed pair (both standing, none walking/warping),
        // with 225+ minerals in the bank. Later Gateways keep the old
        // rich-plus-busy rule below.
        if (state.gatewaysExisting == 2) {
            if (state.minerals < GATEWAY_THIRD_BANK_MINERALS) return;
            if (state.gatewaysFree != 0) return;
            if (state.gatewayConstructions > 0) return;
        }
        else {
            boolean allBusy = state.gatewaysFree == 0;
            if (allBusy) lastAllBusySecond = state.seconds;
            if (!allBusy
                && (lastAllBusySecond < 0 || state.seconds - lastAllBusySecond > 10)) return;

            if (state.minerals < GATEWAY_BANK_MINERALS) return;
        }

        goals.add(new ProductionGoal(
            UnitProducible.of(AUnitType.Protoss_Gateway),
            ProductionGoal.PRIORITY_NORMAL, 1, 0, TargetPlacement.anywhere()));
    }

    /** Third Gateway no earlier than this: the BO owns the opening. */
    private static final int GATEWAY_DEMAND_SUPPLY = 30;

    /**
     * The last game-second every Gateway stood busy. The rich-plus-busy gate
     * below fires on this, not only on a simultaneously-busy snapshot: with
     * coarse evaluation (sim strides, re-issue gaps) the all-busy instant
     * rarely lands on the same frame as a rich bank, while per-frame live
     * play sees it constantly (measured: stride-60 sim never fires, stride-1
     * sim fires - same game, same rules). A 10 s window keeps the intent
     * (demand exceeds capacity) without demanding simultaneity.
     */
    private static int lastAllBusySecond = Integer.MIN_VALUE;

    public static void clearLatchForTests() {
        lastAllBusySecond = Integer.MIN_VALUE;
    }

    /** Rich-only rule: a new Gateway never starves tech or units. */
    private static final int GATEWAY_BANK_MINERALS = 400;

    /** Third Gateway bank floor (owner 2026-11-04): saturated pair, 225+. */
    private static final int GATEWAY_THIRD_BANK_MINERALS = 225;

    /**
     * Baseline army: always some combat units once we have a base and a
     * facility that can train them. The count is a floor, not a ceiling - the
     * strategic layer raises it through its own goals, and this generator exists
     * so the bot is never caught with an empty army while tech is researched.
     */
    private static void addArmyGoal(List<ProductionGoal> goals, GameSnapshot state, ProductionRacePolicy race) {
        // Below this the economy cannot support an army at all; the worker and
        // supply goals must win first.
        if (state.supplyUsed < 12)
            return;

        // Max-supply cap (legacy AllowProduce gates): basics never close the
        // game out, so they stop at 188 while tech and eyes keep flowing
        // through their own goals.
        if (state.supplyUsed >= 188)
            return;

        // Owner 2026-10-19: Dragoons ASAP. The moment the Core stands and
        // 50 gas is banked the advanced unit is wanted regardless of how
        // many Zealots stand - the old floor counted every army unit
        // against the transition and froze it forever behind a Zealot
        // army. Without gas for Dragoons, Zealots are a 275-mineral dump,
        // not a trickle - and pre-Core the opening Zealots flow ungated
        // (no mineral gate before the Core exists to save for).
        //
        // Tight-supply veto (legacy AllowProduceDragoon): at 184+ with no
        // room (4 or less free) or no gas behind it (200 or less) the next
        // Dragoon waits - unless the bank is rich (500+), which buys
        // through any veto. Saving for Dark Templar vetoes below 350.
        boolean wantGoons = state.hasCore && state.gas >= 50
            && !(state.minerals < 500 && state.supplyUsed >= 184
                && (state.supplyFree <= 4 || state.gas <= 200))
            && !(state.savingForDarkTemplar && state.minerals < 350);

        // Ling-flood mix (legacy ZealotInsteadDragoon): with 4+ Dragoons
        // standing against 4+ lings the line wants Zealots instead - a
        // 9-zealot head start, then ratio 0.8. The dead guard
        // (zealots>=2 && dragoons<=2) never fires past the 4-Dragoon gate
        // and stays out. The pump below reads the same flag: a mix frame
        // trains Zealots, never Dragoons.
        boolean mixZealot = wantGoons && state.enemyIsZerg && state.zerglings >= 4 && state.dragoons >= 4
            && (state.zealots <= 9
                || ((double) state.zealots) / (state.dragoons + 0.1) < 0.8);
        if (mixZealot) {
            wantGoons = false;
        }
        // The mix trains through its own goal (a replacement rule, not a
        // floor top-up): while the flood holds, Zealots - never the
        // Dragoon the pump would have trained. A free Gateway gates it
        // for the same reason it gates the pump.
        if (mixZealot && state.gatewaysFree > 0) {
            goals.add(new ProductionGoal(
                UnitProducible.of(race.armyUnitType()),
                ProductionGoal.PRIORITY_MAINARMY,
                1,
                0,
                TargetPlacement.anywhere()));
        }
        AUnitType army;
        int have;
        if (wantGoons) {
            army = race.armyUnitTypeAdvanced();
            have = state.dragoons;
        }
        else {
            if (state.hasCore && state.minerals < 275)
                return;

            // Late-game Zealot brakes (legacy AllowProduceZealot): at 180+
            // with gas banked or an army standing the Zealot stops (tech
            // eats the minerals instead), and at 175+ with no room the next
            // one waits. Saving for Dark Templar vetoes at 300 or below.
            if (state.supplyUsed >= 180
                && (state.gas >= 100 || state.armyStrength >= 130))
                return;
            if (state.supplyUsed >= 175 && state.supplyFree <= 4)
                return;
            if (state.savingForDarkTemplar && state.minerals <= 300)
                return;

            army = race.armyUnitType();
            have = state.armySize;
        }
        if (army == null)
            return;

        // Mid-game Dragoon pump (owner: no combat units stall past the mid
        // game): evaluated INDEPENDENTLY of the floor below - the floor
        // returns early once its count is met, and the pump must keep
        // firing exactly then (a met floor with idle Gateways is the
        // stall). Past the opening, a rich bank with a free Gateway trains
        // Dragoons continuously: the bank is the governor (never starves
        // tech), the free Gateway is the signal (never queues behind busy
        // facilities). Symmetric with GatewayDemand (rich+busy->build):
        // rich+free->train, and the two together form the self-regulating
        // loop. Dragoon-only by construction (hasCore is Protoss-only).
        if (!state.inEarlyGame && !mixZealot && state.hasCore && state.gas >= 50
            && state.minerals >= ARMY_RICH_MINERALS && state.gatewaysFree > 0) {
            goals.add(new ProductionGoal(
                UnitProducible.of(race.armyUnitTypeAdvanced()),
                ProductionGoal.PRIORITY_MAINARMY,
                1,
                0,
                TargetPlacement.anywhere()));
        }

        int targetArmy = Math.max(1, state.workers / 4);
        int missing = targetArmy - have;
        if (missing <= 0)
            return;

        goals.add(new ProductionGoal(
            UnitProducible.of(army),
            ProductionGoal.PRIORITY_MAINARMYBASE,
            missing,
            0,
            TargetPlacement.anywhere()));
    }

    /**
     * Rich-only rule, mirroring {@link #GATEWAY_BANK_MINERALS}: the pump
     * never starves tech - a Dragoon plus tech headroom always stays banked.
     */
    private static final int ARMY_RICH_MINERALS = 400;

    /**
     * Tech: Singularity Charge once Dragoons exist. Mirrors the legacy
     * trigger minus readiness (bank policy and in-flight dedup stay
     * execution-side: the wallet skips what the bank cannot cover, the
     * director refuses duplicate research). The Core itself is not named
     * here - the scheduler inserts it as a prerequisite from engine data.
     * Dragoons-gated, so other races stay silent without a race check.
     */
    private static void addTechGoal(List<ProductionGoal> goals, GameSnapshot state) {
        if (state.singularity)
            return;

        boolean want = state.dragoons >= 4
            || (state.gas >= 80 && (state.gas >= 180 || state.dragoons >= 6))
            || (state.dragoons >= 2 && state.supplyUsed >= 38
            && state.armyStrength >= 120 && state.minerals >= 240);
        if (!want)
            return;

        goals.add(new ProductionGoal(
            atlantis.production.producibles.UpgradeProducible.of(bwapi.UpgradeType.Singularity_Charge),
            ProductionGoal.PRIORITY_NORMAL,
            1,
            0,
            TargetPlacement.anywhere()));
    }

    /**
     * Gas: the next refinery once two bases mine and the bank shapes like
     * the legacy commander's (supply, mineral-vs-gas, army and core gates).
     * The queue-cost clause is deliberately not mirrored: the scheduler's
     * in-flight dedup already refuses a second refinery while one is coming.
     * Placement is exact-geyser (the planner's gas branch), never a search.
     */
    private static void addGasGoal(List<ProductionGoal> goals, GameSnapshot state, ProductionRacePolicy race) {
        AUnitType gas = race.gasBuildingType();
        if (gas == null)
            return;

        if (state.supplyUsed < 40)
            return;

        if (state.bases < 2)
            return;

        if (!(state.gas < state.minerals && state.minerals >= 105))
            return;

        if (!(state.gas <= 150 || state.armySize >= 12))
            return;

        if (!(state.bases > state.gasBuildings))
            return;

        if (!(state.minerals >= 400 || state.hasCore))
            return;

        goals.add(new ProductionGoal(
            UnitProducible.of(gas),
            ProductionGoal.PRIORITY_GAS,
            1,
            0,
            TargetPlacement.anywhere()));
    }

    /**
     * Owner order 2026-11-04 (Zealot-into-Goon build order): follow the
     * build order - a dynamic tech building (Core, first Assimilator)
     * mints only once the BO has no unfinished rows left, or the bank
     * covers it three times over (300+). Measured live: a dynamic Core
     * at ~supply 14 jumped the BO second Gateway (At 12, warped at 25)
     * in three consecutive losses, and the Assimilator followed it -
     * both rows the BO lists at 22/23. Pure over the snapshot, so the
     * rule pins without a game.
     */
    static boolean dynamicTechBuildingsAllowed(GameSnapshot state) {
        return state.minerals >= 300 || state.buildOrderDone;
    }

    /**
     * B1 (owner order): the V2 home for the Cybernetics Core building.
     * The legacy producer waited for minerals 210 or a supply-25 army
     * (or a gas building already up); V2 fires on the affordable-now
     * bank once a Gateway stands, so the tech unlock never waits on a
     * BO row. In-flight dedup rides the scheduler's committed set, the
     * same as every other building goal.
     */
    private static void addCoreBuildingGoal(List<ProductionGoal> goals, GameSnapshot state) {
        if (!We.protoss()) return;
        if (state.hasCore) return;
        if (state.gatewaysExisting < 1) return;
        if (state.minerals < AUnitType.Protoss_Cybernetics_Core.mineralPrice()) return;
        if (!dynamicTechBuildingsAllowed(state)) return;

        goals.add(new ProductionGoal(
            UnitProducible.of(AUnitType.Protoss_Cybernetics_Core),
            ProductionGoal.PRIORITY_NORMAL,
            1,
            0,
            TargetPlacement.anywhere()));
    }

    /**
     * B1 (owner order): the V2 home for the first Assimilator. The legacy
     * producer queued gas once the Core was nearly done (under 80 warp
     * seconds left) instead of waiting for it to finish; the same sight
     * lives on the snapshot now. Single-base openings included: unlike
     * the multi-base gas goal above, this one answers the first refinery.
     */
    private static void addFirstAssimilatorGoal(List<ProductionGoal> goals, GameSnapshot state,
                                               ProductionRacePolicy race) {
        AUnitType gas = race.gasBuildingType();
        if (gas == null) return;
        if (state.gasBuildings > 0) return;
        if (state.bases < 1) return;
        boolean coreNearlyDone = state.coreRemainingSeconds >= 0 && state.coreRemainingSeconds < 80;
        if (!state.hasCore && !coreNearlyDone) return;
        if (!dynamicTechBuildingsAllowed(state)) return;

        goals.add(new ProductionGoal(
            UnitProducible.of(gas),
            ProductionGoal.PRIORITY_GAS,
            1,
            0,
            TargetPlacement.anywhere()));
    }

    /**
     * Expansion: one more base is wanted as soon as the current one is close to
     * saturated. Expressed as a goal, so the timeline decides whether the 400
     * minerals belong to the expansion or to the defence this frame - the old
     * commander decided that by issuing the order first and letting the queue
     * sort it out.
     */
    private static void addExpansionGoal(List<ProductionGoal> goals, GameSnapshot state, ProductionRacePolicy race) {
        AUnitType base = race.baseType();
        if (base == null)
            return;

        // A base already under construction is not missing: without this the
        // generator asks for a second Nexus every frame while the first is
        // being built.
        if (state.basesExisting > state.bases)
            return;

        // One doctrine, three readers (owner 2026-11-01): the Protoss bank
        // rule fires here, not only in the legacy commander - a banked 777
        // used to sit until 25 workers arrived (measured live: 1700 banked
        // frames before the f5952 Nexus). WANT names the natural for the
        // second base, anywhere after; THICKET keeps the old saturation
        // rule; HOLD stays silent.
        ExpansionTiming.Snapshot timing = new ExpansionTiming.Snapshot();
        timing.minerals = state.minerals;
        timing.basesTotal = state.basesTotal;
        timing.basesInProduction = state.basesInProduction;
        timing.basesPlanned = state.basesPlanned;
        timing.basesUnfinished = state.basesUnfinished;
        timing.enemyZerg = state.enemyIsZerg;
        timing.enemyProtoss = state.enemyIsProtoss;
        timing.enemyTerran = state.enemyIsTerran;
        timing.enemyMutalisks = state.enemyMutalisks;
        timing.cannons = state.cannonsAtMain;
        timing.armyStrength = state.armyStrength;
        timing.seconds = state.seconds;
        ExpansionTiming.Verdict verdict = ExpansionTiming.decide(timing);

        if (verdict == ExpansionTiming.Verdict.HOLD)
            return;

        if (verdict == ExpansionTiming.Verdict.WANT_BANK) {
            goals.add(new ProductionGoal(
                UnitProducible.of(base),
                ProductionGoal.PRIORITY_NORMAL,
                1,
                0,
                state.bases < 2
                    ? TargetPlacement.namedArea("NATURAL")
                    : TargetPlacement.anywhere()));
            return;
        }

        if (state.workers < WORKERS_PER_BASE * state.bases)
            return;

        if (state.bases >= 4)
            return;

        goals.add(new ProductionGoal(
            UnitProducible.of(base),
            ProductionGoal.PRIORITY_NORMAL,
            1,
            0,
            TargetPlacement.anywhere()));
    }

    /**
     * The few facts a goal decision needs. Deliberately primitive: this is the
     * seam that keeps the goal layer testable, and it is intentionally not a
     * window onto {@code Select}/{@code Count} - if a decision needs more than
     * these numbers, it does not belong in this layer.
     */
    public static final class GameSnapshot {
        public final int workers;
        public final int bases;
        /**
         * Bases finished, under construction and pending - what counts as "we have one".
         */
        public final int basesExisting;
        public final int supplyUsed;
        public final int supplyFree;
        public final int supplyTotal;
        public final int minerals;
        /**
         * Gas bank: Dragoons, tech and the second gas all read it.
         */
        public final int gas;
        public final int armySize;
        public final boolean inEarlyGame;

        /**
         * Advanced army unlocked (Protoss: Cybernetics Core standing; zeros
         * for other races until their policies fill equivalents). Gates the
         * Zealot-to-Dragoon advancement and the Singularity want.
         */
        public final boolean hasCore;
        /**
         * Dragoons standing (Protoss; 0 elsewhere). Sizes the tech want.
         */
        public final int dragoons;
        /**
         * Gas buildings standing (Protoss: Assimilators; 0 elsewhere).
         */
        public final int gasBuildings;
        /**
         * Singularity Charge researched (Protoss; false elsewhere).
         */
        public final boolean singularity;
        /**
         * Army strength for tech/economy gates.
         */
        public final int armyStrength;

        /**
         * Cannon count already standing at the main base.
         */
        public final int cannonsAtMain;
        /**
         * Enemy race facts the fortification policy reads.
         */
        public final boolean enemyIsZerg;
        public final boolean enemyIsProtoss;
        public final int enemyMutalisks;

        /**
         * The base the fortification goals are anchored to; -1 when unknown.
         */
        public final int mainBaseTileX;
        public final int mainBaseTileY;

        /**
         * Supply providers (Pylons/Depots/Overlords) already ordered and not yet
         * finished. Counting them is what stops the supply goal being re-emitted
         * every frame until one completes.
         */
        public int supplyProvidersComing;

        /**
         * Gateways existing, completed or not. Set post-construction like
         * {@link #supplyProvidersComing}: the build order owns the first
         * two rows, the 3rd-and-next Gateway author reads this (measured
         * live 183253: 2 Gateways the whole game, bank to 7963 while
         * Dragoon demand waited on busy facilities).
         */
        public int gatewaysExisting;

        /**
         * Expansion doctrine inputs (ProtossShouldExpand, verbatim): total
         * bases, in-production-or-queued, planned, unfinished. Set
         * post-construction like {@code gatewaysExisting}; the goal layer
         * never counts them itself.
         */
        public int basesTotal;
        public int basesInProduction;
        public int basesPlanned;
        public int basesUnfinished;
        public boolean enemyIsTerran;

        /**
         * Gateways free to take an order right now (same post-construction
         * pattern). A free Gateway trains first; a new one is wanted only
         * when every existing one is busy.
         */
        public int gatewaysFree;

        /**
         * Gateway constructions still underway (walking builder or warping,
         * not yet producing). The third Gateway waits for a saturated
         * completed pair - minting it behind a walking builder is how
         * 30/48 slides toward 38/65. Set post-construction like
         * {@link #gatewaysFree}.
         */
        public int gatewayConstructions;

        /**
         * Least warp seconds left on unfinished Cybernetics Cores, -1 when
         * none is warping (same post-construction pattern). The first
         * Assimilator times gas to a nearly-done Core the way the legacy
         * producer did (under 80 warp seconds left), instead of waiting
         * for it to finish.
         */
        public int coreRemainingSeconds = -1;

        /**
         * No unfinished build-order rows left (owner order 2026-11-04,
         * Zealot-into-Goon: follow the build order - dynamic tech
         * buildings wait for this or a 300+ bank). Set post-construction
         * from the live queue; unit tests pin the gate through this flag
         * instead of a queue.
         */
        public boolean buildOrderDone = false;

        /**
         * A Forge already standing (Protoss; other races leave it true so the
         * gate below stays a Protoss rule). Legacy never secured a base with
         * cannons before a Forge stood ({@code ProtossSecureBasesCommander}
         * gates on {@code Have.forge()}), and the V2 fortification port
         * dropped that gate - so a Cannon goal from frame 0 pulled an orphan
         * Forge ahead of the Gateway in openings whose build order has no
         * Forge (measured LIVE: Forge f2112-2746 before Gateway in three
         * Zealot-into-Goon games, zero Cannons ever completing). Set
         * post-construction like {@link #supplyProvidersComing}.
         */
        public boolean hasForge;

        /**
         * Enemy pressure the Stargate branch answers to (Zerg air play).
         * Fed live from the discovered counts; zero when nothing is seen.
         */
        public int enemyHydras;
        public int enemyLurkers;

        /**
         * Discovered carriers or a Fleet Beacon going up (the Protoss
         * branch of the Stargate want). Collapsed to one bit, the way the
         * board feeds it - consumers never run two unit queries.
         */
        public boolean enemyCarrierTech;

        /**
         * Own combat units standing (the Zerg branch refuses a Stargate
         * for a toothless line even with the supply for it).
         */
        public int combatUnits;

        /**
         * Corsairs standing (the pump replaces losses, it never stockpiles
         * eyes ahead of the want).
         */
        public int corsairs;

        /**
         * A Stargate standing or warping (Protoss; false blocks the whole
         * Corsair chain - no eyes before the building that trains them).
         */
        public boolean hasStargate;

        /**
         * Observers standing (the pump scales eyes with the threat and
         * replaces losses, it never stockpiles).
         */
        public int observers;

        /**
         * Zealots standing and enemy zerglings seen: the vs-Zerg mix rule
         * trains Zealots instead of Dragoons into ling floods (ratio 0.8
         * with a 9-zealot head start once 4 Dragoons stand).
         */
        public int zealots;
        public int zerglings;

        /**
         * Upgrade levels standing (Protoss ground weapons/armor ladders
         * climb one level at a time; in-flight coverage is execution-side).
         */
        public int weaponsLevel;
        public int armorLevel;

        /**
         * Enemy anti-land combat buildings (the PvP gateway rush yields to
         * fortified enemies - same read the legacy prioritize used).
         */
        public int enemyCombatBuildings;

        /**
         * Game seconds (the too-weak-to-tech veto expires after minute 10 -
         * a late small army still techs).
         */
        public int seconds;

        /**
         * Observatory standing (the Citadel wants clearer eyes first) and
         * Citadel standing (Leg Enhancements research at it). Standing
         * only, verbatim with the legacy gates.
         */
        public boolean hasObservatory;
        public boolean hasCitadel;

        /**
         * Templar chain inputs: Archives standing (HT/DT train at it),
         * High Templar and Dark Templar standing, Support Bay standing
         * with Reavers standing (a bay with no Reaver yet outranks the
         * first DT - the gas builds the Reaver first).
         */
        public boolean hasArchives;
        public int highTemplar;
        public int darkTemplar;
        public boolean hasSupportBay;
        public int reavers;

        /**
         * Reaver chain inputs: Robotics Facility standing or warping
         * (Reavers and Shuttles train at it), Shuttles standing, Tribunal
         * standing (Arbiters need it).
         */
        public boolean hasRobo;
        public int shuttles;
        public boolean hasTribunal;

        /**
         * Arbiters standing (two behind a Tribunal, losses replaced).
         */
        public int arbiters;

        /**
         * Saving for Dark Templar: hidden-units tech is warping
         * (Citadel or Archives unfinished) while the strategy goes hidden.
         * Basics wait below their floors instead of eating the DT bank.
         */
        public boolean savingForDarkTemplar;

        /**
         * The enemy showed or is going hidden combat units (lurkers, Dark
         * Templar: seen, or predicted from Citadel/Archives/HydraDen).
         * Only Zerg and Protoss own the hidden set, so a set flag always
         * means one of those two. Set post-construction like
         * {@link #supplyProvidersComing}.
         */
        public boolean enemyHasHiddenUnits;

        public GameSnapshot(int workers, int bases, int supplyUsed, int supplyFree,
                            int supplyTotal, int minerals, boolean inEarlyGame) {
            this(workers, bases, supplyUsed, supplyFree, supplyTotal, minerals, inEarlyGame, 0, bases);
        }

        public GameSnapshot(int workers, int bases, int supplyUsed, int supplyFree,
                            int supplyTotal, int minerals, boolean inEarlyGame, int armySize, int basesExisting) {
            this(workers, bases, supplyUsed, supplyFree, supplyTotal, minerals, inEarlyGame, armySize,
                basesExisting, 0, false, false, 0);
        }

        /**
         * The full snapshot, including the fortification inputs. Kept as one
         * constructor rather than a builder: the goal layer is deliberately fed a
         * handful of numbers, and a test that needs to say "zerg, twelve mutas"
         * should be able to say exactly that.
         */
        public GameSnapshot(int workers, int bases, int supplyUsed, int supplyFree,
                            int supplyTotal, int minerals, boolean inEarlyGame, int armySize, int basesExisting,
                            int cannonsAtMain, boolean enemyIsZerg, boolean enemyIsProtoss, int enemyMutalisks) {
            this(workers, bases, supplyUsed, supplyFree, supplyTotal, minerals, inEarlyGame, armySize,
                basesExisting, cannonsAtMain, enemyIsZerg, enemyIsProtoss, enemyMutalisks,
                0, false, 0, 0, false, 0);
        }

        /**
         * The full snapshot plus tech/army inputs. The 13-argument form fills
         * zeros: a test that only cares about workers or supply keeps saying
         * exactly that, and tech goals stay silent on it.
         */
        public GameSnapshot(int workers, int bases, int supplyUsed, int supplyFree,
                            int supplyTotal, int minerals, boolean inEarlyGame, int armySize, int basesExisting,
                            int cannonsAtMain, boolean enemyIsZerg, boolean enemyIsProtoss, int enemyMutalisks,
                            int gas, boolean hasCore, int dragoons, int gasBuildings, boolean singularity,
                            int armyStrength) {
            this(workers, bases, supplyUsed, supplyFree, supplyTotal, minerals, inEarlyGame, armySize,
                basesExisting, cannonsAtMain, enemyIsZerg, enemyIsProtoss, enemyMutalisks,
                gas, hasCore, dragoons, gasBuildings, singularity, armyStrength, -1, -1);
        }

        private GameSnapshot(
            int workers, int bases, int supplyUsed, int supplyFree,
            int supplyTotal, int minerals, boolean inEarlyGame, int armySize, int basesExisting,
            int cannonsAtMain, boolean enemyIsZerg, boolean enemyIsProtoss, int enemyMutalisks,
            int gas, boolean hasCore, int dragoons, int gasBuildings, boolean singularity,
            int armyStrength,
            int mainBaseTileX, int mainBaseTileY) {
            this.workers = workers;
            this.bases = bases;
            this.basesExisting = basesExisting;
            this.supplyUsed = supplyUsed;
            this.supplyFree = supplyFree;
            this.supplyTotal = supplyTotal;
            this.minerals = minerals;
            this.gas = gas;
            this.armySize = armySize;
            this.hasCore = hasCore;
            this.dragoons = dragoons;
            this.gasBuildings = gasBuildings;
            this.singularity = singularity;
            this.armyStrength = armyStrength;
            this.inEarlyGame = inEarlyGame;
            this.cannonsAtMain = cannonsAtMain;
            this.enemyIsZerg = enemyIsZerg;
            this.enemyIsProtoss = enemyIsProtoss;
            this.enemyMutalisks = enemyMutalisks;
            this.mainBaseTileX = mainBaseTileX;
            this.mainBaseTileY = mainBaseTileY;
        }

        /**
         * The same snapshot anchored to a base, for the fortification policy. The
         * 13-argument form leaves the anchor unknown, which is what a test that only
         * cares about workers or supply wants - and what makes fortification emit
         * nothing rather than guess a location.
         */
        public GameSnapshot atBase(int tileX, int tileY) {
            GameSnapshot anchored = new GameSnapshot(
                workers, bases, supplyUsed, supplyFree, supplyTotal, minerals, inEarlyGame,
                armySize, basesExisting, cannonsAtMain, enemyIsZerg, enemyIsProtoss, enemyMutalisks,
                gas, hasCore, dragoons, gasBuildings, singularity, armyStrength,
                tileX, tileY
            );

            // A fresh construction drops the post-construction fields; carry
            // them across, or an anchored snapshot silently forgets the
            // Forge, the in-flight counts and the hidden-unit flag.
            anchored.supplyProvidersComing = supplyProvidersComing;
            anchored.gatewaysExisting = gatewaysExisting;
            anchored.coreRemainingSeconds = coreRemainingSeconds;
            anchored.basesTotal = basesTotal;
            anchored.basesInProduction = basesInProduction;
            anchored.basesPlanned = basesPlanned;
            anchored.basesUnfinished = basesUnfinished;
            anchored.enemyIsTerran = enemyIsTerran;
            anchored.gatewaysFree = gatewaysFree;
            anchored.hasForge = hasForge;
            anchored.enemyHasHiddenUnits = enemyHasHiddenUnits;
            anchored.enemyHydras = enemyHydras;
            anchored.enemyLurkers = enemyLurkers;
            anchored.enemyCarrierTech = enemyCarrierTech;
            anchored.combatUnits = combatUnits;
            anchored.corsairs = corsairs;
            anchored.hasStargate = hasStargate;
            anchored.observers = observers;
            anchored.zealots = zealots;
            anchored.zerglings = zerglings;
            anchored.weaponsLevel = weaponsLevel;
            anchored.armorLevel = armorLevel;
            anchored.enemyCombatBuildings = enemyCombatBuildings;
            anchored.seconds = seconds;
            anchored.hasObservatory = hasObservatory;
            anchored.hasCitadel = hasCitadel;
            anchored.hasArchives = hasArchives;
            anchored.highTemplar = highTemplar;
            anchored.darkTemplar = darkTemplar;
            anchored.hasSupportBay = hasSupportBay;
            anchored.reavers = reavers;
            anchored.hasRobo = hasRobo;
            anchored.shuttles = shuttles;
            anchored.hasTribunal = hasTribunal;
            anchored.arbiters = arbiters;
            anchored.savingForDarkTemplar = savingForDarkTemplar;

            return anchored;
        }
    }

}
