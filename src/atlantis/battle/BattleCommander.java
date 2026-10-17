package atlantis.battle;

import atlantis.architecture.Commander;
import atlantis.architecture.CommanderFactory;
import atlantis.game.A;
import atlantis.information.enemy.EnemyUnits;
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
            ? attackAnchor() : defendAnchor();

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
            order(unit, tactic, anchor);
        }

        return false;
    }

    private FightOrFlight.Tactic tactic() {
        AUnit leader = Alpha.get().leader();
        double eval = leader != null ? leader.eval() : 0;

        FightOrFlight.Tactic tactic = FightOrFlight.decide(eval, A.now(), fleeStartedAt);
        if (tactic == FightOrFlight.Tactic.FLEE && fleeStartedAt < 0) {
            fleeStartedAt = A.now();
        }
        if (tactic == FightOrFlight.Tactic.FIGHT) {
            fleeStartedAt = -1;
        }
        return tactic;
    }

    private void order(AUnit unit, FightOrFlight.Tactic tactic, HasPosition anchor) {
        if (unit == null || !unit.isAlive()) return;

        if (tactic == FightOrFlight.Tactic.FLEE) {
            HasPosition home = defendAnchor();
            if (home != null) unit.move(home, Actions.RUN_ENEMY, "ClusterFlee");
            return;
        }

        AUnit target = atlantis.battle.targeting.ASquadTargeting.useSquadTargetIfPossible(unit);
        if (target != null) {
            unit.attackUnit(target);
            return;
        }
        if (anchor != null) unit.move(anchor, Actions.MOVE_ENGAGE, "ClusterMove");
    }

    private void gatherCluster() {
        Selection army = Select.ourCombatUnits();
        if (army == null) return;
        for (AUnit unit : army.list()) {
            if (unit != null && unit.isAlive() && unit.squad() == null) {
                Alpha.get().addUnit(unit);
            }
        }
    }

    private HasPosition defendAnchor() {
        return Select.mainOrAnyBuilding();
    }

    private HasPosition attackAnchor() {
        AUnit base = EnemyUnits.enemyBase();
        if (base != null) return base.position();
        HasPosition main = EnemyUnits.enemyMainBase();
        if (main != null) return main;
        return Select.mainOrAnyBuilding();
    }
}
