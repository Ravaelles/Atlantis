package atlantis.battle;

import atlantis.battle.twins.FightTrace;

import atlantis.battle.kite.CombatTuning;

import atlantis.battle.cluster.FightOrFlight;
import atlantis.battle.cluster.LeaveBreachedBase;
import atlantis.battle.retreating.RetreatFromLostFight;
import atlantis.battle.strategy.BattleStrategies;
import atlantis.battle.strategy.BattleStrategy;
import atlantis.battle.strategy.ChokeCollapse;
import atlantis.battle.strategy.ChokeStandoff;
import atlantis.battle.strategy.SquadScout;
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

/**
 * Owns every army unit each frame. Phase 0 of __PLAN-COMBAT step 5: one
 * cluster (Alpha), one posture (main base or enemy base), one gate
 * (evaluator fight/flee with a retreat latch). No per-unit decisions.
 */
public class BattleCommander extends Commander {

    /** Per-game reset next to the other static resets in OnGameStarted. */
    public static void reset() {
        ClusterTactic.reset();
        CombatTuning.resetToDefaults();
        FormationSlots.clearForTests();
        SharedEscape.clearForTests();
        FightTrace.clearForTests();
        FightTarget.clearForTests();
    }

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
            atlantis.util.log.EventLog.key(atlantis.util.log.LogKey.CLUSTER)
                .param("cluster", Alpha.get().size())
                .param("posture", posture.name())
                .param("tactic", tactic.name())
                .param("breached", ClusterBreach.breachForcesFight())
                .param("anchor", anchor == null ? "-" : anchor.toString())
                .log(A.now(), A.minSec());
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
     * never interrupted, otherwise it attacks or regroups.
     */
    public static void act(AUnit unit, FightOrFlight.Tactic tactic, HasPosition anchor, AUnit leader) {
        if (RetreatFromLostFight.retreat(unit, tactic, anchor, leader)) return;
        if (LeaveBreachedBase.leave(unit)) return;
        BattleStrategy strategy = BattleStrategies.pick(unit, anchor);
        AUnit target = AcquireTarget.acquire(unit);
        if (target != null) {
            FightTarget.fight(unit, target, strategy);
            return;
        }
        if (huntLastContact(unit)) return;
        holdFormation(unit, anchor, leader, strategy);
    }

    /** Lost contact on a baseless map: march the last engaged point, or false to hold. */
    private static boolean huntLastContact(AUnit unit) {
        // No enemy in reach: on baseless maps, hunt the last engaged
        // point instead of sitting (1Drag_v_1Zeal: brief contact, then
        // 9 min idle 60 tiles apart - neither side re-acquires). The point
        // is snapshotted while visible, so a vanished enemy still leaves
        // an address. No UMS check on purpose: the gate is contact memory
        // plus baselessness, so melee frame 0 and based maps can never
        // reach here. Gated beyond the 9-tile squad envelope: inside it
        // the fight logic above already decided (focus refusals included
        // - never overruled from here).
        if (Count.bases() == 0) {
            APosition last = FightTarget.lastContact(unit);
            if (last != null && unit.distTo(last) > 9) {
                if (unit.move(last, Actions.MOVE_ENGAGE, "ClusterHunt")) {
                    FightTarget.markRepositioned(unit);
                    return true;
                }
            }
        }
        return false;
    }

    /** Nothing to fight: bunch on the leader through the formation slot. */
    private static void holdFormation(AUnit unit, HasPosition anchor, AUnit leader, BattleStrategy strategy) {
        // Choke doctrine (NEXT #170): the decider owns the approach, this
        // only executes it - bait pulls into the funnel, the line holds
        // the standoff, everyone else bunches on the leader as before.
        if (strategy == BattleStrategy.COLLAPSE) {
            APosition collapse = ChokeCollapse.collapsePointFor(unit);
            if (collapse != null && unit.move(collapse, Actions.MOVE_ENGAGE, "ClusterCollapse")) {
                FightTarget.markRepositioned(unit);
                return;
            }
        }
        if (strategy == BattleStrategy.SCOUT_BAIT) {
            APosition bait = SquadScout.baitPointFor(unit);
            if (bait != null && unit.move(bait, Actions.MOVE_ENGAGE, "ClusterBait")) {
                FightTarget.markRepositioned(unit);
                return;
            }
        }
        else if (strategy == BattleStrategy.CHOKE_HOLD) {
            APosition hold = ChokeStandoff.holdPointFor(unit);
            if (hold != null && unit.move(hold, Actions.MOVE_ENGAGE, "ClusterStandoff")) {
                FightTarget.markRepositioned(unit);
                return;
            }
        }
        // No enemy in reach: take a formation slot when the cluster
        // forms up (3+ combat units, known pivot), else bunch on the
        // leader so the cluster advances (or retreats) as one instead
        // of a line (owner 2026-10-19: march and retreat both strung
        // out). The leader sets the pace; stragglers catch up instead
        // of everyone racing the anchor.
        HasPosition point = ClusterRally.formationSlotFor(unit, leader, anchor);
        if (point == null) point = ClusterRally.gatherPointFor(unit, leader, anchor);
        if (point != null && unit.move(point, Actions.MOVE_ENGAGE, "ClusterMove")) {
            FightTarget.markRepositioned(unit);
        }
    }


    public static void clearForTests() {
        CombatTuning.resetToDefaults();
        FormationSlots.clearForTests();
        SharedEscape.clearForTests();
        FightTrace.clearForTests();
        FightTarget.clearForTests();
        StickyTarget.clearForTests();
    }

}
