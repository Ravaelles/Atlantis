package atlantis.repair;

import atlantis.AGame;
import atlantis.combat.missions.Missions;
import atlantis.map.AChoke;
import atlantis.map.Chokes;
import atlantis.strategy.EnemyStrategy;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

import java.util.*;


public class ARepairCommander {

    private static final int MAX_PROTECTORS = 3;

    public static void update() {
        if (AGame.everyNthGameFrame(21)) {
            assignProtectors();
        }
        if (AGame.everyNthGameFrame(15)) {
            RepairerAssigner.assignRepairersToWoundedUnits();
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
                System.err.println("Dead repairer " + repairer.shortName() + " // " + repairer.hp());
                ARepairAssignments.removeRepairerOrProtector(repairer);
                iterator.remove();
                continue;
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
        }

        assignUnitsProtectorsIfNeeded();
    }
    
    protected static void assignBunkerProtectorsIfNeeded() {
        if (Missions.isGlobalMissionAttack()) {
            return;
        }

        AUnit mainBase = Select.main();
        if (mainBase == null) {
            return;
        }

        // =========================================================

        Selection bunkers = Select.ourOfType(AUnitType.Terran_Bunker);
//        int bunkersCounter = bunkers.count();
        // Assign two repairers to a bunker if it's not surrounded by many of our combat units
//        if (bunkersCounter == 1) {
//        for (AUnit bunker : bunkers.list()) {
        AChoke chokepointForNatural = Chokes.natural(mainBase.position());
        if (chokepointForNatural != null) {
            AUnit bunker = bunkers.nearestTo(chokepointForNatural.center());
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
    
    protected static boolean assignUnitsProtectorsIfNeeded() {
        if (removeProtectorsIfNeeded()) {
            return true;
        }

        List<AUnit> tanks = Select.ourTanks().list();
        if (tanks.isEmpty()) {
            return false;
        }

        int totalNow = ARepairAssignments.countTotalProtectors();
        for (int i = 0; i < MAX_PROTECTORS - totalNow; i++) {
            assignProtectorsFor(tanks.get(i % tanks.size()), 1);
        }

        if (ARepairAssignments.countTotalProtectors() == 0) {
            assignProtectorsFor(Select.ourOfType(AUnitType.Terran_Medic).last(), 1);
        }

//        if (!Missions.isGlobalMissionAttack() && !Missions.isGlobalMissionContain()) {
//            return;
//        }
//
//        // === Protect Vulture =================================
//
//        int vultures = Select.countOurOfType(AUnitType.Terran_Vulture);
//        if (vultures >= 2) {
//            AUnit firstVulture = Select.ourOfType(AUnitType.Terran_Vulture).first();
//            int protectors = ARepairAssignments.countProtectorsFor(firstVulture);
//            int lackingProtectors = 2 - protectors;
//            if (lackingProtectors > 0) {
//                assignProtectorsFor(firstVulture, lackingProtectors);
//                return;
//            }
//        }
//
//        // === Protect Tank ====================================
//
//        int tanks = Select.ourTanks().count();
//        if (tanks >= 2) {
//            AUnit firstTank = Select.ourTanks().first();
//            int protectors = ARepairAssignments.countProtectorsFor(firstTank);
//            int lackingProtectors = 2 - protectors;
//            if (lackingProtectors > 0) {
//                assignProtectorsFor(firstTank, lackingProtectors);
//                return;
//            }
//        }
        return false;
    }

    private static boolean removeProtectorsIfNeeded() {
//        System.out.println("PROT = " + ARepairAssignments.countTotalProtectors() + " // " + MAX_PROTECTORS);

        if (ARepairAssignments.countTotalProtectors() >= MAX_PROTECTORS) {
            for (int i = 0; i < ARepairAssignments.countTotalProtectors() - MAX_PROTECTORS; i++) {
                ARepairAssignments.removeRepairerOrProtector(
                        ARepairAssignments.getProtectors().get(ARepairAssignments.getProtectors().size() - 1)
                );
            }
            return true;
        }
        return false;
    }

    // =========================================================
    
    protected static void assignProtectorsFor(AUnit unitToProtect, int numberOfProtectorsToAssign) {
        if (unitToProtect == null) {
            return;
        }

        for (int i = 0; i < numberOfProtectorsToAssign; i++) {
            AUnit worker = ARepairerManager.repairerFor(unitToProtect, false);
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
                if (EnemyStrategy.isEnemyStrategyKnown()) {
                    int repairersWhenRush = 1 
                            + (AGame.timeSeconds() > 180 ? 1 : 0)
                            + (AGame.timeSeconds() > 200 ? 1 : 0);
                    
                    if (EnemyStrategy.get().isGoingCheese()) {
                        return repairersWhenRush 
                                + 2
                                + (AGame.timeSeconds() > 210 ? 1 : 0);
                    }
                    if (EnemyStrategy.get().isRush()) {
                        return repairersWhenRush;
                    }
                } 
                
                // === We don't know enemy strategy ==================================
                else {
                    int enemyRaceBonus = !AGame.isEnemyTerran() && AGame.timeSeconds() > 175 ? 1 : 0;
                    return 1 + (AGame.timeSeconds() > 280 ? 1 : 0) + enemyRaceBonus;
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

        if (bunker.hp() < 100) {
            optimalNumber += 2;
        }

        return Math.min(7, (int) Math.ceil(optimalNumber));
    }

}
