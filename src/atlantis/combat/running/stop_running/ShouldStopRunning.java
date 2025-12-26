package atlantis.combat.running.stop_running;

import atlantis.architecture.Manager;
import atlantis.combat.running.stop_running.protoss.ProtossShouldStopRunning;
import atlantis.combat.running.stop_running.protoss.TerranShouldStopRunning;
import atlantis.combat.running.stop_running.zerg.ZergShouldStopRunning;
import atlantis.units.AUnit;

public class ShouldStopRunning extends Manager {
    public ShouldStopRunning(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isRunning()
            && allowStoppingRunningWorker()
            && (unit.eval() >= 2 || unit.enemiesThatCanAttackMe(unit.isRetreating() ? 10 : 3).count() == 0);
//        return unit.enemiesThatCanAttackMe(1).count() == 0;
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
//            TerranShouldStopRunning.class,
            ZergShouldStopRunning.class,
//            RemoveStoppedRunning.class,
        };
    }

    private boolean allowStoppingRunningWorker() {
        if (!unit.isWorker()) return true;

        if (unit.isScout() && unit.enemiesThatCanAttackMe(10).empty()) return false;

        return unit.enemiesThatCanAttackMe(2.5 + unit.woundPercent() / 50.0).isEmpty();
    }
}
