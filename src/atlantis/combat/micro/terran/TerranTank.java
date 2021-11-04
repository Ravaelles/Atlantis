package atlantis.combat.micro.terran;

import atlantis.AGame;
import atlantis.combat.missions.Missions;
import atlantis.map.AChoke;
import atlantis.map.MapChokes;
import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.util.A;


public class TerranTank {
    private static AUnit nearestEnemyUnit;
    private static double nearestEnemyUnitDist;
    private static AUnit nearestEnemyBuilding;
    private static double nearestEnemyBuildingDist;

    public static boolean update(AUnit tank) {
        if (!tank.isInterruptible()) {
            tank.setTooltip("Can't interrupt");
            return true;
        }
        
        // =========================================================
        
        nearestEnemyUnit = Select.enemyRealUnits().combatUnits().groundUnits().nearestTo(tank);
        nearestEnemyUnitDist = nearestEnemyUnit != null ? tank.distTo(nearestEnemyUnit) : 999;

        nearestEnemyBuilding = Select.enemy().buildings().nearestTo(tank);
        nearestEnemyBuildingDist = nearestEnemyBuilding != null ? tank.distTo(nearestEnemyBuilding) : 999;

//        String string = (enemy != null ? enemy.shortName() : "NULL");
//        if (enemy != null) {
//             string += " (" + enemy.distanceTo(tank) + ")";
//        }
//        AtlantisPainter.paintTextCentered(tank.getPosition().translateByPixels(0, 16), 
//                string, 
//                Color.Red);

        // =========================================================
        
        if (tank.isSieged()) {
            return updateWhenSieged(tank);
        }
        else {
            return updateWhenUnsieged(tank);
        }
        
//        // =========================================================
//        
//        tank.setTooltip("Ta-ta-ta!");
//        return false;
    }

    // =========================================================
    
    /**
     * Sieged
     */
    private static boolean updateWhenSieged(AUnit tank) {

        if (handleShootingAtInvisibleUnits(tank)) {
            return true;
        }

        // =========================================================

        // Mission is CONTAIN
        if (Missions.isGlobalMissionContain()) {
            return false;
        }

        // =========================================================

        if ((nearestEnemyUnit == null && nearestEnemyBuilding == null)
                || (nearestEnemyUnitDist >= 16 && nearestEnemyBuildingDist > 12.2)) {
            tank.setTooltip("Considers unsiege");

            if (AGame.isUms()) {
                tank.unsiege();
                tank.setTooltip("Unsiege");
                return true;
            }

            if (tank.mission() == null) {
                System.err.println("Mission NULL for " + tank);
                return false;
            }

            if (tank.mission().isMissionAttack() && A.chance(2)) {
                tank.unsiege();
                tank.setTooltip("Unsiege");
                return true;
            }

            if (tank.mission().isMissionContain()) {
                APosition focusPoint = Missions.globalMission().focusPoint();
                if (focusPoint != null && tank.distTo(focusPoint) >= 13 && A.chance(2)) {
                    tank.unsiege();
                    tank.setTooltip("Unsiege");
                    return true;
                }
            }
        }
        
        return false;
    }

    private static boolean handleShootingAtInvisibleUnits(AUnit tank) {
        if (tank.cooldownRemaining() <= 3) {
            for (AUnit enemy : Select.enemyRealUnits().effCloaked().inRadius(11, tank).list()) {
                if (enemy.distTo(tank) >= tank.getGroundWeaponMinRange()) {
                    tank.attackPosition(enemy.position());
                    tank.setTooltip("Smash invisible!");
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Not sieged
     */
    private static boolean updateWhenUnsieged(AUnit tank) {

        // Mission is CONTAIN
        if (Missions.isGlobalMissionContain()) {
            APosition focusPoint = Missions.globalMission().focusPoint();
            if (focusPoint != null && tank.distTo(focusPoint) <= 7.2) {
                tank.siege();
                tank.setTooltip("Contain siege!");
                return true;
            }

            return false;
        }

        // =========================================================

        // If tank is holding position, siege
//        if (Missions.getGlobalMission().isMissionDefend() && canSiegeHere(tank)) {
//            tank.siege();
//            tank.setTooltip("Hold & siege");
//            return true;
//        }

        AUnit nearEnemyCombatBuilding = Select.enemy().combatBuildings().inRadius(10.9, tank).first();
        if (nearEnemyCombatBuilding != null) {
            return handleNearEnemyCombatBuilding(tank, nearEnemyCombatBuilding);
        }
        
        if (nearestEnemyUnit != null) {
            return nearestEnemyIsUnit(tank, nearestEnemyUnit, nearestEnemyUnitDist);
        }
        
        // =========================================================
        
        return false;
    }
    
    // =========================================================
    
    private static boolean handleNearEnemyCombatBuilding(AUnit tank, AUnit combatBuilding) {
        double distanceToEnemy = tank.distTo(combatBuilding);
        
        if (distanceToEnemy <= 9.5) {
            tank.siege();
            tank.setTooltip("Siege - building");
            return true;
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
        
        if (distanceToEnemy < 12.6 && enemy.type().isDangerousGroundUnit() && canSiegeHere(tank)) {
            tank.siege();
            tank.setTooltip("Better siege");
            return true;
        }

        if (distanceToEnemy <= 12 && canSiegeHere(tank)) {
            tank.siege();
            tank.setTooltip("Siege!");
            return true;
        }

        return false;
    }
    
    // =========================================================
    
    private static boolean canSiegeHere(AUnit tank) {
        AChoke choke = MapChokes.nearestChoke(tank.position());
        if (choke == null) {
            return true;
        }
        else {
            return tank.distTo(choke.getCenter()) > 4 || (choke.getWidth() > 3);
        }
    }

}
