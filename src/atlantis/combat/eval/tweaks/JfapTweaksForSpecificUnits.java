package atlantis.combat.eval.tweaks;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Selection;

public class JfapTweaksForSpecificUnits {
    //    private static final int HYDRA_SINGLE_UNIT_BONUS = 180;
//    private static final int HYDRA_SINGLE_UNIT_BONUS = 5;
    private static final int HYDRA_SINGLE_UNIT_BONUS = 0;
    private static final double MORE_GOONS_VS_GOONS_BONUS_MODIFIER = 40.0;

    public static double applyAll(double score, AUnit unit) {
//        score += hydra(unit);
        score += goonsVsGoons(unit);

        return score;
    }

    private static double goonsVsGoons(AUnit unit) {
        if (!unit.isGoon()) return 0;
        if (unit.hp() <= 0) return 0;

        Selection enemies = unit.enemiesNear();
        if (!enemies.mostlyOfType(AUnitType.Protoss_Dragoon, 80)) return 0;

        Selection ours = unit.friendsNear();
        if (!ours.mostlyOfType(AUnitType.Protoss_Dragoon, 80)) return 0;

        return (MORE_GOONS_VS_GOONS_BONUS_MODIFIER * ours.count()) / enemies.count();
    }

    public static double hydra(AUnit unit) {
        Selection friendlyHydras = unit.friendsNear().add(unit).ofType(AUnitType.Zerg_Hydralisk);
        return friendlyHydras.count() * HYDRA_SINGLE_UNIT_BONUS;
    }
}
