package atlantis.combat.micro.terran.wraith;

import atlantis.architecture.Manager;
import atlantis.debug.painter.AAdvancedPainter;
import atlantis.game.A;
import atlantis.map.AMap;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;
import bwapi.Color;

public class ChangeLocationIfRanTooLong extends Manager {
    public ChangeLocationIfRanTooLong(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.isGroundUnit()) return false;
        if (unit.looksIdle()) return false;
        if (unit.enemiesNear().air().notEmpty()) return false;

        Selection enemiesNear = unit.enemiesNear();

        if (enemiesNear.nonBuildings().canBeAttackedBy(unit, 0.95).notEmpty()) return false;

        if (unit.didntShootRecently(7)) return true;

        if (enemiesNear.inRadius(14, unit).empty()) return false;

        if (
            unit.lastActionMoreThanAgo(3) || unit.lastAttackFrameMoreThanAgo(30 * 4)
        ) return true;

        return unit.lastStartedAttackAgo() > 30 * 5 && unit.lastStartedRunningLessThanAgo(30 * 5);
    }

    @Override
    public Manager handle() {
        if (A.seconds() % 14 >= 5) {
            if (goElsewhere()) return usedManager(this);
        }

        if (unit.lastActionLessThanAgo(30 * 14, Actions.MOVE_SPECIAL)) {
            if (unit.isMoving()) {
                return usedManager(this);
            }

            return null;
        }

        int delta = 12 + unit.id() % 7;
        int dx = -delta + A.rand(0, 2 * delta);
        int dy = -delta + A.rand(0, 2 * delta);
        APosition goTo = unit.position().translateByTiles(dx, dy).makeValid();

        AAdvancedPainter.paintCircleFilled(unit, 6, Color.Orange);

        if (goTo != null) {
            unit.move(goTo, Actions.MOVE_SPECIAL, "BeSmart");
            return usedManager(this);
        }

        return null;
    }

    private boolean goElsewhere() {
        APosition invisiblePosition = AMap.randomInvisiblePosition(unit);
        if (invisiblePosition != null) {
            unit.move(invisiblePosition, Actions.MOVE_SPECIAL, "ChangeLocRand", true);
            return true;
        }
        return false;
    }
}
