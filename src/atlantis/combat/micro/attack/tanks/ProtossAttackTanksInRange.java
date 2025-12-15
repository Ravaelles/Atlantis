package atlantis.combat.micro.attack.tanks;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.range.OurDragoonRange;
import atlantis.units.select.Selection;
import atlantis.util.We;

public class ProtossAttackTanksInRange extends Manager {
    private AUnit tank;

    public ProtossAttackTanksInRange(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!We.protoss()) return false;
        if (!Enemy.terran()) return false;
        if (unit.hp() <= 42) return false;
        if (!unit.hasGroundWeapon()) return false;

        return (tankInRange() != null)
            && (unit.lastTargetWasTank() || unit.hp() >= 65)
            && unit.enemiesNear().tanks().countInRadius(OurDragoonRange.range() + 0.5, unit) > 0
            && tank.friendsNear().nonTanks().countInRadius(4, tank) <= 3;
    }

    private AUnit tankInRange() {
        int tanksInRadius = unit.isMissionDefendOrSparta() ? 5 : 8;

        double extra = unit.isRanged() ? 1.2 : 3.2;
        Selection tanks = unit.enemiesNear()
            .tanks()
            .visibleOnMap()
            .havingAtLeastHp(1)
            .inRadius(tanksInRadius, unit);
        if (tanks.empty()) return null;

        tank = tanks
            .tanksSieged()
            .canBeAttackedBy(unit, extra)
            .nearestTo(unit);
        if (tank != null) return tank;

        extra = unit.isRanged() ? 0 : 3.2;
        tank = tanks
            .tanksUnsieged()
            .canBeAttackedBy(unit, extra)
            .nearestTo(unit);
        if (tank != null) return tank;

        return null;
    }

    @Override
    public Manager handle() {
        if (tank == null || tank.hp() == 0) return null;

//        System.err.println("Tank: " + tank);
        if (shouldComeCloserToTank()) {
            unit.move(tank, Actions.MOVE_ENGAGE);
            unit.forceLastTarget(tank);

//            System.err.println(A.now + ": Coming closer to tank: " + unit);
            return usedManager(this, "MoveToTank");
        }

        if (attackTank()) {
            return usedManager(this);
        }

        return null;
    }

    private boolean shouldComeCloserToTank() {
        if (!tank.isSieged()) return false;

        return unit.cooldown() >= 7 && unit.distTo(tank) >= 0.4 && unit.groundDist(tank) <= 11;
    }

    private boolean attackTank() {
        return unit.attackUnit(tank);
    }
}
