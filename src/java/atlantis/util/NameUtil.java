package atlantis.util;

import atlantis.units.AUnitType;
import java.lang.reflect.Field;

import bwapi.UnitType;
import bwapi.TechType;
import bwapi.UpgradeType;

/**
 * Utilities for retrieving names of game entities
 *
 * @author Anderson TODO: check whether AUnitType.class.getFields() from bwapi works the same way as in
 * bwapi.UnitTypes
 */
public class NameUtil {

    /**
     * Returns a shorter name for a UnitType
     *
     * @param t
     * @return
     */
//    public static String getShortName(AUnitType t) {
//        return t.toString().replace("Terran_", "").replace("Protoss_", "").replace("Zerg_", "");
//    }

    public static boolean disableErrorReporting = false;

    /**
     * You can "Terran_Marine" or "Terran Marine" or even "Marine".
     */
    public static AUnitType getUnitTypeByName(String string) {
        string = string.replace(" ", "_").toLowerCase();

        // if (!string.startsWith("Terran") && !string.startsWith("Protoss") && !string.startsWith("Zerg")) {
        //
        // }
        for (Field field : AUnitType.class.getFields()) {
            String otherTypeName = field.getName().toLowerCase();
            if (!otherTypeName.startsWith("Hero") && otherTypeName.contains(string)) {
                try {
                    AUnitType unitType = (AUnitType) AUnitType.class.getField(field.getName()).get(null);
                    return unitType;
                } catch (Exception e) {
                    if (!disableErrorReporting) {
                        System.err.println("error trying to find AUnitType for: '" + string + "'\n" + e.getMessage());
                    }
                }
            }
        }

        return null;
    }

    /**
     * You can use "Terran_U-238_Shells" or "U-238_Shells" or even "U-238 Shells".
     */
    public static UpgradeType getUpgradeTypeByName(String string) {
        string = string.replace(" ", "_").toLowerCase();
        string = string.replace("-", "_").toLowerCase();

        for (Field field : UpgradeType.class.getFields()) {
            String otherTypeName = field.getName().toLowerCase();
            if (otherTypeName.contains(string)) {
                try {
                    UpgradeType upgradeType = (UpgradeType) UpgradeType.class.getField(field.getName()).get(null);
                    return upgradeType;
                } catch (Exception e) {
                    if (!disableErrorReporting) {
                        System.err.println("error trying to find AUnitType for: '" + string + "'\n" + e.getMessage());
                    }
                }
            }
        }

        return null;
    }

    public static TechType getTechTypeByName(String name) {
        name = name.replace(" ", "_").toLowerCase();

        for (Field field : TechType.class.getFields()) {
            String otherTechName = field.getName().toLowerCase();
            if (otherTechName.contains(name)) {
                try {
                    TechType techType = (TechType) TechType.class.getField(field.getName()).get(null);
                    return techType;
                } catch (Exception e) {
                    if (!disableErrorReporting) {
                        System.err.println("error trying to find TechType for: '" + name + "'\n" + e.getMessage());
                    }
                }
            }
        }

        return null;
    }
}
