package atlantis.repair;

import atlantis.AGame;
import atlantis.buildings.managers.TerranFlyingBuildingManager;
import atlantis.combat.squad.missions.Missions;
import atlantis.information.AMap;
import atlantis.scout.AScoutManager;
import atlantis.strategy.AEnemyStrategy;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import bwta.Chokepoint;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class ARepairCommander {

    public static void update() {
        if (AGame.getTimeFrames() % 15 == 0) {
            assignConstantBunkerRepairersIfNeeded();
        }

        if (AGame.getTimeFrames() % 15 == 0) {
            assignUnitRepairersToWoundedUnits();
        }

        // =========================================================
        
        for (AUnit bunkerRepairer : ARepairManager.getConstantBunkerRepairers()) {
            ARepairManager.updateBunkerRepairer(bunkerRepairer);
        }

        for (AUnit unitRepairer : ARepairManager.getUnitRepairers()) {
            ARepairManager.updateUnitRepairer(unitRepairer);
        }
    }

    // === Asign repairers if needed =============================
    
    private static void assignUnitRepairersToWoundedUnits() {
        
        for (AUnit woundedUnit : Select.our().repairable(true).listUnits()) {

            // Some units shouldn't be repaired
            if (AScoutManager.isScout(woundedUnit) || TerranFlyingBuildingManager.isFlyingBuilding(woundedUnit)) {
//                    || ARepairManager.isConstantBunkerRepairer(woundedUnit)) {
                continue;
            }

            // =========================================================
            
            int numberOfRepairers = ARepairManager.countRepairersForUnit(woundedUnit)
                    + ARepairManager.countConstantRepairersForBunker(woundedUnit);

            // === Repair bunker ========================================
            
            if (woundedUnit.type().isBunker()) {
                int shouldHaveThisManyRepairers = defineOptimalRepairersForBunker(woundedUnit);
                assignConstantBunkerRepairers(woundedUnit, shouldHaveThisManyRepairers - numberOfRepairers);
            } 

            // === Repair ordinary unit =================================
            else {
                assignUnitRepairers(woundedUnit, 2 - numberOfRepairers);
            }
        }
    }

    private static void assignConstantBunkerRepairersIfNeeded() {

        // If mission is not DEFEND, release all bunker repairers
        if (Missions.getGlobalMission() == null || !Missions.getGlobalMission().isMissionDefend()) {
            for (AUnit bunkerRepairer : ARepairManager.getConstantBunkerRepairers()) {
                ARepairManager.removeConstantBunkerRepairer(bunkerRepairer);
            }
            return;
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
                int numberOfRepairersAssigned = ARepairManager.countConstantRepairersForBunker(bunker);
                System.out.println(defineOptimalConstantBunkerRepairers());
                assignConstantBunkerRepairers(
                        bunker, defineOptimalConstantBunkerRepairers() - numberOfRepairersAssigned
                );
            }
        }
//        }
//        }
    }

    // =========================================================
    
    private static void assignConstantBunkerRepairers(AUnit bunker, int numberOfRepairersToAssign) {
        for (int i = 0; i < numberOfRepairersToAssign; i++) {
            AUnit worker = defineBestRepairerFor(bunker, false);
            if (worker != null) {
                ARepairManager.addConstantBunkerRepairer(worker, bunker);
            }
        }
    }

    private static void assignUnitRepairers(AUnit unitToRepair, int numberOfRepairersToAssign) {
        for (int i = 0; i < numberOfRepairersToAssign; i++) {
            AUnit worker = defineBestRepairerFor(unitToRepair, true);
            if (worker != null) {
                ARepairManager.addUnitRepairer(worker, unitToRepair);
            }
        }
    }

    // =========================================================
    
    private static int defineOptimalConstantBunkerRepairers() {

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
            return Select.ourWorkers().notRepairing().notConstructing().nearestTo(unitToRepair);
        } else {
            return Select.ourWorkers().notCarrying().notRepairing().notConstructing().nearestTo(unitToRepair);
        }
    }

}
