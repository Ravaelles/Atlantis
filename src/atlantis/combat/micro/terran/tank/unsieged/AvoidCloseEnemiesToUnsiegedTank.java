package atlantis.combat.micro.terran.tank.unsieged;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;

public class AvoidCloseEnemiesToUnsiegedTank extends Manager {
    private Selection enemies;

    public AvoidCloseEnemiesToUnsiegedTank(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        boolean hasCooldown = unit.cooldown() >= 3;

        double minDist = (hasCooldown ? 6.8 : 6.12) - (unit.isHealthy() ? 1.65 : 0);
        enemies = unit.enemiesNear()
            .groundUnits()
            .nonBuildings()
            .ranged()
            .havingAntiGroundWeapon()
            .inRadius(minDist, unit);

        return enemies.atLeast(1);
    }

    protected Manager handle() {
        if (unit.moveToMain(Actions.MOVE_AVOID, "CloseEnemies")) return usedManager(this);

//        unit.runningManager().runFrom(
//            enemies.nearestTo(unit), 2, Actions.MOVE_AVOID, false
//        );

        return null;
    }
}
