package atlantis.combat.micro.zerg;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class ZergBase {
    
    public static void creepOneHatcheryIntoLair() {
        AUnit hatchery = (AUnit) Select.ourBuildings().ofType(AUnitType.Zerg_Hatchery).first();
        if (hatchery == null) {
            System.err.println("No Hatchery found to morph into Lair");
        }
        else {
            hatchery.morph(AUnitType.Zerg_Lair);
        }
    }

    public static void creepOneLairIntoHive() {
        AUnit hatchery = (AUnit) Select.ourBuildings().ofType(AUnitType.Zerg_Lair).first();
        if (hatchery == null) {
            System.err.println("No Lair found to morph into Hive");
        }
        else {
            hatchery.morph(AUnitType.Zerg_Hive);
        }
    }
    
}
