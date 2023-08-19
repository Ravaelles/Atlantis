package atlantis.terran.repair;

import atlantis.information.strategy.GamePhase;
import atlantis.terran.TerranFlyingBuildingScoutCommander;
import atlantis.units.AUnit;

public class ShouldNotRepairUnit {
    public static boolean shouldNotRepairUnit(AUnit unit) {
        if (unit == null) return false;

        return !unit.isRepairable()
            || (unit.isAir() && unit.hp() >= 91 && unit.friendsNear().workers().notRepairing().empty())
            || unit.isScout()
            || unit.isFlyingScout()
            || (unit.isRunning() && unit.lastStoppedRunningLessThanAgo(30 * 2))
            || (
            unit.isABuilding()
                && TerranFlyingBuildingScoutCommander.isFlyingBuilding(unit)
                && unit.lastUnderAttackLessThanAgo(30 * 6)
        )
//                || (unit.isBuilding() && !unit.isCombatBuilding() && !unit.woundPercentMin(40))
            || RepairerManager.itIsForbiddenToRepairThisUnitNow(unit)
            || GamePhase.isEarlyGame() && (unit.isABuilding() && !unit.isCombatBuilding() && unit.enemiesNear().atLeast(2));
    }
}
