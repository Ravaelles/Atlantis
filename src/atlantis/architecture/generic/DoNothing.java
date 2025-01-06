package atlantis.architecture.generic;

import atlantis.architecture.Manager;
import atlantis.combat.missions.Mission;
import atlantis.debug.painter.AAdvancedPainter;
import atlantis.game.A;
import atlantis.information.enemy.EnemyUnits;
import atlantis.map.position.APosition;
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

        AUnit enemy = EnemyUnits.discovered().groundUnits().nearestTo(unit);
        if (enemy != null && unit.move(enemy, Actions.MOVE_UNFREEZE)) return usedManager(this, "DoNothing2Enemy");

        if (!unit.isLeader() && unit.moveToLeader(Actions.MOVE_FORMATION, "DoNothingMove2Leader")) {
            return usedManager(this);
        }

        APosition center = unit.friendsNear().groundUnits().center();
        if (center != null && unit.move(center, Actions.MOVE_FORMATION, "DoNothingMove2Center")) {
            return usedManager(this);
        }

//        Mission mission = unit.mission();
//        if (mission != null) mission.forceHandle();

        return null;
    }
}
