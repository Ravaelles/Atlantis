package atlantis.combat.micro.avoid.special;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.util.We;

public class AvoidLurkers extends Manager {

    private AUnit lurker;

    public AvoidLurkers(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isGroundUnit() || unit.isABuilding()) return false;

        lurker = unit.enemiesNear().lurkers().effUndetected().inRadius(radius(), unit).nearestTo(unit);
        if (lurker == null) return false;

        if (unit.combatEvalRelative() < 1.5) return true;

//        if (beBraveWithDetectorsNearby()) return false;

        return unit.woundPercent() >= 30
            && unit.friendsNear().inRadius(4, unit).atMost(dontEngageWhenAtMostFriendsNearby());
    }

    private static int dontEngageWhenAtMostFriendsNearby() {
        if (We.protoss()) return 3;

        return 4;
    }

    @Override
    protected Manager handle() {

        // Defend buildings from lurkers
        if (lurker.enemiesNear().combatBuildingsAntiLand().inRadius(6.1, lurker).notEmpty()) return null;

        unit.runningManager().runFromAndNotifyOthersToMove(lurker, "LURKER!");
        return usedManager(this);
    }

//    private boolean beBraveWithDetectorsNearby() {
////        if (unit.combatEvalRelative() < 1.7) return false;
//        if (lurker.enemiesNear().detectors().inRadius(8, unit).empty()) return false;
//
//        return true;
//    }

    private double radius() {
        return 8.1
            + (unit.isMelee() ? 1.8 : 0)
            + unit.woundPercent() / 80.0;
    }
}
