package atlantis.combat.micro.terran.wraith;

import atlantis.architecture.Manager;
import atlantis.debug.painter.AAdvancedPainter;
import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import bwapi.Color;

public class ChangeLocationIfRanTooLong extends Manager {
    public ChangeLocationIfRanTooLong(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.isGroundUnit()) return false;
        if (unit.enemiesNear().inRadius(14, unit).empty()) return false;

        if (
            unit.looksIdle()
                || unit.lastActionMoreThanAgo(3)
                || unit.lastAttackFrameMoreThanAgo(30 * 3)
        ) {
            return true;
        }

        return unit.lastStartedAttackAgo() > 30 * 5 && unit.lastStartedRunningLessThanAgo(30 * 5);
    }

    @Override
    public Manager handle() {
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
}
