package atlantis.combat.micro.terran;

import atlantis.AGame;
import atlantis.combat.missions.Missions;
import atlantis.map.AChoke;
import atlantis.map.Chokes;
import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.UnitActions;
import atlantis.units.select.Select;
import atlantis.util.A;


public class TerranSiegeTank {
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

        if (tooLonely(unit)) {
            unit.unsiege();
            unit.setTooltip("TooLonely");
            return true;
        }

        if (
                (nearestEnemyUnit == null && nearestEnemyBuilding == null)
                || (nearestEnemyUnitDist >= 16 && nearestEnemyBuildingDist > 12.2)
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

            if (unit.mission().isMissionAttack() && A.chance(2)) {
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
        
        return false;
    }

    private static boolean handleShootingAtInvisibleUnits(AUnit tank) {
//        if (tank.cooldownRemaining() <= 3) {
            for (AUnit enemy : Select.enemyRealUnits().effCloaked().groundUnits().inRadius(12, tank).list()) {
                if (enemy.distTo(tank) >= tank.getGroundWeaponMinRange()) {
                    if (tank.lastActionMoreThanAgo(30, UnitActions.ATTACK_POSITION)) {
                        tank.setTooltip("SMASH invisible!");
                        tank.attackPosition(enemy.position());
                    }
                    tank.setTooltip("SmashInvisible");
                    return true;
                }
            }
//        }

        return false;
    }

    /**
     * Not sieged
     */
    private static boolean updateWhenUnsieged(AUnit unit) {

        // Mission is CONTAIN
        if (Missions.isGlobalMissionContain() || Missions.isGlobalMissionDefend()) {
            APosition focusPoint = Missions.globalMission().focusPoint();
            if (focusPoint != null && unit.distTo(focusPoint) <= 7.2 && canSiegeHere(unit)) {
                unit.siege();
                unit.setTooltip("Contain siege!");
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

        AUnit nearEnemyCombatBuilding = Select.enemy().combatBuildings().inRadius(11.9, unit).first();
        if (nearEnemyCombatBuilding != null) {
            return handleNearEnemyCombatBuilding(unit, nearEnemyCombatBuilding);
        }
        
        if (nearestEnemyUnit != null) {
            return nearestEnemyIsUnit(unit, nearestEnemyUnit, nearestEnemyUnitDist);
        }
        
        // =========================================================
        
        return false;
    }

    // =========================================================

    private static boolean tooLonely(AUnit tank) {
        return Select.ourCombatUnits().inRadius(6, tank).atMost(4);
    }

    private static boolean handleNearEnemyCombatBuilding(AUnit tank, AUnit combatBuilding) {
        double distanceToEnemy = tank.distTo(combatBuilding);
        
        if (distanceToEnemy <= 11.9 && canSiegeHere(tank)) {
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
        if (tooLonely(tank)) {
            return false;
        }

        AChoke choke = Chokes.nearestChoke(tank.position());
        if (choke == null) {
            return true;
        }
        else {
            return tank.distTo(choke.getCenter()) > 4 || (choke.getWidth() > 3);
        }
    }

}
