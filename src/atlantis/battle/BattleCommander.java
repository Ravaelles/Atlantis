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
        act(unit, tactic, anchor, leader);
    }

    /**
     * One unit's act for the frame, extracted so the composition pins
     * without a game: FLEE with nowhere to go falls through to the
     * fight logic (no DoNothing deaths on baseless maps), a cooling
     * Dragoon with a close threat steps back, a mid-animation unit is
     * never interrupted, otherwise it attacks or regroups.
     */
    public static void act(AUnit unit, FightOrFlight.Tactic tactic, HasPosition anchor, AUnit leader) {
        if (tactic == FightOrFlight.Tactic.FLEE) {
            HasPosition home = ClusterAnchor.defendAnchor();
            HasPosition point = gatherPointFor(unit, leader, home);
            if (point != null) {
                unit.move(point, Actions.RUN_ENEMY, "ClusterFlee");
                return;
            }
            // Nowhere to run to (baseless map, null anchor): standing
            // still with DoNothing until death is never the answer -
            // fall through and fight in place instead (kite included).
        }

        AUnit target = atlantis.battle.targeting.ASquadTargeting.useSquadTargetIfPossible(unit);
        if (target == null) {
            target = atlantis.battle.targeting.ASquadTargeting.nearestEngageableEnemy(unit, 9);
        }
        if (target != null) {
            // Never interrupt our own shot: a move order during the attack
            // animation cancels the damage (PurpleWave AttackDelay) - the
            // shot that just started must be allowed to land, so an
            // animating unit gets no new order at all this frame.
            if (unit.isAttackFrame() || unit.isStartingAttack()) return;

            // A Dragoon steps back only when the enemy is inside its
            // safety margin - otherwise it stands and lets the standing
            // attack order fire the instant the cooldown hits zero
            // (PurpleWave BeDragoonVsSlowlot: margin over threats inside
            // 5 tiles, never a flat distance + cooldown gate; the threat
            // is the nearest enemy inside the envelope, not necessarily
            // the focused target: focus fire must not pin a Dragoon that
            // is being shredded).
            AUnit threat = atlantis.battle.targeting.ASquadTargeting
                .nearestEngageableEnemy(unit, KITE_ENEMY_TILES);
            if (threat != null && shouldKite(unit, threat)) {
                // A failed step must never mean standing idle: a cornered
                // Dragoon fights instead of waiting to die (Wine
                // 1Drag_v_1Zeal follow-up: kite-move-kite, then a stop,
                // then trading at point blank).
                if (unit.moveAwayFrom(threat, KITE_STEP_TILES, Actions.MOVE_AVOID, "ClusterKite")) {
                    KiteTwin.steppedAside(unit, threat);
                    return;
                }
                KiteTwin.nowhereToStep(unit, threat);
            }
            // Order discipline, not order spam: a new attack order issues
            // only when the engine holds a different target (first
            // acquisition, lost order while moving, target switch).
            // Otherwise nothing issues this frame - the standing order
            // fires on its own the moment the weapon is ready (PurpleWave
            // Commander.attack: order on new/overdue target, else sleep).
            if (target.equals(unit.target())) return;
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
     * A Dragoon steps back only while the closest threat sits inside its
     * safety margin (PurpleWave BeDragoonVsSlowlot, engine numbers from
     * the vendored jar: Dragoon range 4 tiles, cooldown 30, Zealot
     * range ~0 tiles, 16 damage per pass). Margin is the tiles the
     * threat still must close before it can hit us, minus half a tile
     * when the threat is coming for exactly this unit. A healthy
     * Dragoon holds to about one tile and keeps shooting through the
     * cooldown; a dying one demands up to four. Dragoon-only: Zealots
     * want contact, other races are out of scope. Public as a seam:
     * the rule pins in a stub state.
     */
    public static boolean shouldKite(AUnit unit, AUnit threat) {
        if (unit == null || threat == null || !unit.isDragoon()) return false;
        if (unit.distTo(threat) >= KITE_ENEMY_TILES) return false;
        return kiteMargin(unit, threat) < kiteMarginRequired(unit);
    }

    /**
     * Tiles the threat still must close before it can hit us, minus
     * half a tile when its order target is exactly this unit (the
     * cheap half of PurpleWave's commitment read: targeted plus
     * facing-dot; facing has no cheap API here, so it stays open).
     * Distances are center-based tiles, not edge pixels - about half
     * a tile more permissive than the original, Wine decides.
     */
    private static double kiteMargin(AUnit unit, AUnit threat) {
        double margin = unit.distTo(threat) - unit.enemyWeaponRangeAgainstThisUnit(threat);
        AUnit hunted = threat.orderTarget();
        if (hunted != null && hunted.id() == unit.id()) margin -= 0.5;
        return margin;
    }

    /**
     * Tiles of margin a Dragoon demands before it yields ground:
     * about one while healthy, up to four while dying (PurpleWave:
     * max(32, 128 - 8 * hitsLeft) pixels, Zealot pass = 16 damage).
     */
    private static double kiteMarginRequired(AUnit unit) {
        double hitsLeft = (unit.hp() + unit.shields()) / 16.0;
        return Math.max(1.0, 4.0 - 0.25 * hitsLeft);
    }

    /** Enemies inside this range count as kiting threats (PurpleWave threatsInPixels(160)). */
    private static final double KITE_ENEMY_TILES = 5.0;

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
