package atlantis.information.generic;

import atlantis.units.AUnitType;
import atlantis.units.select.Count;

public class ProtossArmyComposition {

    public static boolean zealotsToDragoonsRatioTooLow() {
        int zealots = Count.ofType(AUnitType.Protoss_Zealot);
        int dragoons = Count.ofType(AUnitType.Protoss_Dragoon);
        return ((double) zealots / (zealots + dragoons)) <= 0.45;
    }

}
