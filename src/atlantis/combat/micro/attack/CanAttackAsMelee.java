package atlantis.combat.micro.attack;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.util.Enemy;
import atlantis.util.We;

public class CanAttackAsMelee {

    public static final double THRESHOLD_OF_COMBAT_EVAL = 0.95;

    public static boolean canAttackAsMelee(AUnit unit) {
        if (!unit.isMelee()) return true;

        if (allowForProtoss(unit)) return true;
        if (allowForTerran(unit)) return true;
        if (allowForZerg(unit)) return true;

        double combatEval = unit.combatEvalRelative();

        if (combatEval < THRESHOLD_OF_COMBAT_EVAL) return false;

        if (unit.enemiesNear().canBeAttackedBy(unit, 1).notEmpty()) return true;

        return combatEval >= THRESHOLD_OF_COMBAT_EVAL;
    }

    private static boolean allowForTerran(AUnit unit) {
        if (!We.terran() || !unit.isFirebat()) return false;
        if (unit.hp() <= (Enemy.protoss() ? 33 : 18)) return false;

//        if (unit.enemiesNear().ranged().nonBuildings().inRadius(2, unit).notEmpty()) return true;
        if (unit.enemiesNear().canBeAttackedBy(unit, 1.5).notEmpty()) return true;

        return unit.friendsNear().ofType(
            AUnitType.Terran_Bunker,
            AUnitType.Terran_Medic
        ).inRadius(4, unit).notEmpty();
    }

    private static boolean allowForProtoss(AUnit unit) {
        if (!We.protoss()) return false;

        if (unit.enemiesNear().ranged().nonBuildings().inRadius(2, unit).notEmpty()) return true;

        if (unit.hp() <= (Enemy.protoss() ? 33 : 18)) return false;

        return unit.friendsNear().ofType(AUnitType.Protoss_Photon_Cannon).inRadius(4, unit).notEmpty();
    }

    private static boolean allowForZerg(AUnit unit) {
        if (!We.zerg()) return false;

        if (unit.enemiesNear().ranged().nonBuildings().inRadius(2, unit).notEmpty()) return true;

        if (unit.hp() <= (Enemy.protoss() ? 18 : 11)) return false;

        return unit.friendsNear().ofType(AUnitType.Zerg_Sunken_Colony).inRadius(4, unit).notEmpty();
    }
}
