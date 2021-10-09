package atlantis.special;

import atlantis.units.AUnit;
import atlantis.units.Select;
import atlantis.units.actions.UnitActions;

public class ProtossObserverManager {

    public static boolean update(AUnit observer) {
        AUnit mainBase = Select.mainBase();
        if (mainBase == null) {
            return false;
        }

        AUnit dangerousInvisibleEnemy = Select.enemy().invisible().combatUnits().nearestTo(mainBase);
        if (dangerousInvisibleEnemy != null) {
            observer.move(dangerousInvisibleEnemy.getPosition(), UnitActions.MOVE, "Reveal");
            return true;
        }

        return false;
    }

}
