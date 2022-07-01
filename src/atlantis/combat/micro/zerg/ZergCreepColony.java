package atlantis.combat.micro.zerg;

import atlantis.map.AChoke;
import atlantis.map.Chokes;
import atlantis.map.position.APosition;
import atlantis.production.constructing.Construction;
import atlantis.production.constructing.position.APositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;


public class ZergCreepColony {

    public static APosition findPosition(AUnitType building, AUnit builder, Construction construction) {
        AUnit secondBase = Select.naturalOrMain();
        if (secondBase != null) {
            APosition near = secondBase.position();

            AChoke mainChoke = Chokes.mainChoke();
            if (mainChoke != null) {
                near = near.translatePercentTowards(50, mainChoke);
            }

            return APositionFinder.findStandardPosition(builder, building, near, 10);
        }
        else {
            return null;
        }
    }
    
    // =========================================================

    public static void creepOneIntoSunkenColony() {
        AUnit creepColony = Select.ourBuildings().ofType(AUnitType.Zerg_Creep_Colony).first();
        if (creepColony != null) {
            creepColony.morph(AUnitType.Zerg_Sunken_Colony);
        }
    }
    
}
