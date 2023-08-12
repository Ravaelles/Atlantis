package atlantis.terran.repair;

import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;
import atlantis.util.cache.Cache;

public class OptimalNumOfBunkerRepairers {

    private static Cache<Integer> cacheInt = new Cache<>();

    public static int forBunker(AUnit bunker) {
        return cacheInt.get(
            "forBunker:" + bunker.id(),
            2,
            () -> {
                Selection potentialEnemies = Select.enemy().combatUnits().inRadius(26, bunker);

                if (potentialEnemies.empty()) {
                    return 0;
                }

                int enemiesNear = potentialEnemies.inRadius(13, bunker).count();
                int enemiesFar = potentialEnemies.count() - enemiesNear;
                double optimalNumber = 0;

                if (thereIsAlmostNooneInside(bunker)) {
                    if (enemiesNear == 0 && enemiesFar == 0) {
                        return A.seconds() <= 300 ? 1 : 0;
                    }
                    if ((enemiesFar + enemiesNear) >= 5 && bunker.loadedUnits().size() > 0) {
                        return 1;
                    }
                }

                // against PROTOSS
                if (Enemy.protoss()) {
                    optimalNumber = enemiesNear * 1.8 + enemiesFar * 0.75;
                }
                // against TERRAN
                else if (Enemy.terran()) {
                    optimalNumber = enemiesNear * 0.38 + enemiesFar * 0.1;
                }
                // against ZERG
                else if (Enemy.zerg()) {
                    optimalNumber = enemiesNear * 0.65 + enemiesFar * 0.5;
                }

                if (bunker.hp() < 300) {
                    optimalNumber += 2;
                }

                Selection enemiesVeryNear = potentialEnemies.inRadius(4, bunker);
                if (
                    enemiesVeryNear.atMost(2) && thereIsAnotherBunkerNearbyThatIsInBiggerDanger(bunker)
                ) {
                    optimalNumber = 1 + (bunker.isHealthy() ? 0 : (bunker.woundPercent() / 25));
                }

                if (enemiesVeryNear.empty() && potentialEnemies.ranged().atMost(1)) {
                    optimalNumber = Math.min(bunker.isHealthy() ? 0 : 1, optimalNumber);
                }

                if (optimalNumber > 1) {
                    if (!A.hasMinerals(7)) {
                        return 2;
                    }
                    else if (!A.hasMinerals(1)) {
                        return 1;
                    }
                }

                if (!A.hasMinerals(2)) {
                    return Math.max(2, Math.min(4, (int) optimalNumber));
                }

                return Math.min(7, (int) Math.floor(optimalNumber));
            });
    }

    private static boolean thereIsAnotherBunkerNearbyThatIsInBiggerDanger(AUnit bunker) {
        AUnit otherBunker = Select.ourOfType(AUnitType.Terran_Bunker).inRadius(12, bunker).nearestTo(bunker);
        if (otherBunker == null) return false;

        int radius = 9;

        int thisBunkerEnemies = bunker.enemiesNearInRadius(radius);
        int otherBunkerEnemies = otherBunker.enemiesNearInRadius(radius);
        if (otherBunkerEnemies >= 2 && thisBunkerEnemies < otherBunkerEnemies) return true;

        return false;
    }

    private static boolean thereIsAlmostNooneInside(AUnit bunker) {
        return bunker.loadedUnits().size() <= 1
            && bunker.friendsNear().terranInfantryWithoutMedics().inRadius(6, bunker).atMost(1);
    }
}
