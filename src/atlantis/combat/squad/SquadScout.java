package atlantis.combat.squad;

import atlantis.combat.micro.AAttackEnemyUnit;
import atlantis.combat.micro.avoid.AvoidEnemies;
import atlantis.combat.missions.MissionChanger;
import atlantis.combat.missions.Missions;
import atlantis.debug.painter.APainter;
import atlantis.game.A;
import atlantis.game.GameLog;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.fogged.AbstractFoggedUnit;
import atlantis.units.actions.Actions;
import bwapi.Color;

public class SquadScout {

    public static boolean handle(AUnit unit) {
        if (unit.equals(unit.squad().squadScout())) {
            return handleSquadScout(unit);
        }

        return false;
    }

    // =========================================================

    private static boolean handleSquadScout(AUnit squadScout) {
        if (AvoidEnemies.avoidEnemiesIfNeeded(squadScout)) {
            return true;
        }

        APosition positionToEngage = positionToEngageEarlyOn(squadScout);

        if (positionToEngage != null) {
            APainter.paintCircle(positionToEngage, 25, Color.Orange);
            String dist = A.dist(squadScout, positionToEngage);
            APainter.paintTextCentered(positionToEngage, "SquadScout" + dist, Color.Orange);

            if (positionToEngage.distTo(squadScout) > 2.2) {
                return squadScout.move(positionToEngage, Actions.MOVE_ENGAGE, "Pioneer" + dist, true);
            }
            else {
                engageWorkersNow(squadScout);
                return true;
            }
        }
        else {
            squadScout.setTooltipTactical("NoEngagePosition");
            if (EnemyInfo.hasDiscoveredAnyBuilding()) {
                System.err.println("positionToEngage null, base = " + EnemyUnits.enemyBase());
            }
        }

        return false;
    }

    private static void engageWorkersNow(AUnit squadScout) {
        AAttackEnemyUnit.handleAttackNearEnemyUnits(squadScout);
        squadScout.setTooltipTactical("MadeContact");

        if (EnemyUnits.discovered().atMost(2)) {
            squadScout.addLog("Squad scout forced GLOBAL ATTACK");
            if (!Missions.isGlobalMissionAttack()) {
                MissionChanger.forceMissionAttack("EngageWorkersNow");
            }
        }
    }

    private static APosition positionToEngageEarlyOn(AUnit squadScout) {
        APosition positionToEngage = null;

        AUnit enemyBase = EnemyUnits.enemyBase();
        if (enemyBase != null) {
            positionToEngage = enemyBase.position();
        }

        if (positionToEngage == null) {
            AUnit nearestEnemyBuilding = EnemyUnits.nearestEnemyBuilding();
            if (nearestEnemyBuilding != null) {
                positionToEngage = nearestEnemyBuilding.position();
            }
        }

        return positionToEngage;
    }

}
