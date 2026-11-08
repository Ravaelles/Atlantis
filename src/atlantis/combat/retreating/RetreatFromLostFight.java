package atlantis.combat.retreating;

import atlantis.battle.DuelMemory;
import atlantis.battle.cluster.Alpha;
import atlantis.battle.kite.CombatTuning;
import atlantis.combat.Behavior;
import atlantis.combat.cluster.ClusterBreach;
import atlantis.combat.cluster.ClusterRally;
import atlantis.combat.cluster.FightOrFlight;
import atlantis.combat.escape.EscapeScoredStep;
import atlantis.combat.formation.FormationSlots;
import atlantis.combat.formation.SharedEscape;
import atlantis.combat.targeting.MeleeChewerTarget;
import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;
import atlantis.units.select.Select;

import java.util.function.Function;

/**
 * FLEE orders a retreat, or false when the unit falls through to fight.
 * Extracted from BattleCommander so the frame reads as a sentence; the
 * escalation ladder (shared direction, then sampling, then stand) lives
 * here, not in the composition.
 */
public final class RetreatFromLostFight {

    /**
     * A chase is a visible armed enemy that can hit this unit inside its
     * range + 3 tiles (owner 2026-10-23, NEXT #187 - the same band as the
     * scout rule). Without one there is nothing to scatter from - the
     * retreat walks home in formation instead (ClusterOrganizedRetreat).
     * NOTE: enemy-centric on purpose - our ability to hit back is
     * irrelevant; a Guardian outranging us still chases us.
     */
    private static final double CHASE_MARGIN_TILES = 3;

    private RetreatFromLostFight() {
    }

    public static boolean retreat(
        AUnit unit, FightOrFlight.Tactic tactic, HasPosition anchor, AUnit leader
    ) {
        Verdict verdict = decide(unit, tactic, anchor, leader);
        if (verdict.dest == null) return false;

        // Displacement guarantee (owner 2026-10-29, Cere game): a retreat
        // that does not displace is not a retreat - dragoons piled into
        // each other for ~190f churning ClusterFlee/OrganizedRetreat/Kite
        // while dying. A retreating unit stuck in place steps away locally
        // (any walkable direction from the nearest threat) instead of
        // re-marching a far rally it never reaches.
        HasPosition dest = stuckRetreatLocalStep(unit, verdict.dest, A.now());

        if (isChased(unit)) {
            unit.move(dest, Actions.RUN_ENEMY, Behavior.FLEE.tag());
        }
        else {
            unit.move(dest, Actions.MOVE_FORMATION, Behavior.ORGANIZED_RETREAT.tag());
        }

        DuelMemory.noteRetreated(unit);

        return true;
    }

    /**
     * Stuck-retreat escape (test seam): a unit that has not displaced for
     * {@link #STUCK_RETREAT_FRAMES} while retreating gets a local step away
     * from the nearest enemy combat unit instead of the far rally. Fresh
     * and displacing units keep the rally; a stuck unit with no local
     * step keeps it too (same as today, never worse).
     */
    public static final int STUCK_RETREAT_FRAMES = 48;
    public static final double STUCK_DISPLACE_TILES = 0.5;
    static final int STUCK_WAYPOINT_STALE_FRAMES = 240;

    public static HasPosition stuckRetreatLocalStep(AUnit unit, HasPosition dest, int now) {
        try {
            // Committed escape direction first: march the same waypoint
            // until it arrives - re-picking every window oscillates the
            // unit in place (sim: 7 tiles survived instead of 8+ escaped).
            APosition committed = DuelMemory.retreatWaypoint(unit);
            if (committed != null) {
                if (unit.distTo(committed) <= 1.0) {
                    DuelMemory.clearRetreatWaypoint(unit);
                    DuelMemory.noteMoved(unit, now, unit.position());

                    return dest;
                }

                Integer waypointFrame = DuelMemory.retreatWaypointFrame(unit);
                if (waypointFrame == null || now - waypointFrame > STUCK_WAYPOINT_STALE_FRAMES) {
                    DuelMemory.clearRetreatWaypoint(unit);
                }
                else {
                    return committed;
                }
            }

            Integer lastMoved = DuelMemory.lastMovedFrame(unit);
            APosition lastPos = DuelMemory.lastMovedPos(unit);
            if (lastMoved == null || lastPos == null || unit.distTo(lastPos) >= STUCK_DISPLACE_TILES) {
                DuelMemory.noteMoved(unit, now, unit.position());

                return dest;
            }
            if (now - lastMoved < STUCK_RETREAT_FRAMES) return dest;

            AUnit threat = nearestEnemyCombatUnit(unit);
            if (threat != null && threat.isAlive()) {
                APosition step = EscapeScoredStep.pickScored(unit, threat, 8);
                // Escape toward safety, never away from it: a local step
                // that takes us farther from the rally than we stand is a
                // detour, not an escape (measured sim: westward steps at
                // the end of a homeward march cost a full tile of the
                // displacement the cutoff pin demands). Rejected steps
                // keep the rally - same as today, never worse.
                if (step != null && !marchesAwayFrom(step, unit, dest)) {
                    DuelMemory.noteRetreatWaypoint(unit, step);

                    return step;
                }
            }

            return dest;
        } catch (Exception ignored) {
            return dest;
        }
    }

    /**
     * True when stepping there takes the unit farther from the rally than
     * it stands (plus one tile of tolerance for lateral unpiling steps).
     * Null destinations cannot judge - the step rides.
     */
    public static boolean marchesAwayFrom(APosition step, AUnit unit, HasPosition dest) {
        try {
            if (step == null || unit == null || dest == null) return false;

            APosition destPos = dest.position();
            if (destPos == null || unit.position() == null) return false;

            return step.distTo(destPos) > unit.distTo(destPos) + 1.0;
        } catch (Exception ignored) {
            return false;
        }
    }

    /**
     * Nearest enemy combat unit to this unit (unit-level enemies, not the
     * global selection - the same source the chase check reads).
     */
    private static AUnit nearestEnemyCombatUnit(AUnit unit) {
        try {
            return unit.enemiesNear().combatUnits().nearestTo(unit);
        } catch (RuntimeException ignored) {
            return null;
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
        // Standardized no-retreat (owner 2026-11-09): with two bases
        // and a real army standing, no retreat leg runs - not the
        // cluster rally, not the organized walk home. The unit fights
        // where it stands, even on a FLEE tactic.
        if (atlantis.combat.cluster.ClusterTactic.neverRetreats()) {
            return new Verdict(FleeReason.STAY_NEVER_RETREAT, null);
        }
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
            // Retreat with an escape route (owner 2026-10-26, tested
            // loss vs Protoss): the rally above is always home ground,
            // but the road there can run straight through the enemy
            // army - marching it donates the unit on its own retreat.
            // The escape route owns the destination; the reason names
            // the reroute so the telemetry tells them apart.
            HasPosition escape = RetreatEscapeRoute.safeRally(unit, home);
            boolean rerouted = false;
            if (escape != null && escape != home) {
                try {
                    rerouted = escape.distTo(home) >= 0.5;
                } catch (RuntimeException ignored) {
                    rerouted = false;
                    escape = home;
                }
            }
            if (rerouted) home = escape;
            // Retreat floor (owner 2026-10-25): a retreat stops at the
            // middle-choke line, it never runs all the way home.
            home = RetreatFloor.clamp(home);
            HasPosition point = ClusterRally.gatherPointFor(unit, leader, home, tactic);
            // The floor gates the spread too: the rally scatter around a
            // line-clamped anchor can step behind the line again, which is
            // exactly what the floor forbids - so the final point, not just
            // the shared anchor, goes through the clamp.
            if (tactic == FightOrFlight.Tactic.FLEE) point = RetreatFloor.clamp(point);
            if (point != null && unit.distTo(point) >= 0.2) {
                // The funnel refuses unwalkable destinations (once-a-minute
                // stack trace) while the unit still counts as retreated -
                // so an unstandable point never leaves this method: the
                // unscattered anchor when it stands, a fight in place
                // otherwise.
                HasPosition valid = standableOrFallback(point, home, FormationSlots::isStandable);
                if (valid != null) {
                    return new Verdict(rerouted ? FleeReason.ESCAPE_RALLY : FleeReason.RALLY, valid);
                }
            }

            return new Verdict(FleeReason.NOWHERE_TO_RUN, null);
        }
        if (noFleeOnBaselessUms) return new Verdict(FleeReason.STAY_BASELESS, null);

        return new Verdict(FleeReason.STAY_MID_SWING, null);
    }

    /**
     * The funnel refuses unwalkable destinations, so validity is decided
     * here, never at issue time: a standable rally marches, an unstandable
     * one falls back to the unscattered anchor when it stands, and two bad
     * points fight in place instead of a refused order. The walkability
     * predicate injects (DIP) so the contract pins without a map.
     */
    public static HasPosition standableOrFallback(
        HasPosition point, HasPosition anchor, Function<APosition, Boolean> standable
    ) {
        APosition pointPos = point == null ? null : point.position();
        if (pointPos != null && Boolean.TRUE.equals(standable.apply(pointPos))) return point;

        APosition anchorPos = anchor == null ? null : anchor.position();
        if (anchorPos != null && Boolean.TRUE.equals(standable.apply(anchorPos))) return anchor;

        return null;
    }

    private static boolean isChased(AUnit unit) {
        return unit.enemiesNear().havingWeapon().canAttack(unit, CHASE_MARGIN_TILES).notEmpty();
    }

    /**
     * One decision with its destination, or a null destination to fight in place.
     */
    public static final class Verdict {
        public final FleeReason reason;
        public final HasPosition dest;

        Verdict(FleeReason reason, HasPosition dest) {
            this.reason = reason;
            this.dest = dest;
        }
    }
}
