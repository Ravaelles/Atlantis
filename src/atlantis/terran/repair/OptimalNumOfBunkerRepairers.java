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

        int enemiesVeryNear = potentialEnemies.inRadius(10, bunker).count();
        int enemiesQuiteFar = potentialEnemies.count() - enemiesVeryNear;
        double optimalNumber = 0;

        if (Enemy.protoss()) {
            optimalNumber = enemiesVeryNear + enemiesQuiteFar * 0.2;
        }
        else if (Enemy.terran()) {
            optimalNumber = enemiesVeryNear * 0.38 + enemiesQuiteFar * 0.1;
        }
        else if (Enemy.zerg()) {
            optimalNumber = enemiesVeryNear * 0.4 + enemiesQuiteFar * 0.15;
        }

        if (bunker.hp() < 250) {
            optimalNumber += 2;
        }

        if (
            potentialEnemies.inRadius(4, bunker).atMost(2)
            && thereIsAnotherBunkerNearbyThatIsInBiggerDanger(bunker)
        ) {
            optimalNumber = 1 + (bunker.isHealthy() ? 0 : (bunker.woundPercent() / 25));
        }

        return Math.min(A.hasMinerals(20) ? 7 : 3, (int) Math.ceil(optimalNumber));
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
