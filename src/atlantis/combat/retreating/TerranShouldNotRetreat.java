package atlantis.combat.retreating;

import atlantis.units.AUnit;
import atlantis.architecture.Manager;

public class TerranShouldNotRetreat extends Manager {

    public TerranShouldNotRetreat(AUnit unit) {
        super(unit);
    }

    public Manager shouldNotRetreat() {
        if (!unit.isTerran()) {
            return null;
        }

        if (unit.isTank() && unit.woundPercentMax(60) && unit.cooldownRemaining() <= 0) {
            unit.setTooltip("BraveTank");
            return usedManager(this);
        }

        if (unit.kitingUnit() && unit.isHealthy()) {
            unit.setTooltip("BraveKite");
            return usedManager(this);
        }

        if (unit.isStimmed() && (unit.hp() >= 17 || unit.noCooldown()) && unit.enemiesNearInRadius(1.8) <= 2) {
            unit.setTooltip("BraveStim");
            return usedManager(this);
        }

//        if (unit.friendsInRadius(4).count() >= 8) {
//            unit.setTooltip("BraveRetard");
//            return true;
//        }

        return null;
    }

    public boolean shouldRetreat() {
        if (unit.isTerranInfantry()) {
            if (!unit.mission().isMissionDefend()) {
                if (unit.enemiesNear().ranged().notEmpty() && unit.friendsNear().atMost(4) && unit.combatEvalRelative() <= 2) {
                    unit.setTooltipTactical("BewareRanged");
                    return true;
                }
            }
        }

        return false;
    }
}
