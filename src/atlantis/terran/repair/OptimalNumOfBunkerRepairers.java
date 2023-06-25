package atlantis.terran.repair;

import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;

public class OptimalNumOfBunkerRepairers {

    public static int optimalRepairersForBunker(AUnit bunker) {
        if (thereIsNooneInsideTheBunker(bunker)) {
            return 0;
        }

        Selection potentialEnemies = Select.enemy().combatUnits().inRadius(18, bunker);

        if (potentialEnemies.empty()) {
            return 0;
        }

        int enemiesNear = potentialEnemies.inRadius(10, bunker).count();
        int enemiesFar = potentialEnemies.count() - enemiesNear;
        double optimalNumber = 0;

        if (Enemy.protoss()) {
            optimalNumber = enemiesNear + enemiesFar * 0.3;
        }
        else if (Enemy.terran()) {
            optimalNumber = enemiesNear * 0.38 + enemiesFar * 0.1;
        }
        else if (Enemy.zerg()) {
            optimalNumber = enemiesNear * 0.55 + enemiesFar * 0.25;
        }

        if (bunker.hp() < 250) {
            optimalNumber += 2;
        }

        Selection enemiesVeryNear = potentialEnemies.inRadius(4, bunker);
        if (
            enemiesVeryNear.atMost(2)
            && thereIsAnotherBunkerNearbyThatIsInBiggerDanger(bunker)
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

        return Math.min(7, (int) Math.floor(optimalNumber));
    }

    private static boolean thereIsAnotherBunkerNearbyThatIsInBiggerDanger(AUnit bunker) {
        AUnit otherBunker = Select.ourOfType(AUnitType.Terran_Bunker).inRadius(12, bunker).nearestTo(bunker);
        if (otherBunker == null) {
            return false;
        }

        int radius = 9;

        int thisBunkerEnemies = bunker.enemiesNearInRadius(radius);
        int otherBunkerEnemies = otherBunker.enemiesNearInRadius(radius);
        if (otherBunkerEnemies >= 2 && thisBunkerEnemies < otherBunkerEnemies) {
            return true;
        }

        return false;
    }

    private static boolean thereIsNooneInsideTheBunker(AUnit bunker) {
        return bunker.loadedUnits().isEmpty()
            && bunker.friendsNear().terranInfantryWithoutMedics().inRadius(6, bunker).atMost(1);
    }
}
