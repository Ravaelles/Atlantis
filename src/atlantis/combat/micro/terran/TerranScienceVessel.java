
package atlantis.combat.micro.terran;

import atlantis.combat.micro.generic.MobileDetector;
import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;
import atlantis.wrappers.ATech;
import bwapi.TechType;

public class TerranScienceVessel extends MobileDetector {

    protected static AUnitType type = AUnitType.Terran_Science_Vessel;

    // =========================================================

    public static boolean update(AUnit scienceVessel) {
        if (useTech(scienceVessel)) {
            return true;
        }

        return MobileDetector.update(scienceVessel);
    }

    // =========================================================

    private static boolean useTech(AUnit scienceVessel) {
        if (Enemy.protoss()) {
            if (scienceVessel.energy(100) && ATech.isResearched(TechType.EMP_Shockwave)) {
                if (empShockwave(scienceVessel)) {
                    scienceVessel.setTooltip("EMP!");
                    return true;
                }
            }
        }

        if (Enemy.zerg()) {
            if (scienceVessel.energy(75) && ATech.isResearched(TechType.Irradiate)) {
                if (irradiate(scienceVessel)) {
                    scienceVessel.setTooltip("Irradiate!");
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
            center = Select.enemyCombatUnits().inRadius(3, center).center();

            return scienceVessel.useTech(TechType.Irradiate, center);
        }

        return false;
    }

    private static boolean empShockwave(AUnit scienceVessel) {
        Selection enemies = Select.enemyCombatUnits().inRadius(10, scienceVessel);

        if (enemies.count() >= 7 || (enemies.count() >= 4 && scienceVessel.energy(180))) {
            APosition center = enemies.center();
            center = Select.enemyCombatUnits().inRadius(3, center).center();

            return scienceVessel.useTech(TechType.EMP_Shockwave, center);
        }

        return false;
    }

}
