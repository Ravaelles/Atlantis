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

            return AtlantisRunManager.run(unit);
        }

        AtlantisRunManager.unitWantsStopRunning(unit);
        return false;
    }

    /**
     * If unit is severly wounded, it should run.
     */
    protected boolean handleLowHealthIfNeeded(Unit unit) {
        if (unit.getHP() <= 11) {
            return AtlantisRunManager.run(unit);
        }

        return false;
    }

}
