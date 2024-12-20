package atlantis.units.workers.defence.fight;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;

public class WorkerDefenceFightCombatUnits extends Manager {
    public WorkerDefenceFightCombatUnits(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.enemiesNear().combatUnits().notEmpty() && !shouldNotFight();
    }

    private boolean shouldNotFight() {
        if (WorkerDoNotFight.doNotFight(unit)) return true;

        if (Count.ourCombatUnits() >= 2) {
            if (unit.hp() <= 29 || unit.friendsNear().combatUnits().inRadius(4.5, unit).empty()) return true;
        }

        if (unit.isBuilder() || unit.isConstructing()) return true;

        Selection enemiesNear = unit.enemiesNear().groundUnits().inRadius(15, unit);
        if (!Enemy.protoss()) {
            if (enemiesNear.atMost(1) && (unit.id() % 2 != 1 || unit.shieldWounded())) return true;
//            if (A.s <= 400 && enemiesNear.atMost(2) && unit.friendsNear().combatUnits().atLeast(5)) return true;
        }

//        // Don't go too far from combat units
//        if (
//            Count.ourCombatUnits() >= 2
//                && unit.friendsNear().combatUnits().inRadius(5, unit).empty()
//        ) return true;

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
//        if (Enemy.protoss() && worker.hp() <= 38) return false;
//        if (Enemy.protoss() && worker.enemiesNear().combatUnits().inRadius(2.6, unit).atLeast(2)) return false;
//        if (worker.hp() <= 20) return false;

        if (worker.friendsNear().ofType(AUnitType.Protoss_Photon_Cannon).isNotEmpty()) {
            return attackNearestEnemy(worker);
        }

        AUnit building = Select.ourBuildingsWithUnfinished().nearestTo(worker);
        if (building != null && building.distTo(worker) >= 6) return false;

//        if (Count.workers() <= 8 || Select.our().inRadius(4, worker).atMost(2)) {
        if (Count.workers() <= 9 && unit.idIsEven()) return false;

        if (Select.enemyCombatUnits().ofType(
            AUnitType.Terran_Siege_Tank_Siege_Mode,
            AUnitType.Terran_Siege_Tank_Tank_Mode,
            AUnitType.Zerg_Lurker,
            AUnitType.Zerg_Ultralisk,
            AUnitType.Protoss_Archon,
            AUnitType.Protoss_Dark_Templar,
            AUnitType.Protoss_Reaver
        ).inRadius(8, worker).count() >= 1) {
            return false;
        }

        // FIGHT against COMBAT UNITS
        AUnit enemy = worker.enemiesNear()
            .canBeAttackedBy(worker, 5)
            .nearestTo(unit);

        if (enemy != null) {
            worker.setTooltipTactical("FurMotherland!");
            return worker.attackUnit(enemy);
        }

        return false;
    }

//    private static boolean fightGroundEnemies(AUnit worker) {
//        for (AUnit enemy : worker.enemiesNear().groundUnits().inRadius(3.2, worker).list()) {
//            if (runIfTheresBunkerNearby(worker)) {
//                worker.setTooltipTactical("Aaargh!");
//                worker.runningManager().runFrom(enemy, 4, Actions.RUN_ENEMY, true);
//                return true;
//            }
//            worker.attackUnit(enemy);
//            worker.setTooltipTactical("ForMotherland!");
//            return true;
//        }
//        return false;
//    }

//    private static boolean runIfTheresBunkerNearby(AUnit worker) {
//        return We.terran()
//            && worker.isScv()
//            && worker.hp() <= 48
//            && worker.friendsNear().bunkers().inRadius(12, worker).notEmpty();
//    }

    private static boolean attackNearestEnemy(AUnit worker) {
        AUnit enemy = worker.enemiesNear().canBeAttackedBy(worker, 5).nearestTo(worker);
        if (enemy == null) return false;

        worker.setTooltip("WDM:Attack", true);
        worker.attackUnit(enemy);
        return true;
    }
}
