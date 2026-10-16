package atlantis.combat.eval.protoss;

import atlantis.game.A;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.select.Selection;
import atlantis.game.player.Enemy;

public class ProtossEvaluateAgainstCombatBuildings {
    public static boolean chancesLookGood(AUnit unit, AUnit enemy) {
//        System.out.println("OUR / ENEMY (" + (ourUnitsStrength(unit) - enemyStrength(unit, position)) + ") // " +
//            "OUR: " + ourUnitsStrength(unit) + " / ENEMY: " + enemyStrength(unit, position));
//        return unit.eval() >= 1.2

        if (A.supplyUsed() >= 185 && A.minerals() >= 700) return true;
//        if (unit.eval() <= 1.2) return false;

        return (
            moreOurUnitsThanBuildings(unit, enemy)
                || (ourUnitsStrength(unit) >= enemyStrength(unit, enemy))
        ) && OurVsEnemyUnitsCount.oursToEnemiesRatio(unit, enemy) >= 1.5;
    }

//    public static boolean looksGoodAgainstThirdOrLaterEnemyBase(AUnit unit, APosition enemyBase) {
//        return unit.combatEvalRelative() >= 1.5
//            || ourUnitsStrength(unit) <= enemyStrength(unit, enemyBase);
//    }

    // =========================================================

    private static boolean moreOurUnitsThanBuildings(AUnit unit, AUnit enemy) {
        int ourCombat = ourCombatUnits(unit).count();

        return (A.s >= 60 * 8 || ourCombat >= 10)
            && ourCombat * 7 >= enemy.enemiesNear().combatBuildingsAnti(enemy).count();
    }

    // =========================================================

    private static int ourUnitsStrength(AUnit unit) {
//        return unit.friendsNear().combatUnits().notRunning().havingAtLeastHp(23).count();
        return ourCombatUnits(unit).totalHp();
    }

    private static Selection ourCombatUnits(AUnit unit) {
        return unit.friendsNear().combatUnits().notRunning().havingAtLeastHp(23);
    }

    // =========================================================

    private static double enemyStrength(AUnit unit, HasPosition enemyBase) {
        return enemyBuildingsStrength(unit) + enemyCombatUnitsStrength(unit, enemyBase);
    }

    private static double enemyBuildingsStrength(AUnit unit) {
//        return 5 * unit.enemiesNear().combatBuildingsAnti(unit).count();
        return 0.8 * unit.enemiesNear().combatBuildingsAnti(unit).totalHp();
    }

    private static double enemyCombatUnitsStrength(AUnit unit, HasPosition enemyBase) {
//        return unit.enemiesNear().combatUnits().nonBuildings().count() * raceModifier();
        return unit.enemiesNear().combatUnits().nonBuildings().totalHp();
    }

    private static double raceModifier() {
        switch (Enemy.race()) {
            case Protoss:
                return 1;
            case Terran:
                return 0.7;
            case Zerg:
                return 0.5;
            default:
                return 1;
        }
    }
}
