package atlantis.battle;

import atlantis.battle.twins.ClusterTwin;

import atlantis.battle.cluster.FightOrFlight;
import atlantis.battle.cluster.LeaveBreachedBase;
import atlantis.battle.retreating.RetreatFromLostFight;
import atlantis.battle.strategy.BattleStrategies;
import atlantis.battle.strategy.BattleStrategy;
import atlantis.battle.strategy.HuntLastContact;
import atlantis.battle.targeting.AcquireTarget;
import atlantis.battle.targeting.StickyTarget;

import atlantis.battle.cluster.Missions;

import atlantis.battle.cluster.ClusterBreach;
import atlantis.battle.cluster.ClusterMission;
import atlantis.battle.cluster.ClusterTactic;

import atlantis.battle.cluster.ClusterAnchor;

import atlantis.battle.cluster.Alpha;

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

        ClusterMission.Posture posture = ClusterMission.postureFor(
            Count.ourCombatUnits(), ClusterBreach.breachForcesFight()
        );
        Missions.followPosture(posture);

        FightOrFlight.Tactic tactic = ClusterTactic.decide();
        HasPosition anchor = ClusterAnchor.anchorFor(posture);

        ClusterTwin.sample(posture, tactic, anchor);

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
        if (unit == null || !unit.isAlive()) return;

        observe(unit);

        if (retreatIfBeaten(unit, tactic, anchor, leader)) return;
        if (leaveIfBreached(unit)) return;

        BattleStrategy strategy = BattleStrategies.pick(unit, anchor);
        AUnit target = AcquireTarget.acquire(unit);
        if (target != null) {
            ArmyContact.noteFight(unit, target, tactic);
            if (FormationHold.holdIfCalm(unit, target, tactic, leader, anchor)) return;

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

    /** The engaged frame: swing discipline, kite, glide, re-arm. */
    private static void fight(AUnit unit, AUnit target, BattleStrategy strategy) {
        FightTarget.fight(unit, target, strategy);
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
        atlantis.battle.kite.CombatTuning.resetToDefaults();
        atlantis.battle.escape.EscapeTuning.resetToDefaults();
        atlantis.battle.strategy.GhostAdvance.clearForTests();
        atlantis.battle.formation.FormationSlots.clearForTests();
        atlantis.battle.formation.SharedEscape.clearForTests();
        atlantis.battle.twins.FightTrace.clearForTests();
        atlantis.battle.twins.UnitTelemetry.clearForTests();
        atlantis.battle.FrozenWatch.clearForTests();
        atlantis.battle.ArmyContact.clearForTests();
        FightTarget.clearForTests();
        StickyTarget.clearForTests();
    }
}
