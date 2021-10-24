
package atlantis.dedicated.protoss;

import atlantis.AGame;
import atlantis.combat.micro.avoid.AAvoidUnits;
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
        if (highTemplar.lastActionLessThanAgo(10, UnitActions.USING_TECH)) {
            highTemplar.setTooltip("Storm...");
            return true;
        }

        if (highTemplar.energy() < 50) {
            return tryMergingIntoArchon(highTemplar);
        }

        if (AGame.everyNthGameFrame(5)) {
            if (handlePsionic(highTemplar)) {
                System.err.println("STORM (" + highTemplar.energy() + ")");
                return true;
            }
        }

        if (AAvoidUnits.avoidEnemiesIfNeeded(highTemplar)) {
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
        if ((enemyCrucialUnit = enemyCrucialUnitClose(highTemplar)) != null) {
            return usePsionic(highTemplar, enemyCrucialUnit);
        }
        if ((enemyCrucialUnit = enemyCrucialUnitFar(highTemplar)) != null) {
            return usePsionic(highTemplar, enemyCrucialUnit);
        }

        return actWhenAlmostDead(highTemplar);
    }

    private static boolean usePsionic(AUnit highTemplar, AUnit condensedEnemy) {
        return highTemplar.useTech(TechType.Psionic_Storm, condensedEnemy);
    }

    private static AUnit veryCondensedEnemy(AUnit highTemplar, boolean forceUsage) {
        Units condensedEnemies = new Units();
        for (AUnit enemy : Select.enemyRealUnits().inRadius(forceUsage ? 8.8 : 12.5, highTemplar).list()) {
            if (!enemy.isUnderStorm()) {
                condensedEnemies.addUnitWithValue(enemy, Select.enemyRealUnits().inRadius(3.3, enemy).count());
            }
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

    private static AUnit enemyCrucialUnitClose(AUnit highTemplar) {
        return Select.enemy().ofType(
            AUnitType.Protoss_Reaver,
            AUnitType.Terran_Siege_Tank_Tank_Mode,
            AUnitType.Terran_Siege_Tank_Siege_Mode
        ).inRadius(9, highTemplar).mostDistantTo(highTemplar);
    }

    private static AUnit enemyCrucialUnitFar(AUnit highTemplar) {
        return Select.enemy().ofType(
            AUnitType.Protoss_Reaver,
            AUnitType.Terran_Siege_Tank_Tank_Mode,
            AUnitType.Terran_Siege_Tank_Siege_Mode
        ).inRadius(13, highTemplar).nearestTo(highTemplar);
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

    private static boolean tryMergingIntoArchon(AUnit highTemplar) {
        Units lowEnergyHTs = new Units();

        for (AUnit other : Select.ourOfType(AUnitType.Protoss_High_Templar).inRadius(7, highTemplar).list()) {
            if (other.energy() < 70) {
                lowEnergyHTs.addUnitWithValue(other, other.distTo(highTemplar));
            }
        }

        AUnit closestOtherHT = lowEnergyHTs.unitWithLowestValue();
        if (closestOtherHT != null) {
            if (closestOtherHT.distTo(highTemplar) <= 3) {
                highTemplar.useTech(TechType.Archon_Warp, closestOtherHT);
                closestOtherHT.useTech(TechType.Archon_Warp, highTemplar);
            }
            else {
                highTemplar.move(closestOtherHT, UnitActions.MOVE, "WarpArchon");
                closestOtherHT.move(highTemplar, UnitActions.MOVE, "WarpArchon");
            }
            return true;
        }

        return false;
    }


}
