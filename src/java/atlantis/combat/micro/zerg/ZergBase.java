package atlantis.combat.micro.zerg;

import atlantis.wrappers.SelectUnits;
import jnibwapi.Unit;
import jnibwapi.types.UnitType;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class ZergBase {
    
    public static void creepOneHatcheryIntoLair() {
        Unit hatchery = SelectUnits.ourBuildings().ofType(UnitType.UnitTypes.Zerg_Hatchery).first();
        if (hatchery == null) {
            System.err.println("No Hatchery found to morph into Lair");
        }
        else {
            hatchery.morph(UnitType.UnitTypes.Zerg_Lair);
        }
    }

    public static void creepOneLairIntoHive() {
        Unit hatchery = SelectUnits.ourBuildings().ofType(UnitType.UnitTypes.Zerg_Lair).first();
        if (hatchery == null) {
            System.err.println("No Lair found to morph into Hive");
        }
        else {
            hatchery.morph(UnitType.UnitTypes.Zerg_Hive);
        }
    }
    
}
