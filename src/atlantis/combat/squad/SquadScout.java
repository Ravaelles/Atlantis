package atlantis.combat.squad;

import atlantis.architecture.Manager;
import atlantis.combat.micro.attack.AttackNearbyEnemies;
import atlantis.combat.missions.MissionChanger;
import atlantis.combat.missions.Missions;
import atlantis.debug.painter.APainter;
import atlantis.game.A;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import bwapi.Color;

public class SquadScout extends Manager {
    public SquadScout(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isSquadScout();
    }

    public Manager handle() {
        if (unit.isSquadScout()) {
            return handleSquadScout();
        }

        return null;
    }

    // =========================================================

    private Manager handleSquadScout() {
        APosition positionToEngage = positionToEngageEarlyOn(unit);

        if (positionToEngage != null) {
            APainter.paintCircle(positionToEngage, 25, Color.Orange);
            String dist = A.dist(unit, positionToEngage);
            APainter.paintTextCentered(positionToEngage, "SquadScout" + dist, Color.Orange);

            if (positionToEngage.distTo(unit) > 2.2) {
                unit.move(positionToEngage, Actions.MOVE_ENGAGE, "Pioneer" + dist, true);
                return usedManager(this);
            }
            else {
                if (engageWorkersNow(unit)) return usedManager(this);
            }
        }
        else {
            unit.setTooltipTactical("NoEngagePosition");
            if (EnemyInfo.hasDiscoveredAnyBuilding()) {
                System.err.println("positionToEngage null, base = " + EnemyUnits.enemyBase());
            }
        }

        return null;
    }

    private boolean engageWorkersNow(AUnit squadScout) {
        if (AttackNearbyEnemies.handleAttackNearEnemyUnits(squadScout)) {
            squadScout.setTooltipTactical("MadeContact");

            if (EnemyUnits.discovered().atMost(2)) {
                squadScout.addLog("Squad scout forced GLOBAL ATTACK");
                if (!Missions.isGlobalMissionAttack()) {
                    MissionChanger.forceMissionAttack("EngageWorkersNow");
                }
            }

            return true;
        }

        return false;
    }

    private APosition positionToEngageEarlyOn(AUnit squadScout) {
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
