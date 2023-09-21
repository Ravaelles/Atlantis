package atlantis.combat.micro.attack;

import atlantis.units.AUnit;

public class CanAttackAsMelee {

    public static final double THRESHOLD_OF_COMBAT_EVAL = 0.95;

    public static boolean canAttackAsMelee(AUnit unit) {
        if (!unit.isMelee()) return true;

        double combatEval = unit.combatEvalRelative();

        if (combatEval < THRESHOLD_OF_COMBAT_EVAL) return false;

        if (unit.enemiesNear().inRadius(2, unit).canBeAttackedBy(unit, 1).notEmpty()) return true;

        return combatEval >= THRESHOLD_OF_COMBAT_EVAL;
    }
}
