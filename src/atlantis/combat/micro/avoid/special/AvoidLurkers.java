package atlantis.combat.micro.avoid.special;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;

public class AvoidLurkers extends Manager {
    public AvoidLurkers(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isGroundUnit();
    }

    @Override
    protected Manager handle() {
        if (unit.isAir() || unit.isABuilding()) return null;

        AUnit lurker = unit.enemiesNear().lurkers().effUndetected().inRadius(radius(), unit).nearestTo(unit);
        if (lurker == null) return null;

        // Defend buildings from lurkers
        if (lurker.enemiesNear().combatBuildingsAntiLand().inRadius(6.1, lurker).notEmpty()) return null;

        unit.runningManager().runFromAndNotifyOthersToMove(lurker, "LURKER!");
        return usedManager(this);
    }

    private double radius() {
        return 8.1 + unit.woundPercent() / 80.0;
    }
}
