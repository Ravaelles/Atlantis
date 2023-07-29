package atlantis;

import atlantis.units.AUnit;

public class OnUnitDiscover {

    public static void update(AUnit unit) {
        // Enemy unit
        if (unit.isEnemy()) {
            Atlantis.enemyNewUnit(unit);
        }

        else if (unit.isOur()) {
        }

        else {
            if (!unit.isRealUnit() && !unit.type().isInvincible()) {
//                    System.out.println("Neutral unit discovered! " + unit.name());
//                if (A.isUms()) {
//                    UmsSpecialActionsManager.NEW_NEUTRAL_THAT_WILL_RENEGADE_TO_US = unit;
//                }
            }
        }
    }
}