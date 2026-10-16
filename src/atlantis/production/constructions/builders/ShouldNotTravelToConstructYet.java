package atlantis.production.constructions.builders;

import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.util.We;

public class ShouldNotTravelToConstructYet {
    public static boolean check(AUnitType building, double distance) {
        if (We.zerg()) return false;

        if (AGame.timeSeconds() < 300) {
            int baseBonus = building.isBase() ? 80 : 0;
            int mineralMargin = mineralMargin(building, distance);

            // Owner 2026-10-16: the first Pylon warped with ~130 banked
            // against its 100 price - arrival with a stockpile. The walk
            // discount above still lands past affordability, so the first
            // Pylon leaves 24 minerals earlier: a briefly idle builder at
            // the site is cheap, a supply-blocked base is not. Only while
            // no Pylon stands - later Pylons keep the old threshold.
            int firstPylonBonus = building.isPylon() && Count.pylons() == 0 ? 24 : 0;

            return !A.canAfford(
                building.mineralPrice() - mineralMargin - (int) (distance * 1.3) - baseBonus - firstPylonBonus,
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
