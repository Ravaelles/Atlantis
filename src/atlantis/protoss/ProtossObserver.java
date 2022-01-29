package atlantis.protoss;

import atlantis.combat.micro.generic.MobileDetector;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;

public class ProtossObserver extends MobileDetector {

    public static AUnitType type() {
        return AUnitType.Protoss_Observer;
    }

    // =========================================================

    public static boolean update(AUnit observer) {
        return MobileDetector.update(observer);
    }

}
