package atlantis.protoss.reaver;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;

import static atlantis.units.actions.Actions.MOVE_AVOID;

public class ReaverForceHoldToFireInRange extends Manager {
    public ReaverForceHoldToFireInRange(AUnit unit) {
        super(unit);
    }

    @Override
    /**
     * Unfortunately, Scarabs are fucking stupid and can walk into Sunkens long time having the range.
     * It's very hard to prevent it.
     */
    public boolean applies() {
        if (Enemy.terran()) return false;
        if (unit.scarabCount() == 0) return false;

        Selection enemies = unit.enemiesICanAttack(1.2).groundUnits().hasPathFrom(unit);
        if (enemies.isEmpty()) return false;

        AUnit nearestEnemy = enemies.nearestTo(unit);
        double nearestEnemyDist = unit.distTo(nearestEnemy);

//        if (unit.isMoving() && !unit.isAction(MOVE_AVOID)) {
//            double minDist = 7.1 + unit.woundPercent() / 45.0;
        double minDist = 7.25 + (unit.lastStartedRunningLessThanAgo(100) ? 1 : 0);
        if (nearestEnemyDist <= minDist) {
//                System.err.println("HAAAAAAAAAAAAAAAAAAAAAARD HOOOOOOLD " + unit.distToTargetDigit());
            if (nearestEnemy.isCombatBuilding()) {
                unit.moveAwayFrom(nearestEnemy, 1, MOVE_AVOID, "AvoidEnemy");
            }
            return true;
        }
//        }

        if (
            unit.isMoving()
                && nearestEnemyDist <= 7.95
                && A.everyNthGameFrame(20)
                && unit.lastActionMoreThanAgo(45, Actions.HOLD_TO_SHOOT)
        ) {
//            System.err.println("HOOOOOOLD " + unit.distToTargetDigit());
            unit.holdToShoot();
            return true;
        }

        return false;
    }

    @Override
    public Manager handle() {
        if (unit.isAction(MOVE_AVOID)) return usedManager(this, "ReaverRatherAvoid");

        unit.holdToShoot();

        return usedManager(this, "ForceHold2Fire");
    }
}

