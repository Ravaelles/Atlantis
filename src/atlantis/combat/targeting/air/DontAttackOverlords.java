package atlantis.combat.targeting.air;

import atlantis.units.AUnit;
import atlantis.units.select.Select;

public class DontAttackOverlords {
    public static boolean forbidden(AUnit unit) {
        return unit.enemiesNear().combatUnits().countInRadius(15, unit) <= (unit.shotSecondsAgo() <= 4 ? 1 : 0)
            && Select.enemyCombatUnits().visibleOnMap().atMost(3);
    }
}
