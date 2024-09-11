package atlantis.combat.running;

import atlantis.game.A;
import atlantis.information.strategy.GamePhase;
import atlantis.information.strategy.OurStrategy;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.util.We;

public class RunTowardsNonStandard {
    private final atlantis.combat.running.ARunningManager ARunningManager;

    public RunTowardsNonStandard(atlantis.combat.running.ARunningManager ARunningManager) {
        this.ARunningManager = ARunningManager;
    }

    protected HasPosition shouldRunTowardsBunker() {
        if (!We.terran() || !GamePhase.isEarlyGame() || Count.bunkers() == 0) {
            return null;
        }

        if (ARunningManager.unit.isTerranInfantry()) {
            AUnit bunker = Select.ourOfType(AUnitType.Terran_Bunker).nearestTo(ARunningManager.unit);
            if (
                bunker != null
                    && ARunningManager.unit.enemiesNearInRadius(2) == 0
                    && bunker.distToMoreThan(ARunningManager.unit, 5 + ARunningManager.unit.woundPercent() / 12)
            ) {
                return bunker.position();
            }
        }

        return null;
    }
}
