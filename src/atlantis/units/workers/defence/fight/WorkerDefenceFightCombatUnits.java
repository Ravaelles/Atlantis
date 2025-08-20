package atlantis.units.workers.defence.fight;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.AliveEnemies;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

public class WorkerDefenceFightCombatUnits extends Manager {
    public WorkerDefenceFightCombatUnits(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.enemiesNear().combatUnits().notEmpty()
            && unit.hp() >= 21
            && unit.lastStartedRunningMoreThanAgo(30 * 10)
            && unit.friendsNear().bases().countInRadius(6, unit) > 0
            && unit.hp() >= (Enemy.protoss() ? 34 : 26)
            && unit.distToBase() <= 8;
//            && !WorkerDoNotFight.doNotFight(unit);
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            ProtectScvBusyConstructing.class,
        };
    }

    @Override
    protected Manager handle() {
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
        if (workers >= 16 && unit.isWounded() && (unit.id() % 5 <= 1 || unit.hp() <= 30)) return false;

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

        if (processFightEnemyCombatUnits(unit)) {
            return true;
        }

        return false;
    }

    public static boolean processFightEnemyCombatUnits(AUnit unit) {
        Selection potentialEnemies = potentialEnemies(unit);
        AUnit enemy = potentialEnemies.nearestTo(unit);

        if (enemy != null && enemy.enemiesNear().bases().countInRadius(12, enemy) > 0) {
            unit.setTooltipTactical("FurMotherland!");
            return unit.attackUnit(enemy);
        }

        return false;
    }

    private static Selection potentialEnemies(AUnit worker) {
        return AliveEnemies.get()
            .canBeAttackedBy(worker, 10);
    }

    private static boolean againstProtossDontFightWhenStrongAndTooWounded(AUnit worker) {
        return Enemy.protoss()
            && worker.hp() <= 33
//            && Count.ourCombatUnits() >= 5
            && worker.meleeEnemiesNearCount(2) == 0;
    }
}
