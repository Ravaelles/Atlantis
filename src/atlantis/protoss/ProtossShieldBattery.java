package atlantis.protoss;

import atlantis.units.AUnit;
import atlantis.units.Select;
import atlantis.util.A;

public class ProtossShieldBattery {

    public static boolean update(AUnit shieldBattery) {
        if (shieldBattery.energy() >= 30) {
            shieldBattery.removeTooltip();
            for (AUnit unit : Select.ourRealUnits().inRadius(11, shieldBattery).listUnits()) {
                if (unit.getShields() + 12 < unit.getMaxShields() ) {
                    if (Select.enemyRealUnits().combatUnits().inRadius(7, unit).isNotEmpty()) {
                        return false;
                    }

                    if (shieldBattery.groundDistance(unit) > 15) {
                        return false;
                    }
                    if (!shieldBattery.equals(unit.getTarget()) || A.chance(2)) {
                        unit.doRightClickAndYesIKnowIShouldAvoidUsingIt(shieldBattery);
                    }
                    shieldBattery.setTooltip("RECHARGE " + unit.shortName());
                    return true;
                }
            }
        }

        return false;
    }

}
