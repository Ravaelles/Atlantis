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
    public int expected() {
        if (!Have.a(AUnitType.Protoss_Forge)) {
            return 0;
        }

        if (GamePhase.isEarlyGame()) {
            if (existingWithUnfinished() <= 1 && ArmyStrength.weAreWeaker()) {
                return 2;
            }
        }

        return 0;
    }

}
