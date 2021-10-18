package atlantis.repair;

import atlantis.AGame;
import atlantis.combat.missions.Missions;
import atlantis.map.AChokepoint;
import atlantis.map.AMap;
import atlantis.strategy.AEnemyStrategy;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;

import java.util.*;


public class ARepairCommander {

    public static void update() {
        if (AGame.everyNthGameFrame(20)) {
            assignProtectors();
        }
        if (AGame.everyNthGameFrame(15)) {
            ARepairerManager.assignRepairersToWoundedUnits();
        }

        // === Handle bunker protectors =================================

        handleBunkerProtectors();
        
        // === Handle normal repairers ==================================

        handleRepairers();
    }

    // =========================================================

    public static void handleRepairers() {
        for (Iterator<AUnit> iterator = ARepairAssignments.getRepairers().iterator(); iterator.hasNext();) {
            AUnit repairer = iterator.next();
            if (!repairer.isAlive()) {
                ARepairAssignments.removeRepairerOrProtector(repairer);
                iterator.remove();
            }
            ARepairerManager.updateRepairer(repairer);
        }
    }

    public static void handleBunkerProtectors() {
        for (Iterator<AUnit> iterator = ARepairAssignments.getProtectors().iterator(); iterator.hasNext();) {
            AUnit protector = iterator.next();
            if (!protector.isAlive()) {
                ARepairAssignments.removeRepairerOrProtector(protector);
                iterator.remove();
            }
            ABunkerProtectorManager.updateProtector(protector);
        }
    }

    // =========================================================
    // === Asign repairers if needed ===========================
    // =========================================================

    protected static void assignProtectors() {
        if (Missions.isGlobalMissionDefend()) {
            assignBunkerProtectorsIfNeeded();
        }

        else {
            
            // Release all bunker protectors
            for (AUnit bunkerProtector : ARepairAssignments.getProtectors()) {
                ARepairAssignments.removeRepairerOrProtector(bunkerProtector);
            }
            
            assignUnitsProtectorsIfNeeded();
        }

    }
    
    protected static void assignBunkerProtectorsIfNeeded() {
//        if (Missions.isGlobalMissionAttack()) {
//        }
        AUnit mainBase = Select.mainBase();
        if (mainBase == null) {
            return;
        }

        // =========================================================

        Select<AUnit> bunkers = Select.ourOfType(AUnitType.Terran_Bunker);
//        int bunkersCounter = bunkers.count();
        // Assign two repairers to a bunker if it's not surrounded by many of our combat units
//        if (bunkersCounter == 1) {
//        for (AUnit bunker : bunkers.list()) {
        AChokepoint chokepointForNaturalBase = AMap.getChokepointForNaturalBase(mainBase.getPosition());
        if (chokepointForNaturalBase != null) {
            AUnit bunker = bunkers.nearestTo(chokepointForNaturalBase.getCenter());
            if (bunker == null) {
                return;
            }
                    
            int numberOfCombatUnitsNearby = Select.ourCombatUnits().inRadius(6, bunker).count();
            if (numberOfCombatUnitsNearby <= 7) {
                int numberOfRepairersAssigned = ARepairAssignments.countProtectorsFor(bunker);
                assignProtectorsFor(
                        bunker, defineOptimalNumberOfBunkerProtectors() - numberOfRepairersAssigned
                );
            }
        }
//        }
//        }
    }
    
    protected static void assignUnitsProtectorsIfNeeded() {
        if (!Missions.isGlobalMissionAttack()) {
            return;
        }

        // === Protect Vulture =================================
        
        int vultures = Select.countOurOfType(AUnitType.Terran_Vulture);
        if (vultures >= 2) {
            AUnit firstVulture = Select.ourOfType(AUnitType.Terran_Vulture).first();
            int protectors = ARepairAssignments.countProtectorsFor(firstVulture);
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
            int protectors = ARepairAssignments.countProtectorsFor(firstTank);
            int lackingProtectors = 2 - protectors;
            if (lackingProtectors > 0) {
                assignProtectorsFor(firstTank, lackingProtectors);
                return;
            }
        }
    }

    // =========================================================
    
    protected static void assignProtectorsFor(AUnit unitToProtect, int numberOfProtectorsToAssign) {
        for (int i = 0; i < numberOfProtectorsToAssign; i++) {
            AUnit worker = ARepairerManager.defineBestRepairerFor(unitToProtect, false);
            if (worker != null) {
                ARepairAssignments.addProtector(worker, unitToProtect);
            }
        }
    }

    // =========================================================
    
    protected static int defineOptimalNumberOfBunkerProtectors() {

        // === Mission DEFEND  =================================
        if (Missions.isGlobalMissionDefend()) {
            if (AGame.isPlayingAsTerran()) {

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

    protected static int defineOptimalRepairersForBunker(AUnit bunker) {
        int enemiesNearby = Select.enemy().combatUnits().inRadius(10, bunker).count();
        double optimalNumber;

        if (AGame.isEnemyProtoss()) {
            optimalNumber = enemiesNearby;
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

}
