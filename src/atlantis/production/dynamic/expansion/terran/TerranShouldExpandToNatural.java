package atlantis.production.dynamic.expansion.terran;

import atlantis.config.AtlantisRaceConfig;
import atlantis.game.A;
import atlantis.information.generic.ArmyStrength;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;

public class TerranShouldExpandToNatural {
    public static boolean shouldExpandToNatural() {
        if (Count.existingOrInProductionOrInQueue(AtlantisRaceConfig.BASE) > 1) return false;

        if (
            ArmyStrength.weAreStronger()
                && (
                A.canAffordWithReserved(350, 0)
                    || Count.withPlanned(AUnitType.Terran_Bunker) == 0
            )
        ) return true;

        return false;
    }
}
