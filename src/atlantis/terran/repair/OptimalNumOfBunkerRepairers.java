package atlantis.terran.repair;

import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;
import atlantis.util.cache.Cache;

public class OptimalNumOfBunkerRepairers {
    private static final Cache<Integer> cacheInt = new Cache<>();
    private static int enemiesNear;
    private static int enemiesFar;
    private static int enemiesTotal;

    public static int forBunker(AUnit bunker) {
        return cacheInt.get(
            "forBunker:" + bunker.id(),
            2,
            () -> define(bunker)
        );
    }

    private static int define(AUnit bunker) {
        Selection potentialEnemies = Select.enemy().combatUnits().inRadius(20, bunker);

        if (potentialEnemies.empty()) {
            return 0;
        }

        enemiesNear = potentialEnemies.inRadius(7, bunker).count();
        enemiesFar = potentialEnemies.count() - enemiesNear;
        enemiesTotal = enemiesFar + enemiesFar;

//                System.out.println("enemiesNear = " + enemiesNear + " / enemiesFar = " + enemiesFar);

        int nooneInside = whenAlmostNooneInside(bunker);
        if (nooneInside != -1) {
            return nooneInside;
        }

        if (thereIsFewAttackers(bunker)) {
            return 1;
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
        if (bunker.hp() < 220) optimalNumber += 1;
        if (bunker.hp() < 160) optimalNumber += 1;
        if (bunker.hp() < 130) optimalNumber += 1;

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

        optimalNumber = Math.min(enemiesTotal, optimalNumber);

        // === When nearly no minerals left ========================

        if (A.seconds() >= 500) {
            if (optimalNumber > 1) {
                if (!A.hasMinerals(5)) {
                    return (int) Math.min(optimalNumber, 3);
                }
                else if (!A.hasMinerals(1)) {
                    return (int) Math.min(optimalNumber, 2);
                }
            }
        }

        if (!A.hasMinerals(1)) {
            return Math.max(2, Math.min(4, (int) optimalNumber));
        }

        // =========================================================

        return Math.min(7, (int) Math.floor(optimalNumber));
    }

    private static boolean thereIsFewAttackers(AUnit bunker) {
        return bunker.hp() >= 290 && enemiesNear <= 1 && enemiesFar <= 1;
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

    private static int whenAlmostNooneInside(AUnit bunker) {
        if (enemiesNear == 0 && enemiesFar == 0) return A.seconds() <= 300 ? 1 : 0;

        if ((enemiesFar + enemiesNear) >= 5 && bunker.loadedUnits().size() > 0) return 2;

        if (
            bunker.loadedUnits().size() <= 0
                && Select.ourOfType(AUnitType.Terran_Marine).inRadius(9, bunker).atMost(1)
        ) return 0;

        if (
            bunker.loadedUnits().size() <= 1
                && bunker.friendsNear().terranInfantryWithoutMedics().inRadius(6, bunker).atMost(1)
        ) return 1;

        return -1;
    }
}
