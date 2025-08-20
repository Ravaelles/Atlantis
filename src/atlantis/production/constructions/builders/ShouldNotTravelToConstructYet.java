package atlantis.production.constructions.builders;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.units.AUnitType;
import atlantis.util.We;

public class ShouldNotTravelToConstructYet {
    public static boolean check(AUnitType building, double distance) {
        if (We.zerg()) return false;

        if (AGame.timeSeconds() < 300) {
            int baseBonus = building.isBase() ? 80 : 0;
            int mineralMargin = mineralMargin(building, distance);

            return !A.canAfford(
                building.mineralPrice() - mineralMargin - (int) (distance * 1.3) - baseBonus,
                building.gasPrice() - 16 - (int) distance
            );
        }

        return false;
    }

    private static int mineralMargin(AUnitType building, double distance) {
        return (distance <= 15 ? 16 : 32)
            + (building.isCyberneticsCore() ? 8 : 0);
    }
}
