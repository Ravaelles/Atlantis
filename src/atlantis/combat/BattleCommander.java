package atlantis.combat;
import atlantis.battle.DuelMemory;

import atlantis.combat.twins.ClusterTwin;

import atlantis.combat.cluster.FightOrFlight;
import atlantis.combat.cluster.LeaveBreachedBase;
import atlantis.combat.retreating.RetreatFromLostFight;
import atlantis.combat.strategy.BattleStrategies;
import atlantis.combat.strategy.BattleStrategy;
import atlantis.combat.strategy.HuntLastContact;
import atlantis.combat.targeting.AcquireTarget;
import atlantis.combat.targeting.StickyTarget;

import atlantis.battle.cluster.Missions;

import atlantis.combat.cluster.ClusterBreach;
import atlantis.combat.cluster.ClusterMission;
import atlantis.combat.cluster.ClusterTactic;

import atlantis.combat.cluster.ClusterAnchor;

import atlantis.battle.cluster.Alpha;
import atlantis.game.A;

import atlantis.architecture.Commander;
import atlantis.architecture.CommanderFactory;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.select.Count;

/**
 * Owns every army unit each frame. Phase 0 of __PLAN-COMBAT step 5: one
 * cluster (Alpha), one posture (main base or enemy base), one gate
 * (evaluator fight/flee with a retreat latch). No per-unit decisions.
 *
 * <p>This class is a table of contents, nothing more: every line names
 * a domain move, every how lives one call away in the named class. A
 * non-programmer reading handle() and act() can retell the battle
 * story without opening any implementation.</p>
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

        atlantis.combat.focus.PostureReasons.Verdict mission =
            ClusterMission.postureWithReasonFor(
                Count.ourCombatUnits(), ClusterBreach.breachForcesFight()
            );
        ClusterMission.Posture posture = mission.posture;
        Missions.followPosture(posture);

        FightOrFlight.Tactic tactic = ClusterTactic.decide();
        HasPosition anchor = ClusterAnchor.anchorFor(posture);

        ClusterTwin.sample(posture, tactic, anchor);

        atlantis.cherryvis.simple.ArmyBoard.publish(A.now(), posture, tactic, mission.text, anchor);

        for (AUnit unit : Alpha.get().selection().list()) {
            act(unit, tactic, anchor, Alpha.get().leader());
        }

        return false;
    }

    /**
     * One unit's act for the frame, in reading order: observe, retreat
     * if beaten, leave a breached base, acquire a target, then either
     * note the fight and hold formation or fight, hunt the last
     * contact, or regroup. Each branch below is one named domain step;
     * the how lives in that step, never here.
     */
    public static void act(AUnit unit, FightOrFlight.Tactic tactic, HasPosition anchor, AUnit leader) {
        if (unit == null || !unit.isAlive() || !unit.isOur()) return;

        observe(unit);

        if (retreatIfBeaten(unit, tactic, anchor, leader)) return;
        if (leaveIfBreached(unit)) return;

        // Specialist behaviors own their frame before the generic
        // engagement: a brewing Reaver, a hunting Corsair, a running Dark
        // Templar, a storming High Templar. Anything else falls through
        // untouched to the shared pipeline below.
        if (atlantis.combat.behaviors.SpecialistBehaviors.act(unit)) return;

        BattleStrategy strategy = BattleStrategies.pick(unit, anchor);
        AUnit target = AcquireTarget.acquire(unit);
        if (target != null) {
            ArmyContact.noteFight(unit, target, tactic);
            if (FormationHold.holdIfCalm(unit, target, tactic, leader, anchor)) return;

            fight(unit, target, strategy, anchor, leader);

            return;
        }

        if (huntLastContact(unit)) return;

        regroup(unit, tactic, anchor, leader, strategy);
    }

    /** Twin + frozen-watch + idle-watch sampling, once per unit per frame. */
    private static void observe(AUnit unit) {
        atlantis.combat.twins.UnitTelemetry.sample(unit);
        FrozenWatch.note(unit);
        IdleWatch.note(unit);
        DuelMemory.noteBattleFrame(unit);
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

    /** The engaged frame: swing discipline, kite, glide, re-arm. */
    private static void fight(AUnit unit, AUnit target, BattleStrategy strategy, HasPosition anchor, AUnit leader) {
        FightTarget.fight(unit, target, strategy, anchor, leader);
    }

    /** Lost contact on a baseless map marches the remembered point. */
    private static boolean huntLastContact(AUnit unit) {
        return HuntLastContact.hunt(unit);
    }

    /** Nothing to fight: march the fallbacks in order. */
    private static void regroup(
        AUnit unit, FightOrFlight.Tactic tactic, HasPosition anchor, AUnit leader, BattleStrategy strategy
    ) {
        Regroup.march(unit, tactic, anchor, leader, strategy);
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
        atlantis.combat.focus.PostureLatch.resetForTests();
        atlantis.battle.kite.CombatTuning.resetToDefaults();
        atlantis.combat.escape.EscapeTuning.resetToDefaults();
        atlantis.combat.strategy.GhostAdvance.clearForTests();
        atlantis.combat.formation.FormationSlots.clearForTests();
        atlantis.combat.formation.ChokePlug.clearForTests();
        atlantis.combat.formation.SharedEscape.clearForTests();
        atlantis.combat.twins.FightTrace.clearForTests();
        atlantis.combat.twins.HoldTwin.clearForTests();
        atlantis.combat.twins.UnitTelemetry.clearForTests();
        FrozenWatch.clearForTests();
        IdleWatch.clearForTests();
        atlantis.combat.twins.ClusterTwin.clearForTests();
        ArmyContact.clearForTests();
        atlantis.combat.retreating.RetreatLadder.reset();
        atlantis.combat.cluster.FirstDragoonGate.clearForTests();
        atlantis.combat.focus.ProtossDefendFocusPoint.clearForTests();
        FightTarget.clearForTests();
        StickyTarget.clearForTests();
    }
}
