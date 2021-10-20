package atlantis.protoss;

import atlantis.units.AUnit;
import atlantis.units.Select;

public class ProtossShieldBattery {

    public static boolean handle(AUnit shieldBattery) {
        if (shieldBattery.getEnergy() >= 20) {
            System.out.println(shieldBattery);
            shieldBattery.removeTooltip();
            for (AUnit unit : Select.ourRealUnits().inRadius(10, shieldBattery).listUnits()) {
                if (unit.getShields() + 13 < unit.getMaxShields() ) {
                    if (Select.enemyRealUnits().combatUnits().inRadius(5, unit).count() > 0) {
                        return false;
                    }

//                    if (!shieldBattery.equals(unit.getTarget())) {
                        System.out.println("Recharge!");
                        unit.doRightClickAndYesIKnowIShouldAvoidUsingIt(shieldBattery);
//                    }
                    shieldBattery.setTooltip("RECHARGE " + unit.shortName());
//                    return true;
                }
            }
        }

        return false;
    }

}
