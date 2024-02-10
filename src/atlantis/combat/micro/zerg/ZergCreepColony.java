package atlantis.combat.micro.zerg;

import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.production.constructing.Construction;
import atlantis.production.constructing.position.APositionFinder;
import atlantis.production.orders.production.queue.order.ProductionOrder;
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

    public static void creepOneIntoSunkenColony(ProductionOrder order) {
        AUnit creepColony = Select.ourBuildings().ofType(AUnitType.Zerg_Creep_Colony).first();
        if (creepColony != null) {
            creepColony.morph(AUnitType.Zerg_Sunken_Colony, order);
        }
    }

}
