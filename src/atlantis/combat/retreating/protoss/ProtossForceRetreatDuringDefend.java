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
    private double distToMain;

    public ProtossForceRetreatDuringDefend(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isMissionDefendOrSparta()) return false;
        if (unit.isAir()) return false;
        if (!unit.isCombatUnit()) return false;
//        if (!unit.squadIsAlpha()) return false;

        double distToFocusPoint = unit.distToFocusPoint();
        if (distToFocusPoint <= 10) return false;

        distToMain = unit.groundDistToMain();
        if (distToMain <= 25) return false;
        if (distToMain <= 60 && unit.enemiesThatCanAttackMe(3).notEmpty()) return false;

        if (unit.friendsNear().cannons().countInRadius(2, unit) > 0) return false;

        if (distToMain >= 60) return true;
        if (distToMain >= 50 && unit.eval() <= 10) return true;
        if (distToFocusPoint >= 10) return true;

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
        if (unit.distToCannon() <= 1.2) return null;

        if (distToMain >= 40 && unit.moveToMain(Actions.RUN_RETREAT)) {
            return usedManager(this);
        }

        if (focus != null && focus.distTo(unit) >= 10 && unit.move(focus, Actions.MOVE_FOCUS)) {
            return usedManager(this, "RetreatToFocusPoint");
        }

        if (unit.distToLeader() >= 8 && unit.eval() >= 3) {
            if (unit.moveToLeader(Actions.MOVE_FORMATION, "RetreatToLeader")) return usedManager(this);
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
