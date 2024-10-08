package atlantis.combat.micro.avoid.zerg;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;

public class ShouldAlwaysAvoidAsZerg extends Manager {
    private int friendsVeryNear;
    private int meleeEnemiesVeryNear;

    public ShouldAlwaysAvoidAsZerg(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isZerg();
    }

    public boolean shouldAlwaysAvoid() {
        if (shouldSkip()) return false;

        friendsVeryNear = unit.friendsInRadiusCount(2);
        meleeEnemiesVeryNear = unit.meleeEnemiesNearCount(2.8);

        if (asZergling()) return true;
        if (asHydra()) return true;

        return false;
    }

    private boolean asHydra() {
        if (!unit.isHydralisk()) return false;

        return meleeEnemiesVeryNear > 0 && friendsVeryNear <= 4;
    }

    private boolean asZergling() {
        if (!unit.isZergling()) return false;

        return unit.hp() <= 22 && friendsVeryNear <= 4 && meleeEnemiesVeryNear > 0;
    }

    private boolean shouldSkip() {
        return !unit.isZerg();
    }
}
