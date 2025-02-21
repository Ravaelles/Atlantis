package atlantis.combat.micro.avoid.special;

import atlantis.architecture.Manager;
import atlantis.information.enemy.EnemyUnits;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.game.player.Enemy;
import atlantis.util.We;
import bwapi.Color;

public class AvoidTanksSieged extends Manager {
    private AUnit tankSieged;
    private double distToTank;

    public AvoidTanksSieged(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.isMissionDefendOrSparta()) return false;

//        AUnit tank = unit.enemiesNear().tanksSieged().first();
//        System.out.println(
//            "unit = " + unit + " / ev:" + unit.eval() + " / sieged: " +
//                unit.enemiesNear().tanksSieged().size() + " / enemyEval:"
//                + (tank != null ? tank.eval() : "-")
//        );

        return Enemy.terran()
            && unit.isGroundUnit()
            && !unit.isSieged()
            && (tankSieged = tank()) != null
            && (distToTank = unit.distTo(tankSieged)) <= 16
            && notStrongerLocally()
            && tankSieged.distToBase() >= 25;
    }

    private boolean notStrongerLocally() {
        return unit.eval() <= 2.5 && (moreEnemyTanksThanOurUnits() || almostDeadAndFarFromFight());
    }

    private boolean almostDeadAndFarFromFight() {
        if (!We.protoss()) return false;

        return unit.hp() <= 75 && distToTank >= 5;
    }

    private boolean moreEnemyTanksThanOurUnits() {
        return (tankSieged.friendsNear().combatUnits().countInRadius(7, unit) + 2)
            >= unit.friendsNear().combatUnits().countInRadius(9, unit);
    }

    private AUnit tank() {
        return EnemyUnits.discovered()
            .tanksSieged()
            .inRadius(13.5 + unit.woundPercent() / 30.0, unit)
            .nearestTo(unit);
    }

    @Override
    protected Manager handle() {
        if (tankSieged == null) return null;

        tankSieged.paintRectangle(1.1, 0.8, Color.Orange);
        tankSieged.paintRectangle(1.06, 0.76, Color.Orange);

        if (!unit.isAttacking() && unit.cooldown() <= 7 && unit.distTo(tankSieged) <= 0.7) {
            unit.holdPosition(Actions.HOLD_POSITION, "TANK!");
            return usedManager(this);
        }

        unit.setTooltip("TANK!");
        unit.runningManager().runFrom(tankSieged, 2, Actions.MOVE_AVOID, false);
        return usedManager(this);
    }
}
