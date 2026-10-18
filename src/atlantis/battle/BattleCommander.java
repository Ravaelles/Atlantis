package atlantis.battle;

import atlantis.architecture.Commander;
import atlantis.architecture.CommanderFactory;
import atlantis.game.A;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

/**
 * Owns every army unit each frame. Phase 0 of __PLAN-COMBAT step 5: one
 * cluster (Alpha), one posture (main base or enemy base), one gate
 * (evaluator fight/flee with a retreat latch). No per-unit decisions.
 */
public class BattleCommander extends Commander {

    private static int fleeStartedAt = -1;

    /** Per-game reset next to the other static resets in OnGameStarted. */
    public static void reset() {
        fleeStartedAt = -1;
    }

    @Override
    protected CommanderFactory[] subcommanders() {
        return new CommanderFactory[]{};
    }

    @Override
    protected boolean handle() {
        gatherCluster();
        if (Alpha.get().isEmpty()) return false;

        ClusterMission.Posture posture = ClusterMission.decide(Count.ourCombatUnits());
        if (posture == ClusterMission.Posture.ATTACK) {
            Missions.forceGlobalMissionAttack("cluster-attack");
        }
        else {
            Missions.forceGlobalMissionDefend("cluster-defend");
        }

        FightOrFlight.Tactic tactic = tactic();
        HasPosition anchor = posture == ClusterMission.Posture.ATTACK
            ? ClusterAnchor.attackAnchor() : ClusterAnchor.defendAnchor();

        // Throttled state line: the only per-game proof of what the
        // cluster does (posture, tactic, size, anchor). JSON-only, stderr.
        if (A.everyNthGameFrame(31)) {
            atlantis.util.log.EventLog.key(atlantis.util.log.LogKey.CLUSTER)
                .param("cluster", Alpha.get().size())
                .param("posture", posture.name())
                .param("tactic", tactic.name())
                .param("anchor", anchor == null ? "-" : anchor.toString())
                .log(A.now(), A.minSec());
        }

        for (AUnit unit : Alpha.get().selection().list()) {
            order(unit, tactic, anchor, Alpha.get().leader());
        }

        return false;
    }

    private FightOrFlight.Tactic tactic() {
        AUnit leader = Alpha.get().leader();
        AUnit assessor = vanguardAssessor(leader);
        double eval = assessor != null ? assessor.eval() : 0;

        FightOrFlight.Tactic tactic = FightOrFlight.decide(eval, A.now(), fleeStartedAt);
        if (tactic == FightOrFlight.Tactic.FLEE && fleeStartedAt < 0) {
            fleeStartedAt = A.now();
        }
        if (tactic == FightOrFlight.Tactic.FIGHT) {
            fleeStartedAt = -1;
        }
        return tactic;
    }

    /**
     * Who the gate listens to: the unit closest to an enemy within 15
     * tiles, else the leader. A leader at the back reads a different fight
     * than the vanguard taking contact (owner 2026-10-19: one cluster, one
     * area assessment - the gate answers for where the fight is, not for
     * where the leader stands). Public as a seam, pinned without a game.
     */
    public static AUnit vanguardAssessor(AUnit leader) {
        AUnit best = leader;
        double bestDist = Double.MAX_VALUE;
        for (AUnit unit : Alpha.get().selection().list()) {
            if (unit == null || !unit.isAlive()) continue;
            AUnit foe = atlantis.battle.targeting.ASquadTargeting.nearestEngageableEnemy(unit, 15);
            if (foe == null) continue;
            double dist = unit.distTo(foe);
            if (dist < bestDist) {
                best = unit;
                bestDist = dist;
            }
        }
        return best;
    }

    private void order(AUnit unit, FightOrFlight.Tactic tactic, HasPosition anchor, AUnit leader) {
        if (unit == null || !unit.isAlive()) return;

        if (tactic == FightOrFlight.Tactic.FLEE) {
            HasPosition home = ClusterAnchor.defendAnchor();
            HasPosition point = gatherPointFor(unit, leader, home);
            if (point != null) unit.move(point, Actions.RUN_ENEMY, "ClusterFlee");
            return;
        }

        AUnit target = atlantis.battle.targeting.ASquadTargeting.useSquadTargetIfPossible(unit);
        if (target == null) {
            target = atlantis.battle.targeting.ASquadTargeting.nearestEngageableEnemy(unit, 9);
        }
        if (target != null) {
            // A cooling-down Dragoon with ANY enemy on top of it backs off
            // instead of trading at point blank (owner 2026-10-19, Wine
            // 1Drag_v_1Zeal: the Dragoon stood still under a Zealot with
            // DoNothing and died - no kite existed at all). The threat is
            // the nearest enemy inside the envelope, not necessarily the
            // focused target: focus fire must not pin a Dragoon that is
            // being shredded. Shooting resumes once the weapon is ready,
            // which closes the range again.
            AUnit threat = atlantis.battle.targeting.ASquadTargeting
                .nearestEngageableEnemy(unit, KITE_ENEMY_TILES);
            if (threat != null && shouldKite(unit, threat)) {
                unit.moveAwayFrom(threat, KITE_STEP_TILES, Actions.MOVE_AVOID, "ClusterKite");
                return;
            }
            unit.attackUnit(target);
            if (unit.squad() != null) unit.squad().targeting().setLastTarget(target);
            return;
        }

        // No enemy in reach: bunch on the leader so the cluster advances
        // (or retreats) as one instead of a line (owner 2026-10-19: march
        // and retreat both strung out). The leader sets the pace; stragglers
        // catch up instead of everyone racing the anchor.
        HasPosition point = gatherPointFor(unit, leader, anchor);
        if (point != null) unit.move(point, Actions.MOVE_ENGAGE, "ClusterMove");
    }

    /**
     * Where this unit moves when there is no enemy to engage: the leader
     * when straggling behind it, else the mission anchor. Public as a seam:
     * the bunching rule pins in a stub state without running the frame.
     */
    public static HasPosition gatherPointFor(AUnit unit, AUnit leader, HasPosition anchor) {
        if (leader != null && unit != leader && unit.distTo(leader) > CLUSTER_GATHER_RADIUS_TILES) {
            return leader;
        }
        return anchor;
    }

    /** Tiles behind the leader before a unit turns back to rejoin. */
    private static final double CLUSTER_GATHER_RADIUS_TILES = 2;

    /**
     * A Dragoon with an enemy inside {@link #KITE_ENEMY_TILES} tiles backs
     * off while its weapon cools down instead of trading at point blank
     * (owner 2026-10-19: enemy closer than 3.5 tiles + cooldown at least
     * 10 frames). Dragoon-only: Zealots want contact, and other races
     * are out of scope. Public as a seam: the rule pins in a stub state.
     */
    public static boolean shouldKite(AUnit unit, AUnit target) {
        if (unit == null || target == null || !unit.isDragoon()) return false;
        if (unit.distTo(target) >= KITE_ENEMY_TILES) return false;
        return unit.cooldown() >= KITE_COOLDOWN_FRAMES;
    }

    /** An enemy closer than this makes a cooling Dragoon step back. */
    private static final double KITE_ENEMY_TILES = 3.5;

    /** Weapon cooldown from which backing off beats standing fire. */
    private static final int KITE_COOLDOWN_FRAMES = 10;

    /** How far the kiting step carries (tiles). */
    private static final double KITE_STEP_TILES = 2.0;

    private void gatherCluster() {
        Selection army = Select.ourCombatUnits();
        if (army == null) return;
        for (AUnit unit : army.list()) {
            if (unit != null && unit.isAlive() && unit.squad() == null) {
                Alpha.get().addUnit(unit);
            }
        }
    }
}
