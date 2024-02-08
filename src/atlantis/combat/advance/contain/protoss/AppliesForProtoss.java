package atlantis.combat.advance.contain.protoss;

import atlantis.combat.advance.contain.DontAdvanceButHoldAndContainWhenEnemyBuildingsClose;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.util.We;

public class AppliesForProtoss {
    private final DontAdvanceButHoldAndContainWhenEnemyBuildingsClose manager;
    private final AUnit unit;

    public AppliesForProtoss(DontAdvanceButHoldAndContainWhenEnemyBuildingsClose manager) {
        this.manager = manager;
        this.unit = this.manager.unit();
    }

    public boolean applies() {
        if (!We.protoss()) return false;
        if (weAreTooStrongToJustContain()) return false;

        return manager.closeToEnemyBuildingsOrChoke()
            && (noPlentyOfFriendsNearby() || tooManyDefences());
    }

    protected boolean tooManyDefences() {
        boolean isNotVeryStrong = A.supplyUsed() <= 185 && unit.friendsNear().atMost(33);

        return isNotVeryStrong && unit.enemiesNear().combatBuildingsAntiLand().notEmpty();
    }

    protected boolean noPlentyOfFriendsNearby() {
        return unit.friendsNear().inRadius(8, unit).atMost(10);
    }

    protected static boolean weAreTooStrongToJustContain() {
        return Alpha.get().size() >= 40
            || A.supplyUsed() > 180
            || A.minerals() > 2000
            || A.resourcesBalance() >= 1300;
    }
}