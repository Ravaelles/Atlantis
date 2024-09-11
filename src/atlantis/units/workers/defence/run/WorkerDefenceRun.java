package atlantis.units.workers.defence.run;

import atlantis.architecture.Manager;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;

import java.util.List;

public class WorkerDefenceRun extends Manager {
    public WorkerDefenceRun(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return !ignoreWhenOnlyAirUnits();
    }

    private boolean ignoreWhenOnlyAirUnits() {
        return unit.hp() >= 34
            && unit.enemiesNear().groundUnits().havingWeapon().empty()
            && unit.enemiesNear().air().ofType(
            AUnitType.Protoss_Scout, AUnitType.Protoss_Arbiter,
            AUnitType.Terran_Wraith,
            AUnitType.Zerg_Overlord, AUnitType.Zerg_Queen
        ).atMost(2);
    }

    @Override
    public Manager handle() {
        if (runFromReaver(unit)) return usedManager(this);

        return null;
    }

    private boolean runFromReaver(AUnit worker) {
        AUnit reaver = worker.enemiesNear().ofType(AUnitType.Protoss_Reaver).nearestTo(worker);
        if (reaver != null) {
            double distTo = reaver.distTo(worker);

            if (distTo <= 11.4) {
                runFromEnemyToAnotherRegion(worker, reaver);
                worker.setTooltip("OhFuckReaver!", true);
                worker.addLog("OhFuckReaver!");
                return true;
            }

            if (distTo <= 12) {
                AUnit goTo = Select.minerals().inRadius(30, worker).mostDistantTo(worker);
                if (goTo != null && goTo.distTo(worker) >= 10) {
//                    worker.gather(goTo, Actions.MOVE_AVOID, "RunToAnotherBase");
                    worker.gather(goTo);
                    worker.setTooltip("OhShitReaver");
                    return true;
                }

                HasPosition runTo = Select.all().inRadius(60, worker).mostDistantTo(worker);
                if (runTo != null && runTo.distTo(worker) >= 10) {
                    worker.move(runTo, Actions.MOVE_AVOID, "RunToHell");
                    return true;
                }
            }
        }

        return false;
    }

    private void runFromEnemyToAnotherRegion(AUnit worker, AUnit reaver) {
//        worker.runningManager().runFrom(reaver, 10, Actions.RUN_ENEMY, true);

        AUnit main = Select.mainOrAnyBuilding();

        if (main != null && main.distTo(worker) >= 16) {
            worker.move(main, Actions.MOVE_AVOID, "RunFarFromReaver");
            return;
        }

        int maxDist = 25;
        List<AUnit> anywhere = Select.all().inRadius(maxDist, worker).sortDataByGroundDistanceTo(reaver, false);
        for (HasPosition goTo : anywhere) {
            if (goTo.distTo(worker) <= (maxDist + 6) && goTo.position().hasPathTo(worker.position())) {
                worker.move(goTo, Actions.MOVE_AVOID, "RunFarFromReaver");
                return;
            }
        }
    }
}
