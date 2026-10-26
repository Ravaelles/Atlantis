package atlantis.combat;

import atlantis.combat.cluster.ClusterRally;
import atlantis.combat.cluster.FightOrFlight;
import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.combat.strategy.BattleStrategy;
import atlantis.combat.strategy.GhostAdvance;
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
        // March in formation (owner 2026-10-26): the line takes slots
        // facing the expected enemy before anyone creeps - the vanguard
        // still detaches fast, everyone else advances as a line, never
        // as a blob chasing the scout. No slot (duels, truly blind on
        // FLEE) falls through to the creep below. Melee on top of the
        // unit skips the slots too (hard veto, same as the calm hold):
        // formation never stands into chews, with or without a target.
        if (!GhostAdvance.isVanguard(unit) && !FormationHold.hasCloseMeleeEnemy(unit)) {
            HasPosition slot = ClusterRally.formationSlotFor(unit, leader, anchor, tactic);
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
}
