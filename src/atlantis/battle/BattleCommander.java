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
            recordClusterState(posture, tactic, anchor);
        }

        for (AUnit unit : Alpha.get().selection().list()) {
            if (unit == null || !unit.isAlive()) continue;
            act(unit, tactic, anchor, Alpha.get().leader());
        }

        return false;
    }

    /**
     * One unit's act for the frame, in reading order: observe, retreat
     * if beaten, leave a breached base, acquire a target, then either
     * hold formation and march, fight, hunt the last contact, or
     * regroup. Each branch below is one named domain step; the how
     * lives in that step, never here.
     */
    public static void act(AUnit unit, FightOrFlight.Tactic tactic, HasPosition anchor, AUnit leader) {
        observe(unit);
        if (retreatIfBeaten(unit, tactic, anchor, leader)) return;
        if (leaveIfBreached(unit)) return;

        BattleStrategy strategy = BattleStrategies.pick(unit, anchor);
        AUnit target = AcquireTarget.acquire(unit);
        if (target != null) {
            noteArmyContact(unit, target, tactic);
            if (holdFormationIfCalm(unit, target, tactic, anchor, leader)) return;
            fight(unit, target, strategy);
            return;
        }
        if (huntLastContact(unit)) return;
        regroup(unit, tactic, anchor, leader, strategy);
    }

    /** Twin + frozen-watch sampling, once per unit per frame. */
    private static void observe(AUnit unit) {
        atlantis.battle.twins.UnitTelemetry.sample(unit);
        atlantis.battle.FrozenWatch.note(unit);
    }

    /** Retreat owns routing units before anything else looks at them. */
    private static boolean retreatIfBeaten(
        AUnit unit, FightOrFlight.Tactic tactic, HasPosition anchor, AUnit leader
    ) {
        return RetreatFromLostFight.retreat(unit, tactic, anchor, leader);
    }

    /** A breached base is left, never defended into a wipe. */
    private static boolean leaveIfBreached(AUnit unit) {
        return LeaveBreachedBase.leave(unit);
    }

    /**
     * Army-level contact for the split-cluster join (NEXT #181):
     * recorded at acquisition, not in fight(), because kiting
     * units never reach fight() yet still engage the enemy.
     * FIGHT only: a routing unit must not pull the army into it.
     */
    private static void noteArmyContact(AUnit unit, AUnit target, FightOrFlight.Tactic tactic) {
        if (tactic == FightOrFlight.Tactic.FIGHT) {
            atlantis.battle.ArmyContact.noteFight(unit, target);
        }
    }

    /**
     * Formation hold (owner 2026-10-26, Stardust doctrine): while
     * the enemy is melee-only and nobody is in contact, the line
     * marches in formation instead of dissolving into a charge.
     * No slot means no formation (duels fall through untouched).
     */
    private static boolean holdFormationIfCalm(
        AUnit unit, AUnit target, FightOrFlight.Tactic tactic, HasPosition anchor, AUnit leader
    ) {
        if (!FormationHold.shouldHold(unit, target, tactic)) return false;
        HasPosition slot = ClusterRally.formationSlotFor(unit, leader, anchor, tactic);
        if (slot == null || !unit.move(slot, Actions.MOVE_ENGAGE, Behavior.HOLD_FORMATION.tag())) {
            return false;
        }
        FightTarget.markRepositioned(unit);
        return true;
    }

    /** The engaged frame: swing discipline, kite, glide, re-arm. */
    private static void fight(AUnit unit, AUnit target, BattleStrategy strategy) {
        FightTarget.fight(unit, target, strategy);
    }

    /** Lost contact on a baseless map marches the remembered point. */
    private static boolean huntLastContact(AUnit unit) {
        return HuntLastContact.hunt(unit);
    }

    /** Throttled CLUSTER twin: posture, tactic, size, anchor. JSON-only, stderr. */
    private static void recordClusterState(
        ClusterMission.Posture posture, FightOrFlight.Tactic tactic, HasPosition anchor
    ) {
        // Cluster center readout (owner 2026-10-26): the file carries
        // the army's mean position and the nearest enemy's distance to
        // it - the forward-movement measure per-unit traces cannot
        // give. Cadence stays the handle's own every-31-frames.
        HasPosition center = Alpha.get().center();
        int foeDist = -1;
        String ghostSource = "-";
        int ghostDist = -1;
        int ghostX = -1;
        int ghostY = -1;
        try {
            if (center != null) {
                AUnit nearest = atlantis.units.select.Select.enemy()
                    .combatUnits().nearestTo(center);
                if (nearest != null) foeDist = (int) (center.distTo(nearest) * 10);
            }
            AUnit leader = Alpha.get().leader();
            if (leader != null && leader.isAlive()) {
                ghostSource = atlantis.battle.strategy.GhostAdvance.ghostSourceFor(leader);
                atlantis.map.position.APosition ghost =
                    atlantis.battle.strategy.GhostAdvance.ghostPointFor(leader);
                if (ghost != null) {
                    ghostDist = (int) (leader.distTo(ghost) * 10);
                    ghostX = ghost.tx();
                    ghostY = ghost.ty();
                }
            }
        } catch (Exception ignored) {
        }
        EventLog.key(LogKey.CLUSTER)
            .param("cluster", Alpha.get().size())
            .param("posture", posture.name())
            .param("tactic", tactic.name())
            .param("breached", ClusterBreach.breachForcesFight())
            .param("anchor", anchor == null ? "-" : anchor.toString())
            .param("cx", center == null ? -1 : center.tx())
            .param("cy", center == null ? -1 : center.ty())
            .param("foe", foeDist)
            .param("ghost", ghostSource)
            .param("gdist", ghostDist)
            .param("gx", ghostX)
            .param("gy", ghostY)
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
        if (regroup != null && unit.move(regroup, Actions.MOVE_ENGAGE, strategy.regroupTag().tag())) {
            FightTarget.markRepositioned(unit);
            return;
        }
        // March in formation (owner 2026-10-26): the line takes slots
        // facing the expected enemy before anyone creeps - the vanguard
        // still detaches fast, everyone else advances as a line, never
        // as a blob chasing the scout. No slot (duels, truly blind on
        // FLEE) falls through to the creep below.
        if (!GhostAdvance.isVanguard(unit)) {
            HasPosition slot = ClusterRally.formationSlotFor(unit, leader, anchor, tactic);
            if (slot != null && unit.move(slot, Actions.MOVE_ENGAGE, Behavior.ADVANCE_FORMATION.tag())) {
                FightTarget.markRepositioned(unit);
                return;
            }
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
            if (reinforce != null && unit.move(reinforce, Actions.MOVE_ENGAGE, Behavior.REINFORCE.tag())) {
                FightTarget.markRepositioned(unit);
                return;
            }
        }
        HasPosition point = ClusterRally.formationSlotFor(unit, leader, anchor, tactic);
        if (point == null) point = ClusterRally.gatherPointFor(unit, leader, anchor);
        if (point != null && unit.move(point, Actions.MOVE_ENGAGE, Behavior.GATHER.tag())) {
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
        atlantis.battle.FrozenWatch.clearForTests();
        atlantis.battle.ArmyContact.clearForTests();
        FightTarget.clearForTests();
        StickyTarget.clearForTests();
    }
}
