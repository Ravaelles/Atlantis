package atlantis.combat.advance.special;

import atlantis.architecture.Manager;
import atlantis.combat.missions.MissionManager;
import atlantis.combat.missions.Missions;
import atlantis.debug.profiler.LongFrames;
import atlantis.game.A;
import atlantis.information.enemy.EnemyUnits;
import atlantis.units.AUnit;

public class FixPerformanceForBigSupply extends MissionManager {
    public FixPerformanceForBigSupply(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return A.supplyUsed(150)
            && Missions.isGlobalMissionAttack()
            && unit.isMoving()
//            && !unit.isRunning()
//            && unit.lastActionLessThanAgo(20)
            && unit.enemiesNear().empty()
            && A.everyFrameExceptNthFrame(17);
    }

    protected Manager handle(AUnit unit) {
        // Indicate that this manager was used - skip any more time-consuming calculations
        return usedManager(this);
    }
}
