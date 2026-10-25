package atlantis.combat.retreating;

import atlantis.combat.Behavior;
import atlantis.battle.DuelMemory;
import atlantis.battle.cluster.Alpha;
import atlantis.combat.cluster.ClusterBreach;
import atlantis.combat.cluster.ClusterRally;
import atlantis.combat.cluster.FightOrFlight;
import atlantis.combat.escape.EscapeScoredStep;
import atlantis.combat.formation.SharedEscape;
import atlantis.battle.kite.CombatTuning;
import atlantis.combat.targeting.MeleeChewerTarget;
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
        Verdict verdict = decide(unit, tactic, anchor, leader);
        if (verdict.dest == null) return false;

        if (isChased(unit)) {
            unit.move(verdict.dest, Actions.RUN_ENEMY, Behavior.FLEE.tag());
        }
        else {
            unit.move(verdict.dest, Actions.MOVE_FORMATION, Behavior.ORGANIZED_RETREAT.tag());
        }

        DuelMemory.noteRetreated(unit);

        return true;
    }

    /** One decision with its destination, or a null destination to fight in place. */
    public static final class Verdict {
        public final FleeReason reason;
        public final HasPosition dest;

        Verdict(FleeReason reason, HasPosition dest) {
            this.reason = reason;
            this.dest = dest;
        }
    }

    /**
     * The single classification point: which flee reason owns this unit
     * this frame, and where it marches (null = stay and fight). Test
     * seam and the only place new flee conditions land (CONVENTIONS
     * §31). retreat() only executes the verdict, never re-decides.
     */
    public static Verdict decide(
        AUnit unit, FightOrFlight.Tactic tactic, HasPosition anchor, AUnit leader
    ) {
        if (tactic != FightOrFlight.Tactic.FLEE) return new Verdict(FleeReason.STAY_TACTIC, null);
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
                    return new Verdict(FleeReason.STAY_MELEE_GRAB, null);
                }
            }

            // Cut off from the army with enemies on it, a unit never
            // marches to the far rally through the pack (owner
            // 2026-10-26, tested loss vs Protoss): if it can hit back it
            // fights instead, otherwise it steps away locally - donating
            // the march home is how reinforcements die without firing.
            AUnit threat = CutoffRetreat.threatIfCutOff(unit);
            // Run-escalation (NEXT #190): a swamped cutoff fight is
            // already lost - trading it donates the unit, so it marches
            // home with the rally below instead of taking the verdicts.
            boolean runHome = threat != null && CutoffRetreat.hopelessStand(unit);
            if (threat != null && !runHome) {
                if (unit.hasWeaponRangeToAttack(threat, 1)) {
                    return new Verdict(FleeReason.FIGHT_CUTOFF, null);
                }

                APosition step = EscapeScoredStep.pickScored(unit, threat, 8);
                if (step != null) {
                    return new Verdict(FleeReason.STEP_CUTOFF, step);
                }

                // Stuck with chewers on it is dying, not trading: the
                // last-stand gun below is only for unchewed units.
                // Chewed ones fall through to the rally and run.
                if (!atlantis.combat.kite.Kite.isBeingChewed(unit)) {
                    return new Verdict(FleeReason.CORNERED_CUTOFF, null);
                }
            }

            HasPosition home;
            if (Select.main() != null) {
                home = ClusterBreach.breachRallyPoint();
                // A bigger army never retreats into the base (owner
                // 2026-10-26, from a tested loss vs Protoss): it piles
                // onto the mineral line and blocks itself - it forms up
                // at the choke standoff instead.
                // Vs Protoss the bar is lower and absolute (owner):
                // with 4+ combat units the army holds the main choke
                // as usual, even outmatched - no retreat home, ever.
                boolean bigArmy = Alpha.get().size() > CombatTuning.current().getRetreatFormationMinArmy();
                boolean protossStand =
                    atlantis.game.player.Enemy.protoss() && Alpha.get().size() >= 4;
                if (bigArmy || protossStand) {
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
            // Retreat floor (owner 2026-10-25): a retreat stops at the
            // middle-choke line, it never runs all the way home.
            home = RetreatFloor.clamp(home);
            HasPosition point = ClusterRally.gatherPointFor(unit, leader, home, tactic);
            if (point != null && unit.distTo(point) >= 0.2) {
                return new Verdict(FleeReason.RALLY, point);
            }

            return new Verdict(FleeReason.NOWHERE_TO_RUN, null);
        }
        if (noFleeOnBaselessUms) return new Verdict(FleeReason.STAY_BASELESS, null);

        return new Verdict(FleeReason.STAY_MID_SWING, null);
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
