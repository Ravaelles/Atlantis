package atlantis.battle;

import atlantis.battle.twins.FightTrace;

import atlantis.battle.kite.CombatTuning;

import atlantis.battle.cluster.FightOrFlight;
import atlantis.battle.cluster.LeaveBreachedBase;
import atlantis.battle.retreating.RetreatFromLostFight;
import atlantis.battle.strategy.BattleStrategies;
import atlantis.battle.strategy.BattleStrategy;
import atlantis.battle.strategy.GhostAdvance;
import atlantis.battle.strategy.HuntLastContact;
import atlantis.battle.targeting.AcquireTarget;
import atlantis.battle.targeting.StickyTarget;

import atlantis.battle.cluster.Missions;

import atlantis.battle.cluster.ClusterBreach;
import atlantis.battle.cluster.ClusterMission;
import atlantis.battle.cluster.ClusterRally;
import atlantis.battle.cluster.ClusterTactic;

import atlantis.battle.cluster.ClusterAnchor;

import atlantis.battle.cluster.Alpha;

import atlantis.architecture.Commander;
import atlantis.architecture.CommanderFactory;
import atlantis.battle.formation.FormationSlots;
import atlantis.battle.formation.SharedEscape;
import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;
import atlantis.util.log.EventLog;
import atlantis.util.log.LogKey;

/**
 * Owns every army unit each frame. Phase 0 of __PLAN-COMBAT step 5: one
 * cluster (Alpha), one posture (main base or enemy base), one gate
 * (evaluator fight/flee with a retreat latch). No per-unit decisions.
 */
public class BattleCommander extends Commander {

    @Override
    protected CommanderFactory[] subcommanders() {
        return new CommanderFactory[]{};
    }

    @Override
    protected boolean handle() {
        Alpha.gather();
        if (Alpha.get().isEmpty()) return false;

        ClusterMission.Posture posture = ClusterMission.postureFor(
            Count.ourCombatUnits(), ClusterBreach.breachForcesFight()
        );

        if (posture == ClusterMission.Posture.ATTACK) {
            Missions.forceGlobalMissionAttack("cluster-attack");
        }
        else {
            Missions.forceGlobalMissionDefend("cluster-defend");
        }

        FightOrFlight.Tactic tactic = ClusterTactic.decide();
        HasPosition anchor = posture == ClusterMission.Posture.ATTACK
            ? ClusterAnchor.attackAnchor() : ClusterAnchor.defendAnchor();

        // Throttled state line: the only per-game proof of what the
        // cluster does (posture, tactic, size, anchor). JSON-only, stderr.
        if (A.everyNthGameFrame(31)) {
            traceCluster(posture, tactic, anchor);
        }

        for (AUnit unit : Alpha.get().selection().list()) {
            order(unit, tactic, anchor, Alpha.get().leader());
        }

        return false;
    }

    private void order(AUnit unit, FightOrFlight.Tactic tactic, HasPosition anchor, AUnit leader) {
        if (unit == null || !unit.isAlive()) return;

        act(unit, tactic, anchor, leader);
    }

    /**
     * One unit's act for the frame, extracted so the composition pins
     * without a game: FLEE with nowhere to go falls through to the
     * fight logic (no DoNothing deaths on baseless maps), a cooling
     * Dragoon with a close threat steps back, a mid-animation unit is
     * never interrupted, otherwise it attacks, hunts or regroups.
     */
    public static void act(AUnit unit, FightOrFlight.Tactic tactic, HasPosition anchor, AUnit leader) {
        atlantis.battle.twins.UnitTelemetry.sample(unit);
        if (RetreatFromLostFight.retreat(unit, tactic, anchor, leader)) return;
        if (LeaveBreachedBase.leave(unit)) return;

        BattleStrategy strategy = BattleStrategies.pick(unit, anchor);
        AUnit target = AcquireTarget.acquire(unit);
        if (target != null) {
            // Army-level contact for the split-cluster join (NEXT #181):
            // recorded at acquisition, not in fight(), because kiting
            // units never reach fight() yet still engage the enemy.
            // FIGHT only: a routing unit must not pull the army into it.
            if (tactic == FightOrFlight.Tactic.FIGHT) {
                atlantis.battle.ArmyContact.noteFight(unit, target);
            }
            FightTarget.fight(unit, target, strategy);
            return;
        }
        if (HuntLastContact.hunt(unit)) return;
        regroup(unit, tactic, anchor, leader, strategy);
    }

    /** Throttled CLUSTER twin: posture, tactic, size, anchor. JSON-only, stderr. */
    private static void traceCluster(
        ClusterMission.Posture posture, FightOrFlight.Tactic tactic, HasPosition anchor
    ) {
        EventLog.key(LogKey.CLUSTER)
            .param("cluster", Alpha.get().size())
            .param("posture", posture.name())
            .param("tactic", tactic.name())
            .param("breached", ClusterBreach.breachForcesFight())
            .param("anchor", anchor == null ? "-" : anchor.toString())
            .log(A.now(), A.minSec());
    }

    /**
     * Nothing to fight: the approach owns its regroup point (collapse joins
     * the bait at the funnel mouth, bait pulls, the line holds the standoff),
     * then the ghost advance creeps after a remembered contact instead of
     * standing (the hunt owns the far baseless case; inside it the gather
     * rule used to march the unit back home every frame), answered on the
     * enum so no call site switches on it. Without a strategy point or a
     * ghost the formation fallback owns the unit: take a formation slot when
     * the cluster forms up (3+ combat units, known pivot), else bunch on the
     * leader so the cluster advances (or retreats) as one instead of a line
     * (owner 2026-10-19: march and retreat both strung out). The leader sets
     * the pace; stragglers catch up instead of everyone racing the anchor.
     */
    private static void regroup(
        AUnit unit, FightOrFlight.Tactic tactic, HasPosition anchor, AUnit leader, BattleStrategy strategy
    ) {
        APosition regroup = strategy.regroupPointFor(unit);
        if (regroup != null && unit.move(regroup, Actions.MOVE_ENGAGE, strategy.regroupTag())) {
            FightTarget.markRepositioned(unit);
            return;
        }
        if (GhostAdvance.move(unit, tactic)) {
            return;
        }
        // Split-cluster reinforcement (NEXT #181): one part fights
        // while another holds formation slots with no target - the
        // idle part marches at the freshest fight instead of waiting.
        // Only on FIGHT (retreats own the unit above), only on UMS
        // (melee keeps its anchor/formation discipline: a scouting
        // skirmish must not drag the home army across the map), and
        // only when nothing else owned it yet (ghost first, static
        // slots after).
        if (tactic == FightOrFlight.Tactic.FIGHT && atlantis.game.A.isUms()) {
            APosition reinforce = atlantis.battle.ArmyContact.reinforcePointFor(unit);
            if (reinforce != null && unit.move(reinforce, Actions.MOVE_ENGAGE, "ClusterReinforce")) {
                FightTarget.markRepositioned(unit);
                return;
            }
        }
        HasPosition point = ClusterRally.formationSlotFor(unit, leader, anchor);
        if (point == null) point = ClusterRally.gatherPointFor(unit, leader, anchor);
        if (point != null && unit.move(point, Actions.MOVE_ENGAGE, "ClusterMove")) {
            FightTarget.markRepositioned(unit);
        }
    }

    /**
     * Per-game reset next to the other static resets in OnGameStarted.
     * Delegates to the test seam so the two lists cannot drift: the game
     * reset once forgot StickyTarget (stale last-seen frames leaked into
     * the next game and held fogged targets).
     */
    public static void reset() {
        ClusterTactic.reset();
        clearForTests();
    }

    public static void clearForTests() {
        CombatTuning.resetToDefaults();
        atlantis.battle.escape.EscapeTuning.resetToDefaults();
        atlantis.battle.strategy.GhostAdvance.clearForTests();
        FormationSlots.clearForTests();
        SharedEscape.clearForTests();
        FightTrace.clearForTests();
        atlantis.battle.twins.UnitTelemetry.clearForTests();
        atlantis.battle.ArmyContact.clearForTests();
        FightTarget.clearForTests();
        StickyTarget.clearForTests();
    }
}
