
package atlantis.dedicated.protoss;

import atlantis.AGame;
import atlantis.combat.micro.avoid.AAvoidUnits;
import atlantis.combat.squad.Squad;
import atlantis.position.APosition;
import atlantis.tech.SpellCoordinator;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import atlantis.units.Units;
import atlantis.units.actions.UnitActions;
import atlantis.util.A;
import bwapi.TechType;

import java.util.List;

public class ProtossHighTemplar {

    public static boolean update(AUnit highTemplar) {
        if (dontDisturb(highTemplar)) {
            return true;
        }

        if (tryMeldingIntoArchon(highTemplar)) {
            return true;
        }

        if (AGame.everyNthGameFrame(3)) {
            if (handlePsionic(highTemplar)) {
                System.err.println(
                        "On " + A.now() + " " +
                        highTemplar.idWithHash() + " casts PSIONIC at " + highTemplar.lastTechPosition()
                        + " " + A.dist(highTemplar, highTemplar.lastTechPosition())
                );
                return true;
            }
        }

        if (AAvoidUnits.avoidEnemiesIfNeeded(highTemplar)) {
            return true;
        }

        return followArmy(highTemplar);
    }

    // =========================================================

    private static boolean dontDisturb(AUnit highTemplar) {

        // Wants to Warp Archon
        if (
                highTemplar.lastTechUsed() != null
                && highTemplar.lastActionLessThanAgo(50, UnitActions.USING_TECH)
                && TechType.Archon_Warp.name().equals(highTemplar.lastTechUsed().name())
                && highTemplar.lastTechUnit().isAlive()
        ) {
            highTemplar.setTooltip("Sex & Archon");
            return true;
        }

        // Is target of Archon Warp
        for (AUnit otherHT : Select.ourOfType(AUnitType.Protoss_High_Templar).inRadius(3, highTemplar).list()) {
            if (highTemplar.equals(otherHT.getTarget()) && highTemplar.lastTechUsedAgo() <= 90) {
                highTemplar.setTooltip("Lets get it on");
                return true;
            }
        }

        if (highTemplar.lastActionLessThanAgo(40, UnitActions.USING_TECH)) {
            highTemplar.setTooltip(highTemplar.lastTechUsed().name() + "...");
            return true;
        }

        return false;
    }

    private static boolean handlePsionic(AUnit highTemplar) {
        if (highTemplar.energy() < 75) {
            highTemplar.setTooltip("NoEnergy");
            return false;
        }

        AUnit enemyToPsionic;
        if ((enemyToPsionic = veryCondensedEnemy(highTemplar, false)) != null) {
            return usePsionic(highTemplar, enemyToPsionic);
        }

        if ((enemyToPsionic = enemyCrucialUnit(highTemplar)) != null) {
            return usePsionic(highTemplar, enemyToPsionic);
        }

        if ((enemyToPsionic = enemyImportantUnit(highTemplar)) != null) {
            return usePsionic(highTemplar, enemyToPsionic);
        }

        return actWhenAlmostDead(highTemplar);
    }

    private static boolean usePsionic(AUnit highTemplar, AUnit enemy) {
        if (SpellCoordinator.noOtherSpellAssignedHere(enemy.getPosition(), TechType.Psionic_Storm)) {
            if (!enemy.isUnderStorm()) {
                highTemplar.useTech(TechType.Psionic_Storm, enemy);
                return true;
            }
        }
        else {
//            System.err.println(
//                    "On " + A.now() + " " + highTemplar.idWithHash() + "'s psionic was BLOCKED by another cast!"
//            );
        }

        return false;
    }

    private static AUnit veryCondensedEnemy(AUnit highTemplar, boolean forceUsage) {
        Units condensedEnemies = new Units();
        for (AUnit enemy : Select.enemyRealUnits().inRadius(forceUsage ? 8.8 : 8.8, highTemplar).list()) {
            if (!enemy.isUnderStorm()) {
                condensedEnemies.addUnitWithValue(enemy, Select.enemyRealUnits().inRadius(3.3, enemy).count());
            }
        }
        AUnit mostCondensedEnemy = condensedEnemies.unitWithHighestValue();

        if (mostCondensedEnemy != null) {
            int most = (int) condensedEnemies.valueFor(mostCondensedEnemy);
            highTemplar.setTooltip("Psionic?(" + most + " enemies)");

            int minUnitsInOnePlace = highTemplar.energy() >= 180 ? 5 : 6;
            if (most >= minUnitsInOnePlace || forceUsage) {
                return mostCondensedEnemy;
            }
        }

        return null;
    }

    private static AUnit enemyCrucialUnit(AUnit highTemplar) {
        List<? extends AUnit> enemyCrucialUnits = Select.enemy().ofType(
                AUnitType.Protoss_Reaver,
                AUnitType.Terran_Siege_Tank_Siege_Mode
        ).inRadius(12, highTemplar).sortDataByDistanceTo(highTemplar, false);

        for (AUnit enemy : enemyCrucialUnits) {
            if (Select.ourRealUnits().inRadius(2, enemy).atMost(2)) {
                return enemy;
            }
        }

        return null;
    }

    private static AUnit enemyImportantUnit(AUnit highTemplar) {
        List<? extends AUnit> enemyCrucialUnits = Select.enemy().ofType(
                AUnitType.Protoss_Carrier,
                AUnitType.Protoss_Reaver,
                AUnitType.Terran_Science_Vessel,
                AUnitType.Terran_Siege_Tank_Tank_Mode,
                AUnitType.Terran_Siege_Tank_Siege_Mode,
                AUnitType.Zerg_Defiler
        ).inRadius(8.9, highTemplar).sortDataByDistanceTo(highTemplar, false);

        for (AUnit enemy : enemyCrucialUnits) {
            if (
                    Select.ourRealUnits().inRadius(2, enemy).atMost(2)
                    && Select.enemyRealUnits().inRadius(3, enemy).atLeast(2)
            ) {
                return enemy;
            }
        }

        return null;
    }

    private static boolean actWhenAlmostDead(AUnit highTemplar) {
        if (highTemplar.hp() <= 31 && highTemplar.energy() >= 75) {
            AUnit condensedEnemy;
            if ((condensedEnemy = veryCondensedEnemy(highTemplar, true)) != null) {
                return usePsionic(highTemplar, condensedEnemy);
            }
        }

        return false;
    }

    private static boolean followArmy(AUnit highTemplar) {
        if (highTemplar.hp() <= 16) {
            return false;
        }

        APosition center = Squad.getAlphaSquad().center();
        if (center != null) {
            if (Select.our().inRadius(0.3, highTemplar).atLeast(3)) {
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

    private static boolean tryMeldingIntoArchon(AUnit highTemplar) {
        if (highTemplar.energy() > 65 && highTemplar.woundPercent() < 60) {
            return false;
        }

        Units lowEnergyHTs = new Units();
        for (AUnit other : Select.ourOfType(AUnitType.Protoss_High_Templar).inRadius(8, highTemplar).list()) {
            if (other.energy() <= 70 || highTemplar.woundPercent() >= 60) {
                lowEnergyHTs.addUnitWithValue(other, other.distTo(highTemplar));
            }
        }

        AUnit closestOtherHT = lowEnergyHTs.unitWithLowestValue();
        if (closestOtherHT != null) {
//            if (closestOtherHT.distTo(highTemplar) <= 0.9) {
                highTemplar.useTech(TechType.Archon_Warp, closestOtherHT);
//                System.out.println("Warp Archon");
                highTemplar.setTooltip("WarpArchon");
                closestOtherHT.setTooltip("OhArchon");
//                GameSpeed.changeSpeedTo(10);
//                CameraManager.centerCameraOn(highTemplar);
//            }
//            else {
//                if (!highTemplar.isMoving() && closestOtherHT.lastActionMoreThanAgo(90, UnitActions.USING_TECH)) {
//                    highTemplar.useTech(TechType.Archon_Warp, closestOtherHT);
//                    highTemplar.move(closestOtherHT, UnitActions.MOVE, "WarpArchon");
//                }
//                if (!closestOtherHT.isMoving() && closestOtherHT.lastActionMoreThanAgo(90, UnitActions.USING_TECH)) {
//                    closestOtherHT.useTech(TechType.Archon_Warp, highTemplar);
//                    closestOtherHT.move(highTemplar, UnitActions.MOVE, "WarpArchon");
//                }
//            }
            return true;
        }

        return false;
    }


}
