package atlantis.units.workers.defence.fight;

import atlantis.architecture.Manager;
import atlantis.game.player.Enemy;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.We;

public class WorkerDefenceFightCombatUnits extends Manager {
    public WorkerDefenceFightCombatUnits(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.enemiesNear().combatUnits().notEmpty() && !shouldNotFight();
    }

    private boolean shouldNotFight() {
        if (WorkerDoNotFight.doNotFight(unit)) {
            return true;
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
//        if (Enemy.protoss() && worker.hp() <= 38) return false;
//        if (Enemy.protoss() && worker.enemiesNear().combatUnits().inRadius(2.6, unit).atLeast(2)) return false;
//        if (worker.hp() <= 20) return false;

//        System.err.println("@ " + A.now() + " - " + unit.typeWithUnitId() + " - " + unit.hp());

//        if (We.protoss() && worker.friendsNear().ofType(AUnitType.Protoss_Photon_Cannon).isNotEmpty()) {
//            return attackNearestEnemy(worker);
//        }

        if (againstProtossDontFightWhenStrongAndTooWounded(worker)) return false;

        int workers = Count.workers();

        if (workers <= 9 && unit.id() % 3 == 0) return false;
        if (workers >= 16 && (unit.id() % 5 <= 1 || unit.shields() <= 2)) return false;

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
            .canBeAttackedBy(worker, 6)
            .nearestTo(unit);

        if (enemy != null) {
            worker.setTooltipTactical("FurMotherland!");
            return worker.attackUnit(enemy);
        }

        return attackNearestEnemy(worker);
    }

    private static boolean againstProtossDontFightWhenStrongAndTooWounded(AUnit worker) {
        return Enemy.protoss()
            && worker.hp() <= 32
            && Count.ourCombatUnits() >= 5
            && worker.meleeEnemiesNearCount(2) == 0;
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

        worker.setTooltip("WDM:Hooray", true);
        worker.attackUnit(enemy);
        return true;
    }
}
