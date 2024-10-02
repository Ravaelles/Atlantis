package atlantis.combat.eval.protoss;

import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.util.Enemy;

public class ProtossEvaluateAgainstCombatBuildings {
    public static boolean chancesLookGood(AUnit unit, HasPosition position) {
//        System.out.println("OUR / ENEMY (" + (ourUnitsStrength(unit) - enemyStrength(unit, position)) + ") // " +
//            "OUR: " + ourUnitsStrength(unit) + " / ENEMY: " + enemyStrength(unit, position));
        return unit.combatEvalRelative() >= 1.7
            || ourUnitsStrength(unit) >= enemyStrength(unit, position);
    }

//    public static boolean looksGoodAgainstThirdOrLaterEnemyBase(AUnit unit, APosition enemyBase) {
//        return unit.combatEvalRelative() >= 1.5
//            || ourUnitsStrength(unit) <= enemyStrength(unit, enemyBase);
//    }

    // =========================================================

    private static int ourUnitsStrength(AUnit unit) {
//        return unit.friendsNear().combatUnits().notRunning().havingAtLeastHp(23).count();
        return unit.friendsNear().combatUnits().notRunning().havingAtLeastHp(23).totalHp();
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
