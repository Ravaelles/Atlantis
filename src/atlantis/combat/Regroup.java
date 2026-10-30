package atlantis.combat;

import atlantis.battle.cluster.Missions;
import atlantis.combat.cluster.ClusterRally;
import atlantis.combat.cluster.FightOrFlight;
import atlantis.combat.cluster.HomeGround;
import atlantis.combat.strategy.BattleStrategy;
import atlantis.combat.strategy.GhostAdvance;
import atlantis.combat.targeting.Engagement;
import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

/**
 * The march without a target: nothing to fight, so the unit falls back
 * through its options in order - the approach's regroup point, the
 * formation advance, the ghost creep, the freshest fight, the gather.
 *
 * <p>The approach owns its regroup point (collapse joins the bait at
 * the funnel mouth, bait pulls, the line holds the standoff), then the
 * ghost advance creeps after a remembered contact instead of standing
 * (the hunt owns the far baseless case; inside it the gather rule used
 * to march the unit back home every frame), answered on the enum so no
 * call site switches on it. Without a strategy point or a ghost the
 * formation fallback owns the unit: take a formation slot when the
 * cluster forms up (3+ combat units, known pivot), else bunch on the
 * leader so the cluster advances (or retreats) as one instead of a
 * line (owner 2026-10-19: march and retreat both strung out). The
 * leader sets the pace; stragglers catch up instead of everyone racing
 * the anchor.</p>
 */
public final class Regroup {

    private Regroup() {
    }

    /**
     * March this unit while it has no target, through the fallbacks in
     * order. When nothing owns it the unit simply stands.
     */
    public static void march(
        AUnit unit, FightOrFlight.Tactic tactic, HasPosition anchor, AUnit leader, BattleStrategy strategy
    ) {
        APosition regroup = strategy.regroupPointFor(unit);
        if (regroup != null && unit.move(regroup, Actions.MOVE_ENGAGE, strategy.regroupTag().tag())) {
            FightTarget.markRepositioned(unit);

            return;
        }
        // Join an ongoing fight before taking a slot (owner: nobody waits
        // in formation while the cluster fights - melee joins, ranged
        // advances to range). Only while contact is fresh (ongoing, not
        // old news - old fights must not drag the line out of formation),
        // only on FIGHT, only on UMS (melee keeps its anchor/formation
        // discipline: a scouting skirmish must not drag the army across
        // the map). Ghosts and static slots wait below.
        if (tactic == FightOrFlight.Tactic.FIGHT && A.isUms() && ArmyContact.isOngoingFight()) {
            APosition reinforce = ArmyContact.reinforcePointFor(unit);
            if (reinforce != null && unit.move(reinforce, Actions.MOVE_ENGAGE, Behavior.REINFORCE.tag())) {
                FightTarget.markRepositioned(unit);

                return;
            }
        }
        // No-contact hold (owner 2026-10-26, live game vs Protoss): with
        // no enemy to engage and the line defending home, hold halfway
        // between the main base and the main choke instead of marching to
        // a far anchor or creeping after a ghost - the far march walked a
        // small line across the map and uncovered the base while raiders
        // ran by. Only on FIGHT (FLEE owns its own rally), only for a
        // home anchor (an attack anchor must still march out), never
        // into an ongoing fight (join it instead). Strategy points, the
        // reinforce and the formation answer first - this owns only the
        // blind wait no slot wants.
        if (tactic == FightOrFlight.Tactic.FIGHT && !ArmyContact.isOngoingFight()) {
            APosition hold = ClusterRally.noContactHoldPoint(anchor);
            if (hold != null) {
                HasPosition slot = ClusterRally.formationSlotFor(unit, leader, anchor, tactic);
                if (slot == null) {
                    // NEXT #210: a throttled hold stands instead of falling
                    // through to the advance below - falling through would
                    // re-issue a different formation order on the same frame
                    // the latch just refused, which is the churn with a new name.
                    if (!atlantis.combat.formation.FormationOrderLatch.shouldIssue(unit, hold)) return;

                    if (unit.move(hold, Actions.MOVE_ENGAGE, Behavior.STANDOFF.tag())) {
                        FightTarget.markRepositioned(unit);

                        return;
                    }
                }
            }
        }
        // Undressed far-leg of the attack march (one site with the dressed
        // collapse below): a line that cannot dress still marches instead
        // of chasing slots. All gates read, none owned - see the method.
        if (shouldMarchUndressedToAnchor(unit, anchor, tactic, strategy)) {
            APosition collapse = walkableCollapsePoint(unit, anchor);
            if (collapse != null
                && unit.move(collapse, Actions.MOVE_ENGAGE, Behavior.COLLAPSE.tag())) {
                FightTarget.markRepositioned(unit);

                return;
            }
        }
        // March in formation (owner 2026-10-26): the line takes slots
        // facing the expected enemy before anyone creeps - the vanguard
        // still detaches fast, everyone else advances as a line, never
        // as a blob chasing the scout. No slot (duels, truly blind on
        // FLEE) falls through to the creep below. Melee on top of the
        // unit skips the slots too (hard veto, same as the calm hold):
        // formation never stands into chews, with or without a target.
        if (!GhostAdvance.isVanguard(unit) && !FormationHold.hasCloseMeleeEnemy(unit)) {
            HasPosition slot = ClusterRally.formationSlotFor(unit, leader, anchor, tactic);
            // Raid collapse (measured in the MotionlessBaseRaid sim: 12 Dragoons
            // marched to 13 tiles of a passive base and stood in standoff
            // forever - nothing pulls the line into an unseen base: no target,
            // no ghost, no contact). Dressed in slot far beyond sight of an
            // attack anchor, march the anchor itself instead of holding the
            // standoff. FIGHT only; home anchors sit within sight so defense
            // postures never take this branch. Ahead of the latch below: a
            // throttled re-issue would stand (`else return`) and trap the
            // collapse forever.
            if (slot != null && shouldCollapseToAnchor(unit, slot, anchor, tactic)) {
                APosition collapse = walkableCollapsePoint(unit, anchor);
                if (collapse != null
                    && unit.move(collapse, Actions.MOVE_ENGAGE, Behavior.COLLAPSE.tag())) {
                    FightTarget.markRepositioned(unit);

                    return;
                }
            }
            if (slot != null) {
                // NEXT #210: same latch as the calm hold - a throttled
                // advance stands instead of falling through to creep/gather
                // below, which would scatter the line the latch is calming.
                // FLEE never throttles: retreats own their movement.
                if (tactic != FightOrFlight.Tactic.FIGHT
                    || atlantis.combat.formation.FormationOrderLatch.shouldIssue(unit, slot)) {
                    if (unit.move(slot, Actions.MOVE_ENGAGE, Behavior.ADVANCE_FORMATION.tag())) {
                        FightTarget.markRepositioned(unit);

                        return;
                    }
                }
                else return;
            }
        }
        if (GhostAdvance.move(unit, tactic)) {
            return;
        }
        HasPosition point = ClusterRally.formationSlotFor(unit, leader, anchor, tactic);
        if (point == null) point = ClusterRally.gatherPointFor(unit, leader, anchor, tactic);
        if (point != null && unit.move(point, Actions.MOVE_ENGAGE, Behavior.GATHER.tag())) {
            FightTarget.markRepositioned(unit);
        }
    }

    /**
     * True when the unit is dressed (in its slot) but the anchor is too far
     * to see from it: holding the slot means holding standoff against
     * nothing, forever. FIGHT only - retreats own their movement, and a
     * home anchor is always within sight of a dressed line.
     */
    private static boolean shouldCollapseToAnchor(
        AUnit unit, HasPosition point, HasPosition anchor, FightOrFlight.Tactic tactic
    ) {
        return tactic == FightOrFlight.Tactic.FIGHT
            && anchor != null
            && unit.distTo(point) < 1
            && unit.distTo(anchor) > COLLAPSE_BEYOND_SIGHT_TILES;
    }

    /**
     * True when an undressed line still marches the attack anchor: FIGHT
     * under the open-field approach, the anchor beyond sight, no ongoing
     * fight to reinforce, no ghost in range to creep at, and the anchor
     * not home (the no-contact hold above owns home ground). Every
     * threshold is reused (sight slack, ghost envelope), never a new
     * literal - this widens the collapse site above, it never opens one.
     */
    private static boolean shouldMarchUndressedToAnchor(
        AUnit unit, HasPosition anchor, FightOrFlight.Tactic tactic, BattleStrategy strategy
    ) {
        if (tactic != FightOrFlight.Tactic.FIGHT) return false;
        if (strategy != BattleStrategy.OPEN_FIELD) return false;
        // The attack marches; this march is its far leg. On DEFEND the
        // line holds (veto guards home, slots own the wait) - marching a
        // defend anchor out abandons the base exactly like the vetoed
        // chase does (same mission read as ChaseVeto, opposite side).
        if (!Missions.isGlobalMissionAttack()) return false;
        if (unit == null || anchor == null) return false;
        try {
            if (unit.distTo(anchor) <= COLLAPSE_BEYOND_SIGHT_TILES) return false;
        } catch (Exception ignored) {
            return false;
        }
        if (ArmyContact.isOngoingFight()) return false;
        // Someone else owns the near-contact: the fight branch above, the
        // creep below, or the slots holding the line (phalanx kites and
        // trapped breakouts die when the march walks stragglers into the
        // pack). Same warm-contact envelope the creep uses - inside it
        // the march never owns the unit; outside it nobody else does.
        try {
            AUnit near = Engagement.nearestEngageableEnemy(unit, GhostAdvance.LOST_TILES);
            if (near != null) return false;
        } catch (Exception ignored) {
            return false;
        }
        // A fresh personal contact still owns the line: the creep walks
        // it (unreached) or holds it (reached). Marching a warm fight out
        // abandons the kill/line for the anchor - the march owns only
        // stale ground. Same staleness the creep uses, read not owned.
        try {
            APosition last = FightTarget.lastContact(unit);
            Integer frame = FightTarget.lastContactFrame(unit);
            if (last != null && frame != null
                && A.now() - frame <= GhostAdvance.STALE_CONTACT_FRAMES) return false;
        } catch (Exception ignored) {
            return false;
        }
        try {
            APosition ghost = GhostAdvance.effectiveGhostPointFor(unit);
            if (ghost != null && unit.distTo(ghost) <= GhostAdvance.LOST_TILES) return false;
        } catch (Exception ignored) {
            return false;
        }
        try {
            HomeGround home = HomeGround.resolve();
            AUnit main = home != null ? home.main() : null;
            if (ClusterRally.isHomeAnchor(anchor, main)) return false;
        } catch (Exception ignored) {
        }
        return true;
    }

    /**
     * Beyond any ground sight plus standoff slack: the sim raid stuck at 13
     * tiles, Dragoon sight is 8. Below this the normal slot/ghost logic owns
     * the unit.
     */
    private static final double COLLAPSE_BEYOND_SIGHT_TILES = 10;

    /**
     * Where the collapse marches: toward the anchor but stopping short of
     * its tile, on walkable ground. Standing ON a building is not a legal
     * order (the sim refuses it outright, the engine only slides), so the
     * anchor tile itself is never the destination - staging 5 short puts
     * the line inside sight on arrival and acquire takes over from there.
     */
    private static APosition walkableCollapsePoint(AUnit unit, HasPosition anchor) {
        APosition target = anchor.position();
        if (target == null) return null;

        double dist = unit.distTo(target);
        if (dist <= COLLAPSE_STAGING_SHORT + 1) return target;

        double ratio = (dist - COLLAPSE_STAGING_SHORT) / dist;
        int tx = (int) (unit.tx() + (target.tx() - unit.tx()) * ratio);
        int ty = (int) (unit.ty() + (target.ty() - unit.ty()) * ratio);

        APosition staged = APosition.create(tx, ty);
        if (staged.isWalkable()) return staged;

        for (int r = 1; r <= 3; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dy = -r; dy <= r; dy++) {
                    if (Math.max(Math.abs(dx), Math.abs(dy)) != r) continue;
                    APosition candidate = APosition.create(tx + dx, ty + dy);
                    if (candidate.isWalkable()) return candidate;
                }
            }
        }

        return null;
    }

    private static final double COLLAPSE_STAGING_SHORT = 5;
}
