package atlantis.protoss.ht;

import atlantis.information.tech.SpellCoordinator;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.HasUnit;
import atlantis.units.Units;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;
import bwapi.TechType;

import java.util.List;

public class UsePsionicStorm extends HasUnit {
    public UsePsionicStorm(AUnit unit) {
        super(unit);
    }

    protected boolean handle() {
        if (unit.energy() < 75) {
            unit.setTooltipTactical("NoEnergy");
            return false;
        }

        AUnit enemyToPsionic;
        if ((enemyToPsionic = veryCondensedEnemy(false)) != null) {
            return usePsionic(enemyToPsionic);
        }

        if ((enemyToPsionic = enemyCrucialUnit()) != null) {
            return usePsionic(enemyToPsionic);
        }

        if ((enemyToPsionic = enemyImportantUnit()) != null) {
            return usePsionic(enemyToPsionic);
        }

        return actWhenAlmostDead();
    }

    private boolean usePsionic(AUnit enemy) {
        if (SpellCoordinator.noOtherSpellAssignedHere(enemy.position(), TechType.Psionic_Storm)) {
            if (!enemy.isUnderStorm()) {
                APosition finalPosition = enemy.friendsNear().groundUnits().combatUnits().inRadius(3.1, enemy).center();
                if (finalPosition != null) {
                    unit.useTech(TechType.Psionic_Storm, finalPosition);
                    return true;
                }
            }
        }
//        else {
//            System.err.println(
//                    "On " + A.now() + " " + unit.idWithHash() + "'s psionic was BLOCKED by another cast!"
//            );
//        }

        return false;
    }

    private AUnit veryCondensedEnemy(boolean forceUsage) {
        Units condensedEnemies = new Units();
        Selection realEnemies = unit.enemiesNear().realUnits();
        for (AUnit enemy : realEnemies.inRadius(forceUsage ? 9.3 : 8.8, unit).list()) {
            if (!enemy.isUnderStorm()) {
                Selection targets = realEnemies.inRadius(3.3, enemy);
                condensedEnemies.addUnitWithValue(enemy, targets.count());
            }
        }
        AUnit mostCondensedEnemy = condensedEnemies.unitWithHighestValue();

        if (mostCondensedEnemy != null) {
            int most = (int) condensedEnemies.valueFor(mostCondensedEnemy);
            unit.setTooltipTactical("Psionic?(" + most + " enemies)");

            int minUnitsInOnePlace = minUnitsInOnePlaceToUsePsionic();
            if (most >= minUnitsInOnePlace || forceUsage) {
                return mostCondensedEnemy;
            }
        }

        return null;
    }

    private int minUnitsInOnePlaceToUsePsionic() {
        int base = Enemy.protoss() ? 3 : 4;
        int energy = unit.energy();

        if (energy >= 195) return 1;
        if (energy >= 180) return 2;

        return energy >= 110 ? (base - 1) : base;
    }

    private AUnit enemyCrucialUnit() {
        List<? extends AUnit> enemyCrucialUnits = enemies().ofType(
            AUnitType.Protoss_Reaver,
            AUnitType.Terran_Siege_Tank_Siege_Mode
        ).inRadius(12, unit).sortDataByDistanceTo(unit, false);

        for (AUnit enemy : enemyCrucialUnits) {
            if (Select.ourRealUnits().inRadius(2, enemy).atMost(2)) {
                return enemy;
            }
        }

        return null;
    }

    private static Selection enemies() {
        return Select.enemyRealUnits().excludeOverlords();
    }

    private AUnit enemyImportantUnit() {
        List<? extends AUnit> enemyCrucialUnits = enemies().ofType(
            AUnitType.Protoss_Carrier,
            AUnitType.Protoss_Reaver,
            AUnitType.Terran_Science_Vessel,
            AUnitType.Terran_Siege_Tank_Tank_Mode,
            AUnitType.Terran_Siege_Tank_Siege_Mode,
            AUnitType.Zerg_Defiler
        ).inRadius(8.9, unit).sortDataByDistanceTo(unit, false);

        for (AUnit enemy : enemyCrucialUnits) {
            if (
                Select.ourRealUnits().inRadius(2, enemy).atMost(2)
                    && enemies().inRadius(3, enemy).atLeast(2)
            ) {
                return enemy;
            }
        }

        return null;
    }

    private boolean actWhenAlmostDead() {
        if (unit.hp() <= 31 && unit.energy() >= 75) {
            AUnit condensedEnemy;
            if ((condensedEnemy = veryCondensedEnemy(true)) != null) {
                return usePsionic(condensedEnemy);
            }
        }

        return false;
    }
}
