package atlantis.terran.repair;

import atlantis.information.strategy.GamePhase;
import atlantis.terran.FlyingBuildingScoutCommander;
import atlantis.terran.repair.repairer.RepairerManager;
import atlantis.units.AUnit;

public class ShouldNotRepairUnit {
    public static boolean shouldNotRepairUnit(AUnit unit, AUnit target) {
        if (target == null) return false;

        return !target.isRepairable()
            || (target.isAir() && target.hp() >= 91 && target.friendsNear().workers().notRepairing().empty())
            || target.isScout()
            || target.isFlyingScout()
            || (target.isRunning() && target.lastStoppedRunningLessThanAgo(30 * 2))
            || (
            target.isABuilding()
                && FlyingBuildingScoutCommander.isFlyingBuilding(target)
                && target.lastUnderAttackLessThanAgo(30 * 6)
        )
//                || (target.isBuilding() && !target.isCombatBuilding() && !target.woundPercentMin(40))
            || RepairerManager.itIsForbiddenToRepairThisUnitNow(unit, target)
            || GamePhase.isEarlyGame() && (
            target.isABuilding() && !target.isCombatBuilding() && target.enemiesNear().atLeast(2)
        );
    }
}
