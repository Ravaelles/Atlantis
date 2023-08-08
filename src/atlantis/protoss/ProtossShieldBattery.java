package atlantis.protoss;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;

public class ProtossShieldBattery extends Manager {
    private final double MAX_DIST = 7.98;

    public ProtossShieldBattery(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.is(AUnitType.Protoss_Shield_Battery);
    }

    @Override
    public Manager handle() {
        AUnit shieldBattery = unit;

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
                    return usedManager(this);
                }
            }
        }

        return null;
    }

}
