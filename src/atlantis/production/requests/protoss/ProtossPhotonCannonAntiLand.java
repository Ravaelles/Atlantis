package atlantis.production.requests.protoss;

import atlantis.information.generic.ArmyStrength;
import atlantis.information.strategy.GamePhase;
import atlantis.map.position.HasPosition;
import atlantis.production.constructing.Construction;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.production.requests.AntiLandBuildingCommander;
import atlantis.units.AUnitType;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.util.Enemy;

public class ProtossPhotonCannonAntiLand extends AntiLandBuildingCommander {

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

    @Override
    public HasPosition nextPosition() {
        return nextPosition(Select.ourBases().last());
    }

    @Override
    public HasPosition nextPosition(HasPosition nearTo) {
        HasPosition standard = super.nextPosition(nearTo);

        if (standard == null) {
            return null;
        }

        HasPosition existing = Select.ourWithUnfinished(type()).inRadius(8, standard).nearestTo(standard);
        if (existing != null) {
            return existing;
        }

        return standard;
    }
}
