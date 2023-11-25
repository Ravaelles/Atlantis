package atlantis.combat.advance.special;

import atlantis.architecture.Manager;
import atlantis.combat.missions.MissionManager;
import atlantis.game.A;
import atlantis.information.enemy.EnemyInfo;
import atlantis.map.AMap;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

public class ImprovePerformanceHavingBugSupply extends MissionManager {
    public ImprovePerformanceHavingBugSupply(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return A.supplyUsed(140)
            && unit.isMoving()
            && !unit.isRunning()
            && unit.lastActionLessThanAgo(20);
    }

    protected Manager handle(AUnit unit) {
        // Indicate that this manager was used - skip any more time-consuming calculations
        return usedManager(this);
    }
}
