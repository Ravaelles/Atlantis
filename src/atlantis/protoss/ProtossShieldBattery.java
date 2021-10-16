package atlantis.protoss;

import atlantis.AGame;
import atlantis.units.AUnit;
import atlantis.units.Select;
import bwapi.TechType;

public class ProtossShieldBattery {

    public static boolean handle(AUnit shieldBattery) {
        if (shieldBattery.getEnergy() >= 20) {
            shieldBattery.removeTooltip();
            for (AUnit unit : Select.ourRealUnits().inRadius(8, shieldBattery).listUnits()) {
                if (unit.getShields() + 13 < unit.getMaxShields() ) {
                    if (Select.enemyRealUnits().combatUnits().inRadius(5, unit).count() > 0) {
                        return false;
                    }

                    if (!shieldBattery.equals(unit.getTarget())) {
                        System.out.println("Recharge!");
                        unit.doRightClickAndYesIKnowIShouldAvoidUsingIt(shieldBattery);
                    }
                    shieldBattery.setTooltip("RECHARGE " + unit.getShortName());
//                    return true;
                }
            }
        }

        return false;
    }

}
