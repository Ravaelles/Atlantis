package atlantis.protoss;

import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.select.Select;

public class ProtossShieldBattery {

//    private  final double MAX_DIST = 3.98;
    private  final double MAX_DIST = 7.98;

    public  boolean update(AUnit shieldBattery) {
        if (shieldBattery.energy() >= 40 && shieldBattery.isPowered()) {
            shieldBattery.removeTooltip();
            for (AUnit unit : Select.ourRealUnits().inRadius(MAX_DIST, shieldBattery).list()) {
                if (unit.shieldDamageAtLeast(25) || (unit.isWorker() && unit.shieldDamageAtLeast(10))) {
//                    if (Select.enemyRealUnits().combatUnits().inRadius(7, unit).isNotEmpty()) {
//                        return false;
//                    }

                    if (unit.enemiesNear().inRadius(3, unit).notEmpty()) {
                        continue;
                    }

//                    if (!shieldBattery.equals(unit.target()) || A.chance(2)) {
                    if (!shieldBattery.equals(unit.target())) {
                        unit.doRightClickAndYesIKnowIShouldAvoidUsingIt(shieldBattery);
                    }

                    String t = "Recharge";
                    shieldBattery.setTooltipTactical(t + ":" + unit.name());
                    unit.addLog(t);
                    return true;
                }
            }
        }

        return false;
    }

}
