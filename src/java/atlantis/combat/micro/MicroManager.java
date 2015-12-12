package atlantis.combat.micro;

import atlantis.combat.AtlantisCombatEvaluator;
import atlantis.information.AtlantisMap;
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
        if (!unit.isRunning() && !AtlantisCombatEvaluator.isSituationFavorable(unit)) {
            unit.runFrom(null);
            unit.setTooltip("Run");
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
        if (unit.getHP() <= 10) {
            run(unit);

            return true;
        }

        return false;
    }

    // =========================================================
    /**
     * Makes unit run (from close enemies) in the most reasonable way possible.
     */
    private void run(Unit unit) {
        Unit nearestEnemy = SelectUnits.enemyRealUnit().nearestTo(unit);
        if (nearestEnemy != null) {
            if (nearestEnemy.distanceTo(nearestEnemy) <= 6.5) {
                unit.runFrom(nearestEnemy);
                unit.setTooltip("RUN");
            }
        } else {
            unit.move(AtlantisMap.getRandomInvisiblePosition(unit), true);
        }
    }

}
