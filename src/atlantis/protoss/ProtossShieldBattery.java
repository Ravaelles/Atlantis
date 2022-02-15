package atlantis.protoss;

import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.select.Select;

public class ProtossShieldBattery {

    private static final double MAX_DIST = 13;

    public static boolean update(AUnit shieldBattery) {
        if (shieldBattery.energy() >= 32) {
            shieldBattery.removeTooltip();
            for (AUnit unit : Select.ourRealUnits().inRadius(MAX_DIST, shieldBattery).list()) {
                if (unit.shieldDamageAtLeast(25) || (unit.isWorker() && unit.shieldDamageAtLeast(15))) {
//                    if (Select.enemyRealUnits().combatUnits().inRadius(7, unit).isNotEmpty()) {
//                        return false;
//                    }

//                    if (!shieldBattery.equals(unit.target()) || A.chance(2)) {
                    if (!shieldBattery.equals(unit.target())) {
                        unit.doRightClickAndYesIKnowIShouldAvoidUsingIt(shieldBattery);
                    }
                    shieldBattery.setTooltipTactical("RECHARGE " + unit.name());
                    unit.addLog("Recharge");
                    return true;
                }
            }
        }

        return false;
    }

}
