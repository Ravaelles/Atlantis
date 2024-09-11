package atlantis.production.dynamic.terran.abundance;

import atlantis.game.A;
import atlantis.units.AUnitType;
import atlantis.units.select.Have;

import static atlantis.units.AUnitType.Terran_Vulture;
import static atlantis.units.AUnitType.Terran_Wraith;

public class AbundanceProduce {
    protected static boolean produceMarine() {
        return Abundance.ifNotNullProduce(Abundance.freeBarracks(), AUnitType.Terran_Marine);
    }

    protected static boolean produceTank() {
        return A.hasGas(320) &&
            Abundance.ifNotNullProduce(
                Abundance.freeFactoryWithMachineShop(), AUnitType.Terran_Siege_Tank_Tank_Mode
            );
    }

    protected static boolean produceWraith() {
        return (
            A.hasGas(550) || (A.hasGas(350) && Have.scienceVessel())
        )
            && Abundance.ifNotNullProduce(Abundance.freeStarport(), Terran_Wraith);
    }

    protected static boolean produceVulture() {
        return Abundance.ifNotNullProduce(Abundance.freeFactory(), Terran_Vulture);
    }
}
