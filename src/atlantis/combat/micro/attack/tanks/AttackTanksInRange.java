package atlantis.combat.micro.attack.tanks;

import atlantis.architecture.Manager;
import atlantis.game.player.Enemy;
import atlantis.units.AUnit;

public class AttackTanksInRange extends Manager {
    private AUnit tank;

    public AttackTanksInRange(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return Enemy.terran() && (unit.lastTargetWasTank() || tankInRange() != null);
    }

    private AUnit tankInRange() {
        double extra = unit.isRanged() ? 1.2 : 3.2;
        return tank = unit.enemiesNear().tanks().canBeAttackedBy(unit, extra).mostWoundedOrNearest(unit);
    }

    @Override
    public Manager handle() {
        if (attackTank()) {
            return usedManager(this);
        }

        return null;
    }

    private boolean attackTank() {
        return unit.attackUnit(tank);
    }
}
