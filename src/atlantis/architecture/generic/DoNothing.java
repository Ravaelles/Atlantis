package atlantis.architecture.generic;

import atlantis.architecture.Manager;
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
        System.out.println("@ " + A.now() + " - DoNothing " + unit.id());

//        if (unit.isAir() && unit.hasAnyWeapon()) {

//            AsAirAttackAnyone asAirUnitAttackAnyEnemy = new AsAirAttackAnyone(unit);
//            return asAirUnitAttackAnyEnemy.invoke();
//        }

        return null;
    }
}
