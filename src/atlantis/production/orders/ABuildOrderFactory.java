
package atlantis.production.orders;

import atlantis.AGame;
import atlantis.AtlantisConfig;
import atlantis.production.ProductionOrder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import atlantis.util.Us;

import java.util.ArrayList;

/**
 * Represents sequence of commands to produce units/buildings.
 */
public abstract class ABuildOrderFactory {

    public static ABuildOrder forRace(String race, String name, ArrayList<ProductionOrder> productionOrders) {
        if ("Protoss".equals(race)) {
            return new ProtossBuildOrder(name, productionOrders);
        } else if ("Terran".equals(race)) {
            return new TerranBuildOrder(name, productionOrders);
        } else if ("Zerg".equals(race)) {
            return new ZergBuildOrder(name, productionOrders);
        } else {
            throw new RuntimeException("Invalid race: " + race);
        }
    }

}
