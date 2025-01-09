package atlantis.combat.micro.protoss;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;

//public class ProtossShieldBattery extends Manager {
//
//    public ProtossShieldBattery(AUnit unit) {
//        super(unit);
//    }
//
//    @Override
//    public boolean applies() {
//        return unit.is(AUnitType.Protoss_Shield_Battery) && unit.isPowered();
//    }
//
//    protected Manager handle() {
//        if (moveHealableUnitsToBattery()) return usedManager(this);
//
//        return null;
//    }
//
//    private boolean moveHealableUnitsToBattery() {
//        AUnit friend = unit.friendsNear().ofType(AUnitType.Protoss_Shield_Battery)
//            .havingEnergy(40)
//            .havingSeriousShieldWound()
//            .nearestTo(unit);
//
//        if (
//            friend != null && friend.distTo(unit) <= (unit.hp() <= 60 ? 12 : 8)
//                && friend.move(unit, Actions.SPECIAL, "ToBattery", false)
//        ) {
//            if (!unit.equals(friend)) {
//                unit.doRightClickAndYesIKnowIShouldAvoidUsingIt(unit);
//            }
//
//            String t = "Recharge";
//            unit.setTooltipTactical(t + ":" + unit.name());
//            unit.addLog(t);
//            friend.addLog(t);
//
//            return true;
//        }
//        return false;
//    }
//}
