package atlantis.combat.advance.contain.protoss;

import atlantis.combat.advance.contain.DontAdvanceButHoldAndContainWhenEnemyBuildingsClose;
import atlantis.combat.squad.alpha.Alpha;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.util.We;

public class AppliesContainForProtoss {
    private final DontAdvanceButHoldAndContainWhenEnemyBuildingsClose manager;
    private final AUnit unit;

    public AppliesContainForProtoss(DontAdvanceButHoldAndContainWhenEnemyBuildingsClose manager) {
        this.manager = manager;
        this.unit = this.manager.unit();
    }

    public boolean applies() {
        if (!We.protoss()) return false;
        if (weAreTooStrongToJustContain()) return false;

        return manager.closeToEnemyBuildingsOrChoke()
            && (noPlentyOfFriendsNearby() || enemyHasDefences());
    }

    protected boolean enemyHasDefences() {
        boolean isNotVeryStrong = A.supplyUsed() <= 185 && unit.friendsNear().atMost(25);

        return isNotVeryStrong && unit.enemiesNear().combatBuildingsAntiLand().notEmpty();
    }

    protected boolean noPlentyOfFriendsNearby() {
        return unit.friendsNear().inRadius(8, unit).atMost(10);
    }

    protected boolean weAreTooStrongToJustContain() {
        if (A.supplyUsed() > 185) return true;
        if (A.minerals() > 2500) return true;

//        if (unit.friendsNear().combatUnits().inRadius(10, unit).atMost(8)) return false;

        if (Alpha.get().size() >= 40) return true;

        if (unit.friendsNear().combatUnits().atMost(18)) return false;

        return A.resourcesBalance() >= 1300;
    }
}
