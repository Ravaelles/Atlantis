package atlantis.production.requests.protoss;

import atlantis.map.position.HasPosition;
import atlantis.production.constructions.position.protoss.FindPositionForCannon;
import atlantis.production.requests.AntiLandBuildingCommander;
import atlantis.units.AUnitType;
import atlantis.units.select.Have;
import atlantis.units.select.Select;
import atlantis.game.player.Enemy;

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

//        if (GamePhase.isEarlyGame() && Army.relative() <= 60 && Count.dragoons() <= 2) {
//            return 2;
//        }

        return 0;
    }

    @Override
    public HasPosition nextPosition() {
        return nextPosition(Select.ourBases().last());
    }

    @Override
    public HasPosition nextPosition(HasPosition nearTo) {
        HasPosition standard = FindPositionForCannon.find(nearTo, null, null);

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
