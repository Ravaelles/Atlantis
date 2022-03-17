package atlantis.production.requests.zerg;

import atlantis.information.generic.ArmyStrength;
import atlantis.information.strategy.GamePhase;
import atlantis.map.position.HasPosition;
import atlantis.production.requests.AntiLandBuildingManager;
import atlantis.units.AUnitType;
import atlantis.units.select.Have;

public class ZergSunkenColony extends AntiLandBuildingManager {

    @Override
    public AUnitType type() {
        return AUnitType.Zerg_Sunken_Colony;
    }

    @Override
    public boolean shouldBuildNew() {
        if (!Have.a(type())) {
            return false;
        }

        if (GamePhase.isEarlyGame()) {
            return existing() <= 4 && ArmyStrength.weAreWeaker();
        }

        return false;
    }

    @Override
    public HasPosition nextBuildingPosition() {
        return super.nextBuildingPosition();
    }
}
