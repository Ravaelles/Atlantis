package atlantis.repair;

import atlantis.AGame;
import atlantis.buildings.managers.TerranFlyingBuildingManager;
import atlantis.combat.micro.ARunManager;
import atlantis.combat.squad.missions.Missions;
import atlantis.information.AMap;
import atlantis.scout.AScoutManager;
import atlantis.strategy.AEnemyStrategy;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import bwta.Chokepoint;
import java.util.Iterator;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class ARepairCommander {

    public static void update() {
        if (AGame.getTimeFrames() % 21 == 0) {
            assignProtectors();
        }

        if (AGame.getTimeFrames() % 15 == 0) {
            assignRepairersToWoundedUnits();
        }

        // =========================================================

        for (Iterator<AUnit> iterator = ARepairManager.getProtectors().iterator(); iterator.hasNext();) {
            AUnit bunkerProtector = iterator.next();
            if (!bunkerProtector.isAlive()) {
                ARepairManager.removeRepairerOrProtector(bunkerProtector);
                iterator.remove();
            }
            ARepairManager.updateProtector(bunkerProtector);
        }

        for (Iterator<AUnit> iterator = ARepairManager.getRepairers().iterator(); iterator.hasNext();) {
            AUnit unitRepairer = iterator.next();
            if (!unitRepairer.isAlive()) {
                ARepairManager.removeRepairerOrProtector(unitRepairer);
                iterator.remove();
            }
            ARepairManager.updateRepairer(unitRepairer);
        }
    }

    // === Asign repairers if needed =============================
    
    private static void assignRepairersToWoundedUnits() {
        
        for (AUnit woundedUnit : Select.our().repairable(true).listUnits()) {

            // Some units shouldn't be repaired
            if (AScoutManager.isScout(woundedUnit) || TerranFlyingBuildingManager.isFlyingBuilding(woundedUnit)) {
                continue;
            }

            // =========================================================
            
            int numberOfRepairers = ARepairManager.countRepairersForUnit(woundedUnit)
                    + ARepairManager.countProtectorsFor(woundedUnit);

            // === Repair bunker ========================================
            
            if (woundedUnit.type().isBunker()) {
                int shouldHaveThisManyRepairers = defineOptimalRepairersForBunker(woundedUnit);
                assignProtectorsFor(woundedUnit, shouldHaveThisManyRepairers - numberOfRepairers);
            } 

            // === Repair ordinary unit =================================
            else {
                assignUnitRepairers(woundedUnit, 2 - numberOfRepairers);
            }
        }
    }

    private static void assignProtectors() {
        if (Missions.isGlobalMissionAttack()) {
            
            // Release all bunker protectors
            for (AUnit bunkerProtector : ARepairManager.getProtectors()) {
                ARepairManager.removeRepairerOrProtector(bunkerProtector);
            }
            
            assignUnitsProtectorsIfNeeded();
        }
        else if (Missions.isGlobalMissionDefend()) {
            assignBunkerProtectorsIfNeeded();
        }
    }
    
    private static void assignBunkerProtectorsIfNeeded() {
        if (Missions.isGlobalMissionAttack()) {
        }

        // =========================================================
        Select<AUnit> bunkers = Select.ourOfType(AUnitType.Terran_Bunker);
//        int bunkersCounter = bunkers.count();
        // Assign two repairers to a bunker if it's not surrounded by many of our combat units
//        if (bunkersCounter == 1) {
//        for (AUnit bunker : bunkers.list()) {
        Chokepoint chokepointForNaturalBase = AMap.getChokepointForNaturalBase();
        if (chokepointForNaturalBase != null) {
            AUnit bunker = bunkers.nearestTo(chokepointForNaturalBase.getCenter());
            if (bunker == null) {
                return;
            }
                    
            int numberOfCombatUnitsNearby = Select.ourCombatUnits().inRadius(6, bunker).count();
            if (numberOfCombatUnitsNearby <= 7) {
                int numberOfRepairersAssigned = ARepairManager.countProtectorsFor(bunker);
                assignProtectorsFor(
                        bunker, defineOptimalNumberOfBunkerProtectors() - numberOfRepairersAssigned
                );
            }
        }
//        }
//        }
    }
    
    private static void assignUnitsProtectorsIfNeeded() {
        if (!Missions.isGlobalMissionAttack()) {
            return;
        }

        // === Protect Vulture =================================
        
        int vultures = Select.countOurOfType(AUnitType.Terran_Vulture);
        if (vultures >= 2) {
            AUnit firstVulture = Select.ourOfType(AUnitType.Terran_Vulture).first();
            int protectors = ARepairManager.countProtectorsFor(firstVulture);
            int lackingProtectors = 2 - protectors;
            if (lackingProtectors > 0) {
                assignProtectorsFor(firstVulture, lackingProtectors);
                return;
            }
        }
        
        // === Protect Tank ====================================
        
        int tanks = Select.ourTanks().count();
        if (tanks >= 2) {
            AUnit firstTank = Select.ourTanks().first();
            int protectors = ARepairManager.countProtectorsFor(firstTank);
            int lackingProtectors = 2 - protectors;
            if (lackingProtectors > 0) {
                assignProtectorsFor(firstTank, lackingProtectors);
                return;
            }
        }
    }

    // =========================================================
    
    private static void assignProtectorsFor(AUnit unitToProtect, int numberOfProtectorsToAssign) {
        for (int i = 0; i < numberOfProtectorsToAssign; i++) {
            AUnit worker = defineBestRepairerFor(unitToProtect, false);
            if (worker != null) {
                ARepairManager.addProtector(worker, unitToProtect);
            }
        }
    }

    private static void assignUnitRepairers(AUnit unitToRepair, int numberOfRepairersToAssign) {
        for (int i = 0; i < numberOfRepairersToAssign; i++) {
            AUnit worker = defineBestRepairerFor(unitToRepair, true);
            if (worker != null) {
                ARepairManager.addRepairer(worker, unitToRepair);
            }
        }
    }

    // =========================================================
    
    private static int defineOptimalNumberOfBunkerProtectors() {

        // === Mission DEFEND  =================================
        if (Missions.isGlobalMissionDefend()) {
            if (AGame.playsAsTerran()) {

                // === We know enemy strategy ========================================
                if (AEnemyStrategy.isEnemyStrategyKnown()) {
                    int repairersWhenRush = 1 
                            + (AGame.getTimeSeconds() > 180 ? 1 : 0)
                            + (AGame.getTimeSeconds() > 200 ? 1 : 0);
                    
                    if (AEnemyStrategy.getEnemyStrategy().isGoingCheese()) {
                        return repairersWhenRush 
                                + 2
                                + (AGame.getTimeSeconds() > 210 ? 1 : 0);
                    }
                    if (AEnemyStrategy.getEnemyStrategy().isGoingRush()) {
                        return repairersWhenRush;
                    }
                } 
                
                // === We don't know enemy strategy ==================================
                else {
                    int enemyRaceBonus = !AGame.isEnemyTerran() && AGame.getTimeSeconds() > 175 ? 1 : 0;
                    return 1 + (AGame.getTimeSeconds() > 230 ? 1 : 0) + enemyRaceBonus;
                }
            } 

            // === Only Terran can repair buildings ==================================
            else {
                return 0;
            }
        } 

        return 0;
    }

    private static int defineOptimalRepairersForBunker(AUnit bunker) {
        int enemiesNearby = Select.enemy().combatUnits().inRadius(10, bunker).count();
        double optimalNumber;

        if (AGame.isEnemyProtoss()) {
            optimalNumber = enemiesNearby * 1;
        } else if (AGame.isEnemyTerran()) {
            optimalNumber = enemiesNearby * 0.5;
        } else {
            optimalNumber = enemiesNearby * 0.5;
        }

        if (bunker.getHP() < 100) {
            optimalNumber += 2;
        }

        return Math.min(7, (int) Math.ceil(optimalNumber));
    }

    private static AUnit defineBestRepairerFor(AUnit unitToRepair, boolean criticallyImportant) {
        if (criticallyImportant) {
            return Select.ourWorkers().notRepairing().notConstructing().notScout().nearestTo(unitToRepair);
        } else {
            return Select.ourWorkers().notCarrying().notRepairing().notConstructing().notScout().nearestTo(unitToRepair);
        }
    }

}
