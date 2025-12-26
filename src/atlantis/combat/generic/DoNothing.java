package atlantis.combat.generic;

import atlantis.architecture.Manager;
import atlantis.debug.painter.AAdvancedPainter;
import atlantis.game.A;
import atlantis.map.choke.Chokes;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import bwapi.Color;

public class DoNothing extends Manager {
    public DoNothing(AUnit unit) {
        super(unit);
    }

    @Override
    protected Manager handle() {
        AAdvancedPainter.paintTextCentered(unit, unit.idWithHash(), Color.Red);
        A.errPrintln("@ " + A.now() + " - Still DoNothing! " + unit.id());

        if (unit.move(Chokes.natural(), Actions.MOVE_IDLE)) return usedManager(this);
        if (unit.move(Chokes.mainChoke(), Actions.MOVE_IDLE)) return usedManager(this);

//        if (FixActions.moveToLeader(unit)) return usedManager(this, "DoNothing-2FP");
//        if (FixActions.movedSlightlyOrToFocusPoint(unit)) return usedManager(this, "DoNothing-2FP");

//        if (!unit.isLeader() && unit.moveToLeader(Actions.MOVE_FORMATION, "DoNothingMove2Leader")) {
//            return usedManager(this);
//        }
//
//        APosition center = unit.friendsNear().groundUnits().center();
//        if (center != null && unit.move(center, Actions.MOVE_FORMATION, "DoNothingMove2Center")) {
//            return usedManager(this);
//        }
//
//        ErrorLog.debug(A.minSec() + ":  DoNothing: " + unit);

//        if (A.s >= 10) {
//            AUnit enemy = EnemyUnits.discovered().groundUnits().nearestTo(unit);
//            if (enemy != null && unit.move(enemy, Actions.MOVE_UNFREEZE)) return usedManager(this, "DoNothing2Enemy");
//        }

//        Mission mission = unit.mission();
//        if (mission != null) mission.forceHandle();

        return null;
    }
}
