package atlantis.combat.micro.avoid.zerg;

import atlantis.units.AUnit;
import atlantis.architecture.Manager;

public class ShouldAlwaysAvoidAsZerg extends Manager {

    private int friendsVeryNear;
    private int meleeEnemiesVeryNear;

    public ShouldAlwaysAvoidAsZerg(AUnit unit) {
        super(unit);
    }

    public boolean shouldAlwaysAvoid() {
        if (shouldSkip()) {
            return false;
        }

        friendsVeryNear = unit.friendsInRadiusCount(2);
        meleeEnemiesVeryNear = unit.meleeEnemiesNearCount(3);

        if (asZergling()) {
            return true;
        }

        return false;
    }

    private boolean asZergling() {
        if (!unit.isZergling()) {
            return false;
        }

        return unit.hp() <= 22 && friendsVeryNear <= 65 && meleeEnemiesVeryNear > 0;
    }

    private boolean shouldSkip() {
        return !unit.isZerg();
    }
}
