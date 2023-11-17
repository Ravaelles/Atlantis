package atlantis.production.dynamic.terran.abundance;

import atlantis.units.AUnitType;

import static atlantis.units.AUnitType.Terran_Vulture;
import static atlantis.units.AUnitType.Terran_Wraith;

public class AbundanceProduce {
    protected static boolean produceMarine() {
        return Abundance.ifNotNullProduce(Abundance.freeBarracks(), AUnitType.Terran_Marine);
    }

    protected static boolean produceTank() {
        return Abundance.ifNotNullProduce(Abundance.freeFactoryWithMachineShop(), AUnitType.Terran_Siege_Tank_Tank_Mode);
    }

    protected static boolean produceWraith() {
        return Abundance.ifNotNullProduce(Abundance.freeStarport(), Terran_Wraith);
    }

    protected static boolean produceVulture() {
        return Abundance.ifNotNullProduce(Abundance.freeFactory(), Terran_Vulture);
    }
}
