package atlantis.combat.micro.generic.managers;

import atlantis.architecture.Manager;
import atlantis.information.enemy.EnemyUnits;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

public class DetectHiddenEnemyClosestToBase extends Manager {
    private AUnit dangerousInvisibleEnemy;

    public DetectHiddenEnemyClosestToBase(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (Select.main() == null) return false;
        if (!isUnitAssignedForThisTask()) return false;

        dangerousInvisibleEnemy = enemyHiddenUnitCloseToBase();

        return dangerousInvisibleEnemy != null
            && dangerousInvisibleEnemy.friendsNear().exclude(unit).notEmpty();
    }

    public Manager handle() {
        return usedManager(this);
    }

    private boolean isUnitAssignedForThisTask() {
        return unit.equals(Select.ourOfType(unit.type()).first());
    }

    protected AUnit enemyHiddenUnitCloseToBase() {
        Selection undetectedEnemies = EnemyUnits.discovered().effUndetected().havingAntiGroundWeapon();
//        AUnit enemy = undetectedEnemies.nearestTo(Select.mainOrAnyBuilding());
        AUnit enemy = null;

        for (AUnit undetected : undetectedEnemies.list()) {
//            if (undetected.friendsNear().detectors().inRadius(9, undetected).isEmpty()) {
            enemy = undetected;
//                break;
//            }
        }

        if (enemy == null) return null;

        AUnit nearestBase = Select.ourBuildingsWithUnfinished().nearestTo(enemy);

        return shouldScienceVesselFlyToEnemy(nearestBase, enemy) ? enemy : null;
    }

    private static boolean shouldScienceVesselFlyToEnemy(AUnit base, AUnit enemy) {
        return base != null
            && base.distTo(enemy) <= 7
            && Select.ourCombatUnits().inRadius(10, enemy).atLeast(2);
    }
}
