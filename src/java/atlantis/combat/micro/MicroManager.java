package atlantis.combat.micro;

import atlantis.combat.AtlantisCombatEvaluator;
import atlantis.wrappers.SelectUnits;
import jnibwapi.Unit;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public abstract class MicroManager {

    /**
     * If chances to win the skirmish with the nearby enemy units aren't favorable, safely retreat.
     */
    protected boolean handleUnfavorableOdds(Unit unit) {
        if (!AtlantisCombatEvaluator.isSituationFavorable(unit)) {
            if (unit.isJustShooting()) {
                return true;
            }

            return run(unit);
//            unit.runFrom(null);
//            Unit safePoint = SelectUnits.mainBase();
//            if (safePoint != null) {
//                if (safePoint.distanceTo(unit) > 15) {
//                    unit.move(unit);
//                } else {
//                    unit.runFrom(null);
//                }
//                return true;
//            } else {
//                unit.setTooltip("No safeplace");
//                return false;
//            }
        }

        return false;
    }

    /**
     * If unit is severly wounded, it should run.
     */
    protected boolean handleLowHealthIfNeeded(Unit unit) {
        if (unit.getHP() <= 11) {
            return run(unit);
        }

        return false;
    }

    // =========================================================
    /**
     * Makes unit run (from close enemies) in the most reasonable way possible.
     */
    private boolean run(Unit unit) {
//        Unit nearestEnemy = SelectUnits.enemyRealUnit().nearestTo(unit);
//        if (nearestEnemy != null) {
//            if (nearestEnemy.distanceTo(nearestEnemy) <= 6.5) {
//                unit.runFrom(nearestEnemy);
//                unit.setTooltip("LOW HP");
//            }
//        } else {
//            unit.move(AtlantisMap.getRandomInvisiblePosition(unit), true);
//        }
        Unit mainBase = SelectUnits.mainBase();
        if (mainBase != null && mainBase.distanceTo(unit) > 10) {
            unit.setTooltip("Run to base");
            unit.move(mainBase);
            return true;
        } else {
            unit.setTooltip("Run fail");
            return false;
        }
    }

}
