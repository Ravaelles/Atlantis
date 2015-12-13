package atlantis.combat;

import atlantis.wrappers.SelectUnits;
import java.util.Collection;
import jnibwapi.Unit;
import jnibwapi.util.BWColor;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class AtlantisCombatEvaluator {

    /**
     * Fight only if our army is locally stronger X% than enemy army. 0.5 = 50%.
     */
    private static double SAFETY_MARGIN = 0.3;

    // =========================================================
    /**
     * Returns <b>TRUE</b> if our unit <b>unit</b> should engage in combat with nearby units or
     * <b>FALSE</b> if enemy is too strong and we should pull back.
     */
    public static boolean isSituationFavorable(Unit unit) {
        return evaluateSituation(unit) >= SAFETY_MARGIN;
    }

    /**
     * Returns <b>POSITIVE</b> value if our unit <b>unit</b> should engage in combat with nearby units or
     * <b>NEGATIVE</b> when enemy is too strong and we should pull back.
     */
    public static double evaluateSituation(Unit unit) {
        Collection<Unit> enemyUnits = SelectUnits.enemy().combatUnits().inRadius(13, unit).list();
        if (enemyUnits.isEmpty()) {
            return +999;
        }
        Collection<Unit> ourUnits = SelectUnits.our().combatUnits().inRadius(7, unit).list();

        double enemyEvaluation = evaluateUnits(enemyUnits);
        double ourEvaluation = evaluateUnits(ourUnits);
        return ourEvaluation / enemyEvaluation - 1;
    }

    // =========================================================
    private static double evaluateUnits(Collection<Unit> units) {
        double strength = 0;
        for (Unit unit : units) {
            double unitEval = evaluateUnitHPandDamage(unit);
            if (unit.isWorker()) {
                strength += 0.2 * unitEval;
            } else if (unit.isBuilding()) {
                strength += 0.5 * unitEval;
            } else {
                strength += unitEval;
            }
        }
        return strength;
    }

    private static double evaluateUnitHPandDamage(Unit unit) {
        return unit.getHP() + unit.getType().getGroundWeapon().getDamageAmount();
    }

    // =========================================================
    // Auxiliary
    /**
     * Auxiliary string with colors.
     */
    public static String getEvalString(Unit unit) {
        double eval = evaluateSituation(unit);
        if (eval > 998) {
            return "";
        } else {
            String string = (eval < 0 ? "" : "+") + String.format("%.1f", eval);

            if (eval < -0.05) {
                string = BWColor.getColorString(BWColor.Red) + string;
            } else if (eval < 0.05) {
                string = BWColor.getColorString(BWColor.Yellow) + string;
            } else {
                string = BWColor.getColorString(BWColor.Green) + string;
            }

            return string;
        }
    }

}
