package atlantis.production.constructing.position;

import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;

public class TerranAddonBuilder {
    public static void buildNewAddon(AUnitType addon, ProductionOrder order) {
        AUnit parentBuilding = Select.ourBuildings().ofType(addon.whatBuildsIt()).free().first();
        if (parentBuilding != null) {
            parentBuilding.buildAddon(addon);
        }
    }

}
