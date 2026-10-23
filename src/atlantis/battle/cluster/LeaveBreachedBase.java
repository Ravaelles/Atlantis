package atlantis.battle.cluster;

import atlantis.battle.Behavior;
import atlantis.battle.kite.CombatTuning;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

/**
 * The single retreat hatch on a breached base, or false to keep fighting.
 * Extracted from BattleCommander: a unit this close to death with no shot
 * ready may leave - everything else fights. Per-unit, never the cluster.
 */
public final class LeaveBreachedBase {

    private LeaveBreachedBase() {
    }

    public static boolean leave(AUnit unit) {
        // The only retreat allowed on a breached base (owner): a unit this
        // close to death with no shot ready may leave - everything else
        // fights. Per-unit, never the cluster: tactic() above already
        // committed the cluster to FIGHT, this is the individual hatch
        // for units caught far out. Close to home it must NOT fire
        // (owner 2026-10-26, tested loss vs Protoss): fleeing next to the
        // base just shuffles the unit among our own buildings instead of
        // retreating - close units keep fighting.
        // Mid-swing it waits one frame like every other move here: cancelling
        // the shot to flee is the same bug as cancelling it to kite.
        if (unit.groundDistToMain() < CombatTuning.current().getBreachLeaveMinDistTiles()) return false;
        if (
            ClusterBreach.breachForcesFight()
                && !unit.isAttackFrame() && !unit.isStartingAttack()
                && unit.hp() <= CombatTuning.current().getBreachRetreatHp()
                && unit.cooldown() >= CombatTuning.current().getBreachRetreatCooldown()
        ) {
            HasPosition rally = ClusterBreach.breachRallyPoint();
            if (rally != null && unit.groundDist(rally) >= 15) {
                unit.move(rally, Actions.RUN_ENEMY, Behavior.BREACH_RETREAT.tag());

                return true;
            }
        }
        return false;
    }
}
