package atlantis.combat.advance.contain.terran;

import atlantis.combat.advance.contain.DontAdvanceButHoldAndContainWhenEnemyBuildingsClose;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.select.Count;
import atlantis.util.We;

public class AppliesContainForTerran {
    private final DontAdvanceButHoldAndContainWhenEnemyBuildingsClose manager;
    private final AUnit unit;

    public AppliesContainForTerran(DontAdvanceButHoldAndContainWhenEnemyBuildingsClose manager) {
        this.manager = manager;
        this.unit = manager.unit();
    }

    public boolean applies() {
        if (!We.terran()) return false;

        manager.tanks = Count.tanks();

        return Count.ourCombatUnits() <= 25
            && (unit.isWounded() || (A.supplyUsed() < 185 && manager.tanks <= 18))
            && A.minerals() < 2000
            && unit.isCombatUnit()
            && safeWithTanksSoSkip()
            && manager.closeToEnemyBuildingsOrChoke();
    }

    protected boolean safeWithTanksSoSkip() {
        if (unit.isTank()) return false;

        return unit.nearestOurTankDist() >= 3 && unit.friendsNear().inRadius(3, unit).atLeast(7);
    }
}
