package atlantis.production.constructions.position.base;

import atlantis.information.enemy.EnemyUnits;
import atlantis.map.base.ABaseLocation;
import atlantis.map.base.BaseLocations;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.select.Select;

import java.util.List;

public class OurNextFreeExpansionMostDistantToEnemy {
    public static APosition find() {
        double bestScore = -99999;
        ABaseLocation best = null;
        for (ABaseLocation baseLocation : nearestBases()) {
            double score = evaluate(baseLocation);
//            System.out.println(baseLocation + " score = " + (int) score);

            if (score > bestScore) {
                bestScore = score;
                best = baseLocation;
            }
        }

//        PrintPositionsToFile.printToFile("test.txt", nearestBases(), best);

        return best != null ? best.position() : null;
    }

    private static double evaluate(ABaseLocation baseLocation) {
        return -1.5 * farFromMainPenalty(baseLocation) + farFromEnemyBonus(baseLocation);
    }

    private static double farFromMainPenalty(ABaseLocation baseLocation) {
        return baseLocation.groundDist(Select.mainOrAnyBuilding());
    }

    private static double farFromEnemyBonus(ABaseLocation baseLocation) {
        double closeToEnemyPenalty = 0;

        AUnit enemyBuilding = EnemyUnits.nearestEnemyBuilding();
        if (enemyBuilding != null) {
            closeToEnemyPenalty = baseLocation.groundDist(enemyBuilding);
        }

        return closeToEnemyPenalty;
    }

    private static List<ABaseLocation> nearestBases() {
        return BaseLocations.expansionFreeBaseLocationNearestTo(Select.mainOrAnyBuildingPosition(), 2);
    }
}
