package atlantis.combat.retreating.protoss.small_scale;

import atlantis.units.AUnit;
import atlantis.units.select.Selection;

public class ProtossMeleeVsMelee {
    public static boolean beBraveIn1v1(AUnit unit, Selection enemies) {
        if (unit.isMelee() && enemies.size() == 1 && enemies.onlyMelee()) {
            if (enemies.first().hp() < unit.hp()) return true;
        }
        return false;
    }
}
