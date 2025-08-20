package atlantis.combat.retreating.protoss;

import atlantis.architecture.Manager;
import atlantis.combat.missions.MissionManager;
import atlantis.combat.squad.squads.alpha.Alpha;
import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.information.enemy.EnemyArmyCenter;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class ProtossForceRetreatDuringDefend extends MissionManager {
    private double groundDistToMain;

    public ProtossForceRetreatDuringDefend(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isMissionDefend()) return false;
        if (unit.isAir()) return false;
        if (!unit.squadIsAlpha()) return false;

        double distToFocusPoint = unit.distToFocusPoint();
        if (distToFocusPoint >= 20) return true;

        if (distToFocusPoint <= 7) return false;

        if (unit.hp() <= 40 && unit.isRunning() && unit.distToTargetMoreThan(2)) return false;
//        if (unit.leaderEval() >= 4 && unit.eval() >= 2.5) return false;
//        if (unit.leaderIsAttacking()) return false;
        if (isTooCloseToBaseToRetreat()) return false;

        if (allowDragoonsVsLings() && unit.isDragoon()) return false;
        if (unit.meleeEnemiesNearCount(2.7) >= 2) return false;
        if (Enemy.zerg() && unit.enemiesThatCanAttackMe(2.9).atLeast(unit.hp() <= 60 ? 1 : 2)) return false;
        if (Enemy.protoss() && unit.enemiesThatCanAttackMe(2.9).atLeast(unit.hp() <= 60 ? 1 : 2)) return false;
        if (unit.enemiesNear().tanks().notEmpty() && unit.friendsNear().buildings().notEmpty()) return false;
        if (unit.noCooldown() && unit.friendsNear().bases().countInRadius(5, unit) > 0) return false;

        if (preventIfEnemyArmyIsMuchCloserToOurMain()) return false;

        return true;
    }

    private boolean preventIfEnemyArmyIsMuchCloserToOurMain() {
        HasPosition center = EnemyArmyCenter.get();
        if (center == null) return false;

        AUnit leader = unit.squadLeader();
        if (leader == null) return false;

        return center.groundDistToMain() + 10 < leader.groundDistToMain();
    }

    @Override
    public Manager handle() {
        if (focus == null || (unit.distTo(focus) <= 6 || groundDistToMain <= 40)) return null;

        if (unit.distToLeader() >= 5 && unit.eval() >= 2) {
            if (unit.moveToLeader(Actions.MOVE_FORMATION, "RetreatToLeader")) return usedManager(this);
        }

        if (unit.move(focus, Actions.MOVE_FOCUS)) {
            return usedManager(this, "RetreatToFocusPoint");
        }

        return null;
    }

    private boolean allowDragoonsVsLings() {
        if (!Enemy.zerg()) return false;
        if (!unit.isDragoon()) return false;

        return unit.hp() >= 90
            && unit.enemiesNear().ranged().empty()
            && unit.enemiesNear().zerglings().atMost(4);
    }

    private boolean isTooCloseToBaseToRetreat() {
        if ((groundDistToMain = unit.groundDistToMain()) <= A.whenEnemyZerg(50, 60)) {
            return true;
        }

        if (A.supplyUsed() >= 50 && unit.distToBase() <= 15) {
            return true;
        }

        return false;
    }
}
