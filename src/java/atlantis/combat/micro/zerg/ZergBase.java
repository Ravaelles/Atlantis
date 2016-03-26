package atlantis.combat.micro.zerg;

import atlantis.wrappers.Select;
import bwapi.Unit;
import bwapi.UnitType;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class ZergBase {
    
    public static void creepOneHatcheryIntoLair() {
        Unit hatchery = (Unit) Select.ourBuildings().ofType(UnitType.Zerg_Hatchery).first();
        if (hatchery == null) {
            System.err.println("No Hatchery found to morph into Lair");
        }
        else {
            hatchery.morph(UnitType.Zerg_Lair);
        }
    }

    public static void creepOneLairIntoHive() {
        Unit hatchery = (Unit) Select.ourBuildings().ofType(UnitType.Zerg_Lair).first();
        if (hatchery == null) {
            System.err.println("No Lair found to morph into Hive");
        }
        else {
            hatchery.morph(UnitType.Zerg_Hive);
        }
    }
    
}
