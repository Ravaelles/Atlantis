package atlantis.combat.micro.terran.tank.unsieged;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;

public class AvoidCloseEnemiesToUnsiegedTank extends Manager {
    private Selection enemies;

    public AvoidCloseEnemiesToUnsiegedTank(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
//        boolean hasCooldown = unit.cooldown() >= 3;

        double base = Enemy.protoss() ? 6.7 : 5.7;
        double minDist = base - (unit.isHealthy() ? 0.8 : 0);
        enemies = unit.enemiesNear()
            .groundUnits()
            .nonBuildings()
            .ranged()
            .havingAntiGroundWeapon()
            .inRadius(minDist, unit);

        return enemies.atLeast(1);
    }

    protected Manager handle() {
        if (unit.moveToSafety(Actions.MOVE_AVOID, "CloseEnemies")) return usedManager(this);

//        unit.runningManager().runFrom(
//            enemies.nearestTo(unit), 2, Actions.MOVE_AVOID, false
//        );

        return null;
    }
}
