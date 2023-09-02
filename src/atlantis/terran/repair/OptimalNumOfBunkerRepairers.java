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

    private static final Cache<Integer> cacheInt = new Cache<>();

    public static int forBunker(AUnit bunker) {
        return cacheInt.get(
            "forBunker:" + bunker.id(),
            2,
            () -> {
                Selection potentialEnemies = Select.enemy().combatUnits().inRadius(20, bunker);

                if (potentialEnemies.empty()) {
                    return 0;
                }

                int enemiesNear = potentialEnemies.inRadius(13, bunker).count();
                int enemiesFar = potentialEnemies.count() - enemiesNear;

//                System.out.println("enemiesNear = " + enemiesNear + " / enemiesFar = " + enemiesFar);

                if (thereIsAlmostNooneInside(bunker)) {
                    if (enemiesNear == 0 && enemiesFar == 0) {
                        return A.seconds() <= 300 ? 1 : 0;
                    }
                    if ((enemiesFar + enemiesNear) >= 5 && bunker.loadedUnits().size() > 0) {
                        return 1;
                    }
                }

                double optimalNumber;

                // against PROTOSS
                if (Enemy.protoss()) {
                    optimalNumber = enemiesNear * 1.8 + enemiesFar * 0.85;
                }
                // against TERRAN
                else if (Enemy.terran()) {
                    optimalNumber = enemiesNear * 0.38 + enemiesFar * 0.1;
                }
                // against ZERG
                else {
                    optimalNumber = enemiesNear * 0.65 + enemiesFar * 0.5;
                }

                if (bunker.hp() < 310) optimalNumber += 1;
                if (bunker.hp() < 250) optimalNumber += 1;
                if (bunker.hp() < 200) optimalNumber += 1;
                if (bunker.hp() < 150) optimalNumber += 1;

                // === Two bunkers near each other =========================

                Selection enemiesVeryNear = potentialEnemies.inRadius(4, bunker);
                if (
                    enemiesNear <= 2
                        && enemiesVeryNear.atMost(2)
                        && thereIsAnotherBunkerNearbyThatIsInBiggerDanger(bunker)
                ) {
                    optimalNumber = 1 + (bunker.isHealthy() ? 0 : (bunker.woundPercent() / 25));
                }

                // =========================================================

                if (enemiesVeryNear.empty() && potentialEnemies.ranged().atMost(1)) {
                    optimalNumber = Math.min(1, optimalNumber);
                }

                // === When nearly no minerals left ========================

                if (A.seconds() >= 500) {
                    if (optimalNumber > 1) {
                        if (!A.hasMinerals(5)) {
                            return Math.min(optimalNumber, 3);
                        }
                        else if (!A.hasMinerals(1)) {
                            return Math.min(optimalNumber, 2);
                        }
                    }
                }

                if (!A.hasMinerals(1)) {
                    return Math.max(2, Math.min(4, (int) optimalNumber));
                }

                // =========================================================

                return Math.min(7, (int) Math.floor(optimalNumber));
            });
    }

    private static boolean thereIsAnotherBunkerNearbyThatIsInBiggerDanger(AUnit bunker) {
        AUnit otherBunker = Select.ourOfType(AUnitType.Terran_Bunker)
            .exclude(bunker)
            .inRadius(12, bunker)
            .nearestTo(bunker);
        if (otherBunker == null) return false;

        int radius = 9;

        int thisBunkerEnemies = bunker.enemiesNearInRadius(radius);
        int otherBunkerEnemies = otherBunker.enemiesNearInRadius(radius);
        return otherBunkerEnemies >= 2 && thisBunkerEnemies < otherBunkerEnemies;
    }

    private static boolean thereIsAlmostNooneInside(AUnit bunker) {
        return bunker.loadedUnits().size() <= 1
            && bunker.friendsNear().terranInfantryWithoutMedics().inRadius(6, bunker).atMost(1);
    }
}
