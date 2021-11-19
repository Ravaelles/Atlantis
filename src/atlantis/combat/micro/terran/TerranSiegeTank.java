package atlantis.combat.micro.terran;

import atlantis.AGame;
import atlantis.combat.missions.Missions;
import atlantis.debug.APainter;
import atlantis.map.AChoke;
import atlantis.map.Chokes;
import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.UnitActions;
import atlantis.units.select.Select;
import atlantis.util.A;
import bwapi.Color;


public class TerranSiegeTank {
    private static AUnit nearestEnemyUnit;
    private static double nearestEnemyUnitDist;
    private static AUnit nearestEnemyCombatBuilding;
    private static double nearestEnemyCombatBuildingDist;

    public static boolean update(AUnit tank) {
//        if (shouldNotDisturb(tank)) {
//            tank.setTooltip("Can't interrupt");
//            return true;
//        }
        
        initCache(tank);

        return tank.isSieged() ? updateWhenSieged(tank) : updateWhenNotSieged(tank);
    }

    // =========================================================


    private static boolean updateWhenNotSieged(AUnit unit) {
        if (unit.lastActionLessThanAgo(30 * 5, UnitActions.UNSIEGE)) {
            return false;
        }

        if (handleNearEnemyCombatBuilding(unit)) {
            return true;
        }

        // =========================================================

        boolean longNotMoved = !unit.isMoving() && unit.lastActionMoreThanAgo(60, UnitActions.MOVE);

//        if (Missions.isGlobalMissionContain() || Missions.isGlobalMissionDefend()) {
        APosition focusPoint = Missions.globalMission().focusPoint();
        if (
                focusPoint != null
                        && (unit.distTo(focusPoint) <= 9)
                        && (canSiegeHere(unit) || longNotMoved)
        ) {
            unit.siege();
            unit.setTooltip("Contain siege!");
            return true;
        }

//        return false;
//        }

        // =========================================================

        // If tank is holding position, siege
//        if (Missions.getGlobalMission().isMissionDefend() && canSiegeHere(tank)) {
//            tank.siege();
//            tank.setTooltip("Hold & siege");
//            return true;
//        }

        if (nearestEnemyUnit != null) {
            return nearestEnemyIsUnit(unit, nearestEnemyUnit, nearestEnemyUnitDist);
        }

        // =========================================================

        return false;
    }

    private static boolean handleShootingAtInvisibleUnits(AUnit tank) {
        for (AUnit enemy : Select.enemyFoggedUnits().effCloaked().groundUnits().inRadius(12, tank).list()) {
            if (enemy.distTo(tank) >= tank.getGroundWeaponMinRange()) {
                if (tank.lastActionMoreThanAgo(30, UnitActions.ATTACK_POSITION)) {
                    tank.setTooltip("SMASH invisible!");
                    tank.attackPosition(enemy.position());
                }
                tank.setTooltip("SmashInvisible");
                return true;
            }
        }

        for (AUnit enemy : Select.enemy().effCloaked().groundUnits().inRadius(12, tank).list()) {
            if (enemy.distTo(tank) >= tank.getGroundWeaponMinRange()) {
                if (tank.lastActionMoreThanAgo(30, UnitActions.ATTACK_POSITION)) {
                    tank.setTooltip("SMASH invisible!");
                    tank.attackPosition(enemy.position());
                }
                tank.setTooltip("SmashInvisible");
                return true;
            }
        }

        return false;
    }

    private static boolean updateWhenSieged(AUnit unit) {
        if (handleShootingAtInvisibleUnits(unit)) {
            return true;
        }

        // =========================================================

        // Mission is CONTAIN
        if (Missions.isGlobalMissionContain()) {
            return false;
        }

        // =========================================================

        if (unit.lastActionLessThanAgo(30 * 5, UnitActions.SIEGE)) {
            return false;
        }

        // =========================================================
        // Should siege?

        if (tooLonely(unit) && hasJustSiegedRecently(unit)) {
            unit.unsiege();
            unit.setTooltip("TooLonely");
            return true;
        }

        if (
                (nearestEnemyUnit == null && nearestEnemyCombatBuilding == null)
                || (nearestEnemyUnitDist > 11.9 && nearestEnemyCombatBuildingDist > 11.9)
        ) {
            unit.setTooltip("Considers unsiege");

            if (AGame.isUms()) {
                unit.unsiege();
                unit.setTooltip("Unsiege");
                return true;
            }

            if (unit.mission() == null) {
                System.err.println("Mission NULL for " + unit);
                System.err.println("Squad: " + unit.squad());
                return false;
            }

            if (!hasJustSiegedRecently(unit)) {
                if (unit.mission().isMissionAttack() && A.chance(1.5)) {
                    unit.unsiege();
                    unit.setTooltip("Unsiege");
                    return true;
                }

                if (unit.mission().isMissionContain()) {
                    APosition focusPoint = Missions.globalMission().focusPoint();
                    if (focusPoint != null && unit.distTo(focusPoint) >= 12.5 && A.chance(1)) {
                        unit.unsiege();
                        unit.setTooltip("Unsiege");
                        return true;
                    }
                }
            }
        }
        
        return false;
    }

    private static boolean hasJustSiegedRecently(AUnit unit) {
        return unit.lastActionLessThanAgo(30 * 9, UnitActions.SIEGE);
    }

    private static boolean shouldNotDisturb(AUnit tank) {
        return tank.lastActionLessThanAgo(15, UnitActions.SIEGE)
                || tank.lastActionLessThanAgo(15, UnitActions.UNSIEGE);
    }

    private static void initCache(AUnit tank) {
        nearestEnemyUnit = Select.enemyRealUnits().combatUnits().groundUnits().nearestTo(tank);
        nearestEnemyUnitDist = nearestEnemyUnit != null ? tank.distTo(nearestEnemyUnit) : 999;

        nearestEnemyCombatBuilding = Select.enemy().buildings().nearestTo(tank);
        nearestEnemyCombatBuildingDist = nearestEnemyCombatBuilding != null ? tank.distTo(nearestEnemyCombatBuilding) : 999;

        if (nearestEnemyCombatBuilding != null) {
            APainter.paintCircle(nearestEnemyCombatBuilding, 22, Color.Orange);
            APainter.paintCircle(nearestEnemyCombatBuilding, 20, Color.Orange);
            APainter.paintCircle(nearestEnemyCombatBuilding, 18, Color.Orange);
            APainter.paintTextCentered(nearestEnemyCombatBuilding, A.digit(nearestEnemyCombatBuildingDist), Color.Yellow);
        }
    }

    // =========================================================

    private static boolean tooLonely(AUnit tank) {
        return Select.ourCombatUnits().inRadius(6, tank).atMost(4);
    }

    private static boolean handleNearEnemyCombatBuilding(AUnit tank) {
//        AUnit building = Select.enemy().combatBuildings().inRadius(12.5, tank).nearestTo(tank);
//        tank.setTooltip("Buildz:" + Select.enemy().combatBuildings().count());

        if (nearestEnemyCombatBuilding != null) {
            if (
                    (tank.distToLessThan(nearestEnemyCombatBuilding, 10.5) && canSiegeHere(tank, false))
                    || tank.distToLessThan(nearestEnemyCombatBuilding, 9.3)
            ) {
//                tank.setTooltip("Buildz:" + Select.enemy().combatBuildings().count() + "," + A.digit(tank.distTo(nearestEnemyCombatBuilding)));
                tank.siege();
                tank.setTooltip("SiegeBuilding" + A.dist(tank, nearestEnemyCombatBuilding));
                return true;
            }
        }

        return false;
    }

    private static boolean nearestEnemyIsUnit(AUnit tank, AUnit enemy, double distanceToEnemy) {
        int supportUnitsNearby = Select.ourCombatUnits().inRadius(10, tank).count();

        if (supportUnitsNearby <= 5) {
            return false;
        }
        
        // Don't siege when enemy is too close
        if (distanceToEnemy < 10 && !enemy.isRanged()) {
            tank.setTooltip("Dont siege");
            return false;
        }
        
        if (distanceToEnemy < 13.6 && enemy.type().isDangerousGroundUnit() && canSiegeHere(tank)) {
            tank.siege();
            tank.setTooltip("Better siege");
            return true;
        }

        if (distanceToEnemy <= 12 && canSiegeHere(tank) && !tooLonely(tank)) {
            tank.siege();
            tank.setTooltip("Siege!");
            return true;
        }

        return false;
    }
    
    // =========================================================
    
    private static boolean canSiegeHere(AUnit tank) {
        return canSiegeHere(tank, true);
    }

    private static boolean canSiegeHere(AUnit tank, boolean checkTooLonely) {
        if (checkTooLonely && tooLonely(tank)) {
            return false;
        }

        AChoke choke = Chokes.nearestChoke(tank.position());
        if (choke == null) {
            return true;
        }

        return (tank.distTo(choke.center()) - choke.width()) >= 1.3 || (choke.width() >= 3.5);
    }

}
