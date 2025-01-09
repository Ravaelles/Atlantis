package atlantis.information.strategy.response.enemy_hidden;

import atlantis.information.enemy.EnemyFlags;
import atlantis.information.strategy.response.RaceStrategyResponse;
import atlantis.information.strategy.response.protoss.RequestProtossDetection;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;

public class EnemyHiddenUnits {
    public static boolean handleBuildingLeadingToHiddenUnits(AUnit enemyUnit) {
        if (!enemyUnit.isABuilding()) return false;

        return enemyUnit.is(
            AUnitType.Protoss_Citadel_of_Adun, AUnitType.Protoss_Templar_Archives,
            AUnitType.Zerg_Hydralisk_Den
        );
    }

    public static boolean handleHiddenUnitDetected(AUnit enemyUnit) {
        if (
            !enemyUnit.isLurker() && !enemyUnit.isLurkerEgg() && !enemyUnit.isDT()
        ) return false;

        EnemyFlags.HAS_HIDDEN_COMBAT_UNIT = true;

        return RaceStrategyResponse.get().requestDetection(enemyUnit);
    }
}
