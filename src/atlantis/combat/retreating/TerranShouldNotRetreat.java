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
        Manager manager = shouldNotRetreat();

        if (manager != null) {
            (new AttackNearbyEnemies(unit)).invoke();
            return manager;
        }

        return null;
    }

    public Manager shouldNotRetreat() {
        if (unit.isTank() && unit.woundPercentMax(20) && unit.cooldownRemaining() <= 0) {
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
