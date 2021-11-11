package atlantis.dedicated.protoss;

import atlantis.combat.micro.generic.MobileDetector;
import atlantis.combat.squad.Squad;
import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.units.actions.UnitActions;

public class ProtossObserver extends MobileDetector {

    public static AUnitType type() {
        return AUnitType.Protoss_Observer;
    }

    // =========================================================

    public static boolean update(AUnit observer) {
        return MobileDetector.update(observer);
    }

}
