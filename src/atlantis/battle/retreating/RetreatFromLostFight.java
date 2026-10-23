package atlantis.battle.retreating;

import atlantis.battle.Behavior;
import atlantis.battle.cluster.Alpha;
import atlantis.battle.cluster.ClusterBreach;
import atlantis.battle.cluster.ClusterRally;
import atlantis.battle.cluster.FightOrFlight;
import atlantis.battle.escape.EscapeScoredStep;
import atlantis.battle.formation.SharedEscape;
import atlantis.battle.kite.CombatTuning;
import atlantis.battle.targeting.MeleeChewerTarget;
import atlantis.game.A;
import atlantis.map.position.APosition;
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
            // Grabbed with a ready shot (owner, Steamhammer loss): a
            // melee unit holding an enemy in weapon range with cooldown
            // <= 5 never marches - the shot lands now, the march donates
            // it. Falls through to fight instead.
            if (unit.isMelee() && unit.cooldown() <= MeleeChewerTarget.READY_COOLDOWN_FRAMES) {
                AUnit grabber = CutoffRetreat.nearestEnemyCombatUnit(unit);
                if (grabber != null && grabber.isAlive() && unit.hasWeaponRangeToAttack(grabber, 1)) {
                    return false;
                }
            }

            // Cut off from the army with enemies on it, a unit never
            // marches to the far rally through the pack (owner
            // 2026-10-26, tested loss vs Protoss): if it can hit back it
            // fights instead, otherwise it steps away locally - donating
            // the march home is how reinforcements die without firing.
            AUnit threat = CutoffRetreat.threatIfCutOff(unit);
            if (threat != null) {
                if (unit.hasWeaponRangeToAttack(threat, 1)) return false;

                APosition step = EscapeScoredStep.pickScored(unit, threat, 8);
                if (step != null) {
                    if (isChased(unit)) {
                        unit.move(step, Actions.RUN_ENEMY, Behavior.FLEE.tag());
                    }
                    else {
                        unit.move(step, Actions.MOVE_FORMATION, Behavior.ORGANIZED_RETREAT.tag());
                    }

                    return true;
                }

                return false;
            }

            HasPosition home;
            if (Select.main() != null) {
                home = ClusterBreach.breachRallyPoint();
                // A bigger army never retreats into the base (owner
                // 2026-10-26, from a tested loss vs Protoss): it piles
                // onto the mineral line and blocks itself - it forms up
                // at the choke standoff instead.
                if (Alpha.get().size() > CombatTuning.current().getRetreatFormationMinArmy()) {
                    HasPosition standoff = ClusterRally.chokeStandoffPoint();
                    if (standoff != null) home = standoff;
                }
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
     * A chase is a visible armed enemy that can hit this unit inside its
     * range + 3 tiles (owner 2026-10-23, NEXT #187 - the same band as the
     * scout rule). Without one there is nothing to scatter from - the
     * retreat walks home in formation instead (ClusterOrganizedRetreat).
     * NOTE: enemy-centric on purpose - our ability to hit back is
     * irrelevant; a Guardian outranging us still chases us.
     */
    private static final double CHASE_MARGIN_TILES = 3;

    private static boolean isChased(AUnit unit) {
        return unit.enemiesNear().havingWeapon().canAttack(unit, CHASE_MARGIN_TILES).notEmpty();
    }
}
