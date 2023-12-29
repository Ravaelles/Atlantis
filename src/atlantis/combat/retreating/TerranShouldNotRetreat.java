package atlantis.combat.retreating;

import atlantis.architecture.Manager;
import atlantis.combat.micro.attack.AttackNearbyEnemies;
import atlantis.units.AUnit;
import atlantis.util.We;

public class TerranShouldNotRetreat extends Manager {
    public TerranShouldNotRetreat(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!We.terran()) return false;
        if (unit.isTank() && (unit.isWounded() || unit.enemiesNear().inRadius(4, unit).notEmpty())) return false;

        return unit.enemiesNear().visibleOnMap().notEmpty();
    }

    @Override
    protected Manager handle() {
        if (shouldNotRetreat()) {
            if ((new AttackNearbyEnemies(unit)).invoke(this) != null) {
                return this;
            }
        }

        return null;
    }

    public boolean shouldNotRetreat() {
        if (unit.isTank() && unit.woundPercentMax(20) && unit.cooldownRemaining() <= 0) {
            unit.addLog("BraveTank");
            return true;
        }

        if (unit.kitingUnit() && unit.isHealthy() && unit.meleeEnemiesNearCount(1.8) <= 0) {
            unit.addLog("BraveKite");
            return true;
        }

        if (unit.isStimmed() && (unit.hp() >= 17 || unit.noCooldown()) && unit.enemiesNearInRadius(1.8) <= 2) {
            unit.addLog("BraveStim");
            return true;
        }

//        if (unit.friendsInRadius(4).count() >= 8) {
//            unit.setTooltip("BraveRetard");
//            return true;
//        }

        return false;
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
