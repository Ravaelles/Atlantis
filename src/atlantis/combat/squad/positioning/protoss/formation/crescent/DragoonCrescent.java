package atlantis.combat.squad.positioning.protoss.formation.crescent;

import atlantis.units.AUnit;

public class DragoonCrescent {
    public static boolean dontApply(AUnit unit) {
        if (!unit.isDragoon()) return false;

        return unit.shields() >= 30 && unit.enemiesNear().onlyMelee();
    }
}
