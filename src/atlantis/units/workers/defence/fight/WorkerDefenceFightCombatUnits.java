package atlantis.units.workers.defence.fight;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;

import java.util.List;

public class WorkerDefenceFightCombatUnits extends Manager {
    public WorkerDefenceFightCombatUnits(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.enemiesNear().combatUnits().notEmpty() && !shouldNotFight();
    }

    private boolean shouldNotFight() {
//        if (A.supplyUsed() >= 40) return true;
//        if (unit.enemiesNear().empty() || unit.enemiesNear().inRadius(4, unit).empty()) return true;
        if (unit.id() % 7 >= 2) return true;
        if (unit.enemiesNear().empty()) return true;
        if (unit.hp() <= 18) return true;
        if (Enemy.protoss() && unit.hp() <= 25) return true;
        if (unit.isBuilder() || unit.isConstructing()) return true;

        Selection enemiesNear = unit.enemiesNear().inRadius(12, unit);
        if (!Enemy.protoss()) {
            if (enemiesNear.atMost(1)) return false;
            if (enemiesNear.atLeast(4) && unit.friendsNear().combatUnits().atMost(1)) return true;
        }

        return false;
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            ProtectScvBusyConstructing.class,
        };
    }

    @Override
    protected Manager handle() {
        if (handleSubmanagers() != null) return usedManager(this);
        if (handleFightEnemyCombatUnits(unit)) return usedManager(this);

        return null;
    }

    private boolean handleFightEnemyCombatUnits(AUnit worker) {
        if (Enemy.protoss() && worker.hp() <= 38) return false;
        if (Enemy.protoss() && worker.enemiesNear().combatUnits().inRadius(2.6, unit).atLeast(2)) return false;
        if (worker.hp() <= 20) return false;

        if (worker.friendsNear().ofType(AUnitType.Protoss_Photon_Cannon).isNotEmpty()) {
            return attackNearestEnemy(worker);
        }

        if (worker.distToMoreThan(Select.main(), 9)) return false;

//        if (Count.workers() <= 8 || Select.our().inRadius(4, worker).atMost(2)) {
        if (Count.workers() <= 8) return false;

        if (Select.enemyCombatUnits().ofType(
            AUnitType.Terran_Siege_Tank_Siege_Mode,
            AUnitType.Terran_Siege_Tank_Tank_Mode,
            AUnitType.Zerg_Lurker,
            AUnitType.Zerg_Ultralisk,
            AUnitType.Protoss_Archon,
            AUnitType.Protoss_Dark_Templar,
            AUnitType.Protoss_Reaver
//                AUnitType.Protoss_Zealot
        ).inRadius(8, worker).count() >= 1) {
            return false;
        }

        if (fightGroundEnemies(worker)) return true;

        // FIGHT against COMBAT UNITS
        List<AUnit> enemies = worker.enemiesNear()
            .canBeAttackedBy(worker, 2)
            .list();
        for (AUnit enemy : enemies) {
            if (
                worker.hp() <= 20
                    || (worker.hp() <= 39 && worker.friendsNear().bunkers().inRadius(12, worker).notEmpty())
            ) {
                worker.runningManager().runFrom(enemy, 4, Actions.RUN_ENEMY, false);
                return true;
            }
            worker.setTooltipTactical("FurMotherland!");
            return worker.attackUnit(enemy);
        }

        return false;
    }

    private static boolean fightGroundEnemies(AUnit worker) {
        // FIGHT against ZERGLINGS
        for (AUnit enemy : worker.enemiesNear().groundUnits().inRadius(3.2, worker).list()) {
            if (worker.hp() <= 37) continue;
            if (worker.noCooldown() && worker.distToBase() >= 8) continue;

//            if ((worker.hp() <= 20 || Count.workers() <= 9) && runToFarthestMineral(worker, enemy)) {
            if (
                worker.isScv() && worker.hp() <= 54
                    && worker.friendsNear().bunkers().inRadius(12, worker).notEmpty()
            ) {
                worker.setTooltipTactical("Aaargh!");
                worker.runningManager().runFrom(enemy, 4, Actions.RUN_ENEMY, true);
                return true;
            }
            worker.attackUnit(enemy);
            worker.setTooltipTactical("ForMotherland!");
            return true;
        }
        return false;
    }

    private static boolean attackNearestEnemy(AUnit worker) {
        AUnit enemy = worker.enemiesNear().canBeAttackedBy(worker, 2).nearestTo(worker);
        if (enemy == null) return false;

        worker.setTooltip("WDM:Attack", true);
        worker.attackUnit(enemy);
        return true;
    }
}
