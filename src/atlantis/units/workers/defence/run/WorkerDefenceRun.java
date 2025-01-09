package atlantis.units.workers.defence.run;

import atlantis.architecture.Manager;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.game.player.Enemy;

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
        if (runFromMassZealots()) return usedManager(this);
        else if (runFromReaver()) return usedManager(this);

        return null;
    }

    private boolean runFromMassZealots() {
        if (!Enemy.protoss()) return false;

        Selection zealots = unit.enemiesNear().zealots();
        if (zealots.countInRadius(3, unit) >= (unit.isWounded() ? 2 : 3)) {
            return runFromEnemyToAnotherRegion(unit, zealots.first());
        }

        return false;
    }

    private boolean runFromReaver() {
        AUnit reaver = unit.enemiesNear().ofType(AUnitType.Protoss_Reaver).nearestTo(unit);
        if (reaver != null) {
            double distTo = reaver.distTo(unit);

            if (distTo <= 11.4) {
                runFromEnemyToAnotherRegion(unit, reaver);
                unit.setTooltip("OhFuckReaver!", true);
                unit.addLog("OhFuckReaver!");
                return true;
            }

            if (distTo <= 12) {
                AUnit goTo = Select.minerals().inRadius(30, unit).mostDistantTo(unit);
                if (goTo != null && goTo.distTo(unit) >= 10) {
//                    unit.gather(goTo, Actions.MOVE_AVOID, "RunToAnotherBase");
                    unit.gather(goTo);
                    unit.setTooltip("OhShitReaver");
                    return true;
                }

                HasPosition runTo = Select.all().inRadius(60, unit).mostDistantTo(unit);
                if (runTo != null && runTo.distTo(unit) >= 10) {
                    unit.move(runTo, Actions.MOVE_AVOID, "RunToHell");
                    return true;
                }
            }
        }

        return false;
    }

    private boolean runFromEnemyToAnotherRegion(AUnit worker, AUnit enemy) {
        AUnit main = Select.mainOrAnyBuilding();

        if (main != null && main.distTo(worker) >= 16) {
            return worker.move(main, Actions.MOVE_AVOID, "RunFar");
        }

        int maxDist = 25;
        List<AUnit> anywhere = Select.all().inRadius(maxDist, worker).sortDataByGroundDistanceTo(enemy, false);
        for (HasPosition goTo : anywhere) {
            if (goTo.distTo(worker) <= (maxDist + 6) && goTo.position().hasPathTo(worker.position())) {
                return worker.move(goTo, Actions.MOVE_AVOID, "RunHellFar");
            }
        }

        return unit.runningManager().runFrom(enemy, 4, Actions.RUN_ENEMY, true);
    }
}
