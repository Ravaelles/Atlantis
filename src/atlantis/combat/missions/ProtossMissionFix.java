package atlantis.combat.missions;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;

public class ProtossMissionFix {

    public static boolean handle(AUnit unit, AUnit enemy) {
        if (unit.isZealot() && enemy.isZealot()) {
            if (unit.friendsNear().ofType(AUnitType.Protoss_Photon_Cannon).inRadius(2.8, unit).notEmpty()) {
                return true;
            }

//            int ourZealots = unit.friendlyZealotsNearCount(1.3);
            int ourZealots = enemy.enemyZealotsNearCount(1.1);
            if (ourZealots < unit.enemiesNear().inRadius(1.2, unit).count()) {
                return false;
            }
        }

        return false;
    }

}
