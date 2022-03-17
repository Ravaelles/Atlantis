package atlantis.production.requests.protoss;

import atlantis.information.generic.ArmyStrength;
import atlantis.information.strategy.GamePhase;
import atlantis.production.requests.AntiLandBuildingManager;
import atlantis.units.AUnitType;
import atlantis.units.select.Have;

public class ProtossPhotonCannonAntiLand extends AntiLandBuildingManager {

    public AUnitType type() {
        return AUnitType.Protoss_Photon_Cannon;
    }

    @Override
    public boolean shouldBuildNew() {
        if (!Have.a(type())) {
            return false;
        }

        if (GamePhase.isEarlyGame()) {
            return existing() <= 2 && ArmyStrength.weAreMuchWeaker();
        }

        return false;
    }

}
