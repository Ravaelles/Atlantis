package atlantis.battle.retreating;

import atlantis.battle.Behavior;
import atlantis.battle.cluster.Alpha;
import atlantis.battle.cluster.ClusterBreach;
import atlantis.battle.cluster.ClusterRally;
import atlantis.battle.cluster.FightOrFlight;
import atlantis.battle.formation.SharedEscape;
import atlantis.battle.kite.CombatTuning;
import atlantis.battle.targeting.ASquadTargeting;
import atlantis.game.A;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;
import atlantis.units.select.Select;

/**
 * FLEE orders a retreat, or false when the unit falls through to fight.
 * Extracted from BattleCommander so the frame reads as a sentence; the
 * escalation ladder (shared direction, then sampling, then stand) lives
 * here, not in the composition.
 */
public final class RetreatFromLostFight {

    private RetreatFromLostFight() {
    }

    public static boolean retreat(
        AUnit unit, FightOrFlight.Tactic tactic, HasPosition anchor, AUnit leader
    ) {
        if (tactic != FightOrFlight.Tactic.FLEE) return false;
        // Owner: ClusterFlee is banned on baseless UMS maps - with no
        // base anywhere a retreat (shared direction or per-unit) only
        // donates the duel, so the unit falls through and fights.
        boolean noFleeOnBaselessUms = A.isUms() && Count.bases() == 0;
        // Baseless maps (UMS micro duels) have no home to run to: the
        // mission anchor there is the ENEMY base, so following it
        // marches the retreat straight into the enemy. Regroup away
        // from the threat instead; with nowhere to step, fall through
        // and fight in place. Based maps keep the choke-side rally.
        // Mid-swing the rally waits: a move order now cancels the shot
        // (PurpleWave AttackDelay) - the fight path below holds it the
        // same way.
        if (!noFleeOnBaselessUms && !unit.isAttackFrame() && !unit.isStartingAttack()) {
            HasPosition home;
            if (Select.main() != null) {
                home = ClusterBreach.breachRallyPoint();
            }
            else {
                // Formation Phase A: one shared escape direction per
                // cluster (leader, ~10f cadence) instead of per-unit
                // compass picks that break the line. Null falls back
                // to the per-unit sampling, unchanged.
                home = SharedEscape.rallyFor(
                    unit, leader, Alpha.get().selection(),
                    CombatTuning.current().getRegroupRallyTiles(), CombatTuning.current().getRegroupThreatTiles());
                if (home == null) home = ClusterRally.regroupRallyPoint(unit);
            }
            HasPosition point = ClusterRally.gatherPointFor(unit, leader, home, tactic);
            if (point != null && unit.distTo(point) >= 0.2) {
                if (isChased(unit)) {
                    unit.move(point, Actions.RUN_ENEMY, Behavior.FLEE.tag());
                }
                else {
                    unit.move(point, Actions.MOVE_FORMATION, Behavior.ORGANIZED_RETREAT.tag());
                }

                return true;
            }
        }
        // Nowhere to run to (baseless map, null anchor): standing
        // still with DoNothing until death is never the answer -
        // fall through and fight in place instead (kite included).

        return false;
    }

    /**
     * A chase is a visible armed enemy inside the regroup threat envelope
     * (owner 2026-10-23): without one there is nothing to scatter from -
     * the retreat walks home in formation instead (ClusterOrganizedRetreat).
     */
    private static boolean isChased(AUnit unit) {
        return ASquadTargeting.nearestEngageableEnemy(
            unit, CombatTuning.current().getRegroupThreatTiles()) != null;
    }
}
