package atlantis.units.workers.defence.run;

import atlantis.architecture.Manager;
import atlantis.game.A;
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
        if (A.s <= 60 * 7 && unit.hp() >= 38) return false;

        if (unit.hp() <= 20 && unit.enemiesNear().notEmpty()) return true;

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
        if (runFromZealots()) return usedManager(this);
        if (runFromDragoons()) return usedManager(this);
        if (runFromMassLings()) return usedManager(this);
        else if (runFromReaver()) return usedManager(this);
        else if (runFromMutas()) return usedManager(this);

        return null;
    }

    private boolean runFromMutas() {
        if (!Enemy.zerg()) return false;

        Selection mutas = unit.enemiesNear().mutalisks();
        if (mutas.countInRadius(7, unit) >= 2) {
            return runFromEnemyToAnotherRegion(unit, mutas.first());
        }

        return false;
    }

    private boolean runFromMassLings() {
        if (!Enemy.zerg()) return false;
        if (unit.hp() >= 39) return false;

        Selection lings = unit.enemiesNear().zerglings();
        if (lings.countInRadius(3, unit) >= (unit.isWounded() ? 1 : 3)) {
            return runFromEnemyToAnotherRegion(unit, lings.first());
        }

        if (unit.isHealthy() && lings.nearestToDist(unit) >= 2.5) {
            return false;
        }

        if (unit.runOrMoveAway(lings.nearestTo(unit), 5)) {
            return true;
        }

        return false;
    }


    private boolean runFromDragoons() {
        if (!Enemy.protoss()) return false;

        Selection dragoons = unit.enemiesNear().zealots();
        int dragoonsNear = dragoons.countInRadius(3.1, unit);

        if (dragoonsNear == 0) return false;
        if (dragoonsNear == 1 && unit.hp() <= 38 && unit.runOrMoveAway(dragoons.first(), 4)) return false;

        return false;
    }

    private boolean runFromZealots() {
        if (!Enemy.protoss()) return false;

        Selection zealots = unit.enemiesNear().zealots();
        int zealotsNear = zealots.countInRadius(3.0, unit);

        if (zealotsNear == 0) return false;
        if (zealotsNear == 1 && unit.hp() >= 36) return false;
        if (zealotsNear == 2 && unit.hp() >= 38) return false;
        if (zealotsNear >= 3 && unit.hp() >= 37 && unit.combatFriendsInRadiusCount(4) >= 1)  {
            return false;
        }

        if (zealotsNear >= (unit.isWounded() ? 2 : 3)) {
            return runFromEnemyToAnotherRegion(unit, zealots.first());
        }

        if (zealotsNear >= (unit.isWounded() ? 0 : 1)) {
            AUnit zealot = zealots.nearestTo(unit);
            if (zealot != null) {
                if (zealot.distTo(unit) <= 3 && unit.runOrMoveAway(zealot, 5)) return true;
                if (unit.moveAwayFrom(zealot, 2, Actions.MOVE_AVOID, "RunFromZealot")) return true;
            }
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
                if (runTo != null && runTo.isWalkable() && runTo.distTo(unit) >= 2) {
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
