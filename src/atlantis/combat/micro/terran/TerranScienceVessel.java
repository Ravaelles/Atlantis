
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

    public static boolean update(AUnit scienceVessel) {
        if (useTech(scienceVessel)) {
            return true;
        }

        return MobileDetector.update(scienceVessel);
    }

    // =========================================================

    private static boolean useTech(AUnit scienceVessel) {
        if (scienceVessel.lastTechUsedAgo() <= 10) {
            return true;
        }

        if (Enemy.protoss()) {
            if (scienceVessel.energy(100) && ATech.isResearched(TechType.EMP_Shockwave)) {
                if (empShockwave(scienceVessel)) {
                    scienceVessel.setTooltipTactical("EMP!");
                    return true;
                }
            }
        }

        if (Enemy.zerg() || A.isUms()) {
            if (scienceVessel.energy(75) && ATech.isResearched(TechType.Irradiate)) {
                if (irradiate(scienceVessel)) {
                    scienceVessel.setTooltipTactical("Irradiate!");
                    return true;
                }
            }
        }

        return false;
    }

    // =========================================================

    private static boolean irradiate(AUnit scienceVessel) {
        Selection enemies = Select.enemyCombatUnits().inRadius(10, scienceVessel);
        if (enemies.count() >= 5 || (enemies.count() >= 3 && scienceVessel.energy(181))) {
            APosition center = enemies.center();
            if (center != null) {
                center = Select.enemyCombatUnits().inRadius(3, center).center();
            } else {
                center = enemies.random().position();
            }

            if (center != null) {
                return scienceVessel.useTech(TechType.Irradiate, center);
            } else {
                System.err.println("Irradiate center is NULL / " + enemies.count());
                return scienceVessel.useTech(TechType.Irradiate, enemies.first());
            }
        }

        // Crucial enemies
        AUnit enemy = Select.enemy().ofType(
                AUnitType.Zerg_Lurker, AUnitType.Zerg_Mutalisk, AUnitType.Zerg_Ultralisk, AUnitType.Zerg_Defiler,
                AUnitType.Zerg_Guardian, AUnitType.Zerg_Scourge,
                AUnitType.Protoss_High_Templar, AUnitType.Protoss_Archon, AUnitType.Protoss_Dark_Archon,
                AUnitType.Terran_Medic
        ).effVisible().inRadius(15, scienceVessel).mostDistantTo(scienceVessel);
        if (enemy != null) {
            return scienceVessel.useTech(TechType.Irradiate, enemy);
        }

        // Regular enemies
        enemy = Select.enemy().ofType(
                AUnitType.Zerg_Zergling, AUnitType.Zerg_Drone,
                AUnitType.Protoss_Dragoon, AUnitType.Protoss_Zealot,
                AUnitType.Terran_Marine
        ).effVisible().inRadius(15, scienceVessel).mostDistantTo(scienceVessel);
        if (enemy != null) {
            return scienceVessel.useTech(TechType.Irradiate, enemy);
        }

        return false;
    }

    private static boolean empShockwave(AUnit scienceVessel) {
        Selection enemies = Select.enemyCombatUnits().inRadius(10, scienceVessel);

        if (enemies.count() >= 7 || (enemies.count() >= 4 && scienceVessel.energy(180))) {
            APosition center = enemies.center();
            if (center != null) {
                center = Select.enemyCombatUnits().inRadius(3, center).center();
            } else {
                center = enemies.random().position();
            }

            return scienceVessel.useTech(TechType.EMP_Shockwave, center);
        }

        return false;
    }

}
