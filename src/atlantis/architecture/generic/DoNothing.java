package atlantis.architecture.generic;

import atlantis.architecture.Manager;
import atlantis.combat.micro.terran.wraith.AsAirUnitAttackAnyEnemy;
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

//            AsAirUnitAttackAnyEnemy asAirUnitAttackAnyEnemy = new AsAirUnitAttackAnyEnemy(unit);
//            return asAirUnitAttackAnyEnemy.invoke();
//        }

        return null;
    }
}
