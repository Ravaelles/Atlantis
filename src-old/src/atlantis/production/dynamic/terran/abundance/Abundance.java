package atlantis.production.dynamic.terran.abundance;

import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;

public class Abundance {
    public static boolean ifNotNullProduce(AUnit building, AUnitType unitToProduce) {
        if (building == null || building.isBusy()) return false;
        if (!A.canAfford(unitToProduce)) return false;

        return building.trainForced(unitToProduce);
    }

    public static AUnit freeBarracks() {
        return Select.ourOfType(AUnitType.Terran_Barracks).free().random();
    }

    public static AUnit freeFactory() {
        return Select.ourOfType(AUnitType.Terran_Factory).free().random();
    }

    public static AUnit freeFactoryWithMachineShop() {
        return Select.ourOfType(AUnitType.Terran_Factory).withAddon().free().random();
    }

    public static AUnit freeStarport() {
        return Select.ourOfType(AUnitType.Terran_Starport).free().random();
    }
}
