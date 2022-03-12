
package atlantis.combat.micro.terran;

import atlantis.combat.micro.generic.MobileDetector;
import atlantis.game.A;
import atlantis.information.tech.ATech;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;
import bwapi.TechType;

public class TerranScienceVessel extends MobileDetector {

    public static AUnitType type() {
        return AUnitType.Terran_Science_Vessel;
    }

    // =========================================================

    public static boolean update(AUnit unit) {
        if (useTech(unit)) {
            return true;
        }

        return MobileDetector.update(unit);
    }

    // =========================================================

    private static boolean useTech(AUnit unit) {
        if (unit.energy() <= 74) {
            return false;
        }

        if (unit.lastTechUsedAgo() <= 10) {
            return true;
        }

        if (Enemy.protoss()) {
            if (unit.energy(100) && ATech.isResearched(TechType.EMP_Shockwave)) {
                if (empShockwave(unit)) {
                    unit.setTooltipTactical("EMP!");
                    return true;
                }
            }
        }

        if (Enemy.zerg() || A.isUms()) {
            if (unit.energy(75) && ATech.isResearched(TechType.Irradiate)) {
                if (irradiate(unit)) {
                    unit.setTooltipTactical("Irradiate!");
                    return true;
                }
            }
        }

        if (defensiveMatrix(unit)) {
            return true;
        }

        return false;
    }

    // =========================================================

    private static boolean defensiveMatrix(AUnit unit) {
        if (unit.energy() < 100) {
            return false;
        }

        Selection tanks = unit.friendsNear().tanks().wounded();
        if (tanks.notEmpty()) {
            for (AUnit tank : tanks.list()) {
                if (tank.lastUnderAttackLessThanAgo(10) && tank.meleeEnemiesNearCount() >= 2) {
                    return unit.useTech(TechType.Defensive_Matrix, tank);
                }
            }
        }

        return false;
    }

    private static boolean irradiate(AUnit unit) {
        Selection enemies = Select.enemyCombatUnits().inRadius(10, unit);
        if (enemies.count() >= 5 || (enemies.count() >= 3 && unit.energy(181))) {
            APosition center = enemies.center();
            if (center != null) {
                center = Select.enemyCombatUnits().inRadius(3, center).center();
            } else {
                center = enemies.random().position();
            }

            if (center != null) {
                return unit.useTech(TechType.Irradiate, center);
            } else {
                System.err.println("Irradiate center is NULL / " + enemies.count());
                return unit.useTech(TechType.Irradiate, enemies.first());
            }
        }

        // Crucial enemies
        AUnit enemy = Select.enemy().ofType(
                AUnitType.Zerg_Lurker, AUnitType.Zerg_Mutalisk, AUnitType.Zerg_Ultralisk, AUnitType.Zerg_Defiler,
                AUnitType.Zerg_Guardian, AUnitType.Zerg_Scourge,
                AUnitType.Protoss_High_Templar, AUnitType.Protoss_Archon, AUnitType.Protoss_Dark_Archon,
                AUnitType.Terran_Medic
        ).effVisible().inRadius(15, unit).mostDistantTo(unit);
        if (enemy != null) {
            return unit.useTech(TechType.Irradiate, enemy);
        }

        // Regular enemies
        enemy = Select.enemy().ofType(
                AUnitType.Zerg_Zergling, AUnitType.Zerg_Drone,
                AUnitType.Protoss_Dragoon, AUnitType.Protoss_Zealot,
                AUnitType.Terran_Marine
        ).effVisible().inRadius(15, unit).mostDistantTo(unit);
        if (enemy != null) {
            return unit.useTech(TechType.Irradiate, enemy);
        }

        return false;
    }

    private static boolean empShockwave(AUnit unit) {
        Selection enemies = Select.enemyCombatUnits().inRadius(10, unit);

        if (enemies.count() >= 7 || (enemies.count() >= 4 && unit.energy(180))) {
            APosition center = enemies.center();
            if (center != null) {
                center = Select.enemyCombatUnits().inRadius(3, center).center();
            } else {
                center = enemies.random().position();
            }

            return unit.useTech(TechType.EMP_Shockwave, center);
        }

        return false;
    }

}
