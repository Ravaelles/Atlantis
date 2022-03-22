package atlantis.production.requests.protoss;

import atlantis.information.generic.ArmyStrength;
import atlantis.information.strategy.GamePhase;
import atlantis.production.requests.AntiLandBuildingManager;
import atlantis.units.AUnitType;
import atlantis.units.select.Have;
import atlantis.util.Enemy;

public class ProtossPhotonCannonAntiLand extends AntiLandBuildingManager {

    public AUnitType type() {
        return AUnitType.Protoss_Photon_Cannon;
    }

    @Override
    public int expected() {
        if (Enemy.terran()) {
            return 0;
        }

        if (!Have.a(AUnitType.Protoss_Forge)) {
            return 0;
        }

        if (GamePhase.isEarlyGame() && ArmyStrength.weAreMuchWeaker()) {
            return 2;
        }

        return 0;
    }

}
