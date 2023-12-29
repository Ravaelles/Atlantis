package atlantis.combat.squad.squad_scout;

import atlantis.architecture.Manager;
import atlantis.combat.managers.*;
import atlantis.combat.micro.attack.AttackNearbyEnemies;
import atlantis.combat.micro.avoid.special.AvoidSpellsAndMines;
import atlantis.combat.missions.MissionChanger;
import atlantis.combat.missions.Missions;
import atlantis.debug.painter.APainter;
import atlantis.game.A;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.ArmyStrength;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.special.SpecialUnitsManager;
import atlantis.util.Enemy;
import bwapi.Color;

public class SquadScout extends Manager {
    public SquadScout(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return allowedToUseScout()
            && unit.isSquadScout();
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            SquadScoutSafety.class,
            SquadScoutProceed.class,
        };
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
//            if (EnemyInfo.hasDiscoveredAnyBuilding()) {
//                System.err.println("positionToEngage null, base = " + EnemyUnits.enemyBase());
//            }
        }

        return null;
    }

    private boolean allowedToUseScout() {
        if (Enemy.terran()) return false;
        if (A.seconds() >= 800) return false;

//        return ArmyStrength.ourArmyRelativeStrength() >= 120 && unit.hp() >= 30;
        return unit.hp() >= 30;
    }

    private boolean engageWorkersNow(AUnit squadScout) {
        if ((new AttackNearbyEnemies(squadScout)).handleAttackNearEnemyUnits()) {
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
