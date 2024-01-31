package atlantis.util;

import atlantis.game.AGame;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import bwapi.UnitType;

public class Helpers {

    public static boolean has(AUnitType type) {
        return Count.ofType(type) > 0;
    }

    public static boolean has(UnitType type) {
        return Count.ofType(AUnitType.from(type)) > 0;
    }

    public static boolean hasFree(AUnitType type) {
        return Count.ofTypeFree(type) > 0;
    }

    public static boolean canAfford(Integer[] mineralsAndGas) {
        return AGame.canAfford(mineralsAndGas[0], mineralsAndGas[1]);
    }

    public static boolean canAfford(int mineralPrice, int gasPrice) {
        return AGame.canAfford(mineralPrice, gasPrice);
    }

    public static boolean supplyUsedAtLeast(int minSupply) {
        return AGame.supplyUsed() >= minSupply;
    }

    public static boolean supplyUsedAtMost(int maxSupply) {
        return AGame.supplyUsed() <= maxSupply;
    }
}
