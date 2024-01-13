package atlantis.architecture.generic;

import atlantis.architecture.Manager;
import atlantis.combat.missions.Mission;
import atlantis.debug.painter.AAdvancedPainter;
import atlantis.game.A;
import atlantis.units.AUnit;
import bwapi.Color;

public class DoNothing extends Manager {
    public DoNothing(AUnit unit) {
        super(unit);
    }

    @Override
    protected Manager handle() {
        AAdvancedPainter.paintTextCentered(unit, unit.idWithHash(), Color.Red);
        A.errPrintln("@ " + A.now() + " - Still DoNothing! " + unit.id());

//        Mission mission = unit.mission();
//        if (mission != null) mission.forceHandle();

        return null;
    }
}
