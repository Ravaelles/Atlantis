package atlantis.production.requests.protoss;

import atlantis.information.generic.ArmyStrength;
import atlantis.information.strategy.GamePhase;
import atlantis.production.requests.AntiAirBuildingManager;
import atlantis.units.AUnitType;
import atlantis.units.select.Have;

public class ProtossPhotonCannonAntiAir extends AntiAirBuildingManager {

    public AUnitType type() {
        return AUnitType.Protoss_Photon_Cannon;
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

}
