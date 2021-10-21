
package atlantis.dedicated.protoss;

import atlantis.AGame;
import atlantis.combat.squad.Squad;
import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import atlantis.units.Units;
import atlantis.units.actions.UnitActions;
import bwapi.TechType;

public class ProtossHighTemplar {

    public static boolean update(AUnit highTemplar) {
        if (AGame.everyNthGameFrame(5) && handlePsionic(highTemplar)) {
            System.err.println("STORM (" + highTemplar.energy() + ")");
            return true;
        }

        return followArmy(highTemplar);
    }

    // =========================================================

    private static boolean handlePsionic(AUnit highTemplar) {
        if (highTemplar.energy() < 75) {
            return false;
        }

        AUnit condensedEnemy;
        if ((condensedEnemy = veryCondensedEnemy(highTemplar, false)) != null) {
            return usePsionic(highTemplar, condensedEnemy);
        }

        AUnit enemyCrucialUnit;
        if ((enemyCrucialUnit = enemyCrucialUnit(highTemplar)) != null) {
            return usePsionic(highTemplar, enemyCrucialUnit);
        }

        return actWhenAlmostDead(highTemplar);
    }

    private static boolean usePsionic(AUnit highTemplar, AUnit condensedEnemy) {
        return highTemplar.useTech(TechType.Psionic_Storm, condensedEnemy);
    }

    private static AUnit veryCondensedEnemy(AUnit highTemplar, boolean forceUsage) {
        Units condensedEnemies = new Units();
        for (AUnit enemy : Select.enemyRealUnits().inRadius(forceUsage ? 14 : 8.8, highTemplar).list()) {
            condensedEnemies.addUnitWithValue(enemy, Select.enemyRealUnits().inRadius(3.3, enemy).count());
        }
        AUnit mostCondensedEnemy = condensedEnemies.unitWithHighestValue();

        if (mostCondensedEnemy != null) {
            int most = (int) condensedEnemies.valueFor(mostCondensedEnemy);
            highTemplar.setTooltip("Psionic?(" + most + ")");

            if (most >= 4 || forceUsage) {
                return mostCondensedEnemy;
            }
        }

        return null;
    }

    private static AUnit enemyCrucialUnit(AUnit highTemplar) {
        return Select.enemy().ofType(
            AUnitType.Protoss_Reaver,
            AUnitType.Terran_Siege_Tank_Tank_Mode,
            AUnitType.Terran_Siege_Tank_Siege_Mode
        ).inRadius(15, highTemplar).nearestTo(highTemplar);
    }

    private static boolean actWhenAlmostDead(AUnit highTemplar) {
        if (highTemplar.hp() <= 31) {
            AUnit condensedEnemy;
            if ((condensedEnemy = veryCondensedEnemy(highTemplar, true)) != null) {
                return usePsionic(highTemplar, condensedEnemy);
            }
        }

        return false;
    }

    private static boolean followArmy(AUnit highTemplar) {
        APosition center = Squad.getAlphaSquad().center();
        if (center != null) {
            if (Select.our().inRadius(0.5, highTemplar).atLeast(3)) {
                return highTemplar.moveAwayFrom(
                        Select.our().exclude(highTemplar).nearestTo(highTemplar),
                        1,
                        "Stacked"
                );
            }

            if (center.distTo(highTemplar) > 1) {
                highTemplar.move(center, UnitActions.MOVE, "Follow army");
                return true;
            }
        }

        return false;
    }

}
