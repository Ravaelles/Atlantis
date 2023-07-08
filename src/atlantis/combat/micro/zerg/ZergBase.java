package atlantis.combat.micro.zerg;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;


public class ZergBase {
    
    public  void creepOneHatcheryIntoLair() {
        AUnit hatchery = Select.ourBuildings().ofType(AUnitType.Zerg_Hatchery).first();
        if (hatchery == null) {
            System.err.println("No Hatchery found to morph into Lair");
        }
        else {
            hatchery.morph(AUnitType.Zerg_Lair);
        }
    }

    public  void creepOneLairIntoHive() {
        AUnit hatchery = Select.ourBuildings().ofType(AUnitType.Zerg_Lair).first();
        if (hatchery == null) {
            System.err.println("No Lair found to morph into Hive");
        }
        else {
            hatchery.morph(AUnitType.Zerg_Hive);
        }
    }
    
}
