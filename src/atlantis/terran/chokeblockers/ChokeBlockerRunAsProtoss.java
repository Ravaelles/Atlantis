package atlantis.terran.chokeblockers;

import atlantis.architecture.Manager;
import atlantis.information.generic.OurArmy;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.util.Enemy;
import atlantis.util.We;

public class ChokeBlockerRunAsProtoss extends Manager {
    public ChokeBlockerRunAsProtoss(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return We.protoss()
            && Enemy.zerg()
            && OurArmy.strength() <= 70
            && (unit.woundPercent() >= 10 || unit.meleeEnemiesNearCount(1.2) >= 2)
            && unit.lastUnderAttackLessThanAgo(30 * 5)
//            && unit.lastAttackFrameLessThanAgo(30 * 4)
//            && unit.friendsNear().combatUnits().countInRadius(3, unit) <= 3
            && unit.distToMain() >= 2;
    }

    @Override
    public Manager handle() {
        if (unit.moveToSafety(Actions.RUN_ENEMY, "BlockerToMain")) return usedManager(this);

        return null;
    }
}
