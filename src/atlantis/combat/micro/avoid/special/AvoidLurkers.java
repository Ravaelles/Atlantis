package atlantis.combat.micro.avoid.special;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;

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

//        if (beBraveWithDetectorsNearby()) return null;

        if (unit.combatEvalRelative() < 1.7) return true;

        return (unit.woundPercent() >= 10 && unit.friendsNear().inRadius(4, unit).atMost(3));
    }

    @Override
    protected Manager handle() {

        // Defend buildings from lurkers
        if (lurker.enemiesNear().combatBuildingsAntiLand().inRadius(6.1, lurker).notEmpty()) return null;

        unit.runningManager().runFromAndNotifyOthersToMove(lurker, "LURKER!");
        return usedManager(this);
    }

    private boolean beBraveWithDetectorsNearby() {
//        if (unit.combatEvalRelative() < 1.7) return false;
        if (unit.friendsNear().detectors().inRadius(5, unit).empty()) return false;

        return true;
    }

    private double radius() {
        return 8.1
            + (unit.isMelee() ? 1.5 : 0)
            + unit.woundPercent() / 80.0;
    }
}
