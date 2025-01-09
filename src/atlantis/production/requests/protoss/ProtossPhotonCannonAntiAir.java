package atlantis.production.requests.protoss;

import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.generic.ArmyStrength;
import atlantis.information.strategy.GamePhase;
import atlantis.production.requests.AntiAirBuildingCommander;
import atlantis.units.AUnitType;
import atlantis.units.select.Have;

public class ProtossPhotonCannonAntiAir extends AntiAirBuildingCommander {
    public AUnitType type() {
        return AUnitType.Protoss_Photon_Cannon;
    }

    @Override
    public int expected() {
        if (!Have.a(AUnitType.Protoss_Forge)) {
            return 0;
        }

        int mutaBonus = mutaBonus();

        if (existingWithUnfinished() <= 1 && ArmyStrength.weAreWeaker()) {
            return 2 + mutaBonus;
        }

        return 0 + mutaBonus;
    }

    private int mutaBonus() {
        if (!Enemy.zerg()) return 0;

        return EnemyInfo.goesZergAirUnits()
            ? Math.min(6, 2 + EnemyUnits.mutas() / 4 + A.minerals() / 400)
            : 0;
    }

}
