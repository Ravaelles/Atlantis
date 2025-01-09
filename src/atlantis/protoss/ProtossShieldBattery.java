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
    protected Manager handle() {
        AUnit shieldBattery = unit;

        if (shieldBattery.energy() >= 40 && shieldBattery.isPowered()) {
            shieldBattery.removeTooltip();
            for (AUnit friend : unit.friendsNear().nonBuildings().inRadius(MAX_DIST, shieldBattery).list()) {
                if (friend.shieldDamageAtLeast(25) || (friend.isWorker() && friend.shieldDamageAtLeast(10))) {
//                    if (Select.enemyRealUnits().combatUnits().inRadius(7, friend).isNotEmpty()) {
//                        return false;
//                    }

                    if (friend.enemiesNear().inRadius(3, friend).notEmpty()) {
                        continue;
                    }

//                    if (!shieldBattery.equals(friend.target()) || A.chance(2)) {
                    if (!shieldBattery.equals(friend.target())) {
                        friend.doRightClickAndYesIKnowIShouldAvoidUsingIt(shieldBattery);
                    }

                    String t = "Recharge";
                    shieldBattery.setTooltipTactical(t + ":" + friend.name());
                    friend.addLog(t);
                    return usedManager(this);
                }
            }
        }

        return null;
    }

}
