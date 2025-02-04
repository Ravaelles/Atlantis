package atlantis.combat.micro.terran.tank.unsieged;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;

public class AvoidCloseMeleeEnemiesToUnsiegedTank extends Manager {
    private Selection enemies;

    public AvoidCloseMeleeEnemiesToUnsiegedTank(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.cooldown() <= 2 || unit.hp() >= 140) return false;

        enemies = unit.enemiesNear().melee().inRadius(3.5 + unit.woundPercent() / 40.0, unit);

        return enemies.atLeast(1);
    }

    protected Manager handle() {
        if (unit.runningManager().runFrom(
            enemies.nearestTo(unit),
            3,
            Actions.MOVE_AVOID,
            true
        )) return usedManager(this, "CloseMelee");

//        if (unit.moveAwayFrom(
//            enemies.nearestTo(unit),
//            3,
//            Actions.MOVE_AVOID,
//            "CloseMelee"
//        )) return usedManager(this);

        return null;
    }
}
