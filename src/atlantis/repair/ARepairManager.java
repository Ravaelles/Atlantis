package atlantis.repair;

import atlantis.AGame;
import atlantis.combat.squad.missions.Missions;
import atlantis.units.AUnit;
import atlantis.units.Select;
import atlantis.units.actions.UnitActions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Rafal Poniatowski <ravaelles@gmail.com>
 */
public class ARepairManager {
    
    // Bunker repairers
    protected static Map<AUnit, AUnit> repairersConstantToBunker = new HashMap<>();
    protected static Map<AUnit, ArrayList<AUnit>> bunkersToRepairers = new HashMap<>();
    
    // Unit repairers
    protected static Map<AUnit, AUnit> repairersToUnit = new HashMap<>();
    protected static Map<AUnit, ArrayList<AUnit>> unitsToRepairers = new HashMap<>();
    
    // =========================================================
    
    public static boolean updateUnitRepairer(AUnit repairer) {
        AUnit unitToRepair = ARepairManager.getUnitToRepairFor(repairer);
        if (unitToRepair != null && unitToRepair.isAlive()) {
            if (unitToRepair.getHPPercent() < 100) {
                repairer.setTooltip("Repair " + unitToRepair.getShortName() + "(alive:" + unitToRepair.isAlive());
                repairer.repair(unitToRepair);
                return true;
            }
            else {
                double distanceToUnit = unitToRepair.distanceTo(repairer);
                if (distanceToUnit > 1) {
                    repairer.setTooltip("Go to " + unitToRepair.getShortNamePlusId());
                    repairer.move(unitToRepair.getPosition(), UnitActions.MOVE_TO_REPAIR);
                    return true;
                }
                else {
                    repairer.setTooltip("Repaired!");
                    removeUnitRepairer(repairer);
                    return true;
                }
            }
        }
        else {
            repairer.setTooltip("Null unit2repair");
            ARepairManager.removeUnitRepairer(repairer);
            return true;
        }
        
//        // === Check if should stop being repairer =================
//        
//        if (!unitToRepair.isWounded() && countRepairersForUnit(unitToRepair) > 1) {
//            removeUnitRepairer(repairer);
//            return true;
//        }
//        
//        // =========================================================
//        
//        return handleRepairerWhenIdle(repairer);
    }
    
    public static boolean updateBunkerRepairer(AUnit repairer) {
        AUnit bunker = ARepairManager.getConstantBunkerToRepairFor(repairer);
        if (bunker != null && bunker.isAlive()) {
            
            // Bunker WOUNDED
            if (bunker.getHPPercent() < 100) {
                repairer.setTooltip("Repair " + bunker.getShortName());
                repairer.repair(bunker);
                return true;
            }
            
            // Bunker fully HEALTHY
            else {
                double distanceToUnit = bunker.distanceTo(repairer);
                if (distanceToUnit > 1 && !repairer.isMoving()) {
                    repairer.setTooltip("Go to " + bunker.getShortName());
                    repairer.move(bunker.getPosition(), UnitActions.MOVE_TO_REPAIR);
                    return true;
                }
                else {
                    repairer.setTooltip("Protect " + bunker.getShortName());
                }
            }
        }
        else {
            repairer.setTooltip("Null bunker");
            ARepairManager.removeConstantBunkerRepairer(repairer);
            return true;
        }
        
        return handleRepairerWhenIdle(repairer);
    }
    
    private static boolean handleRepairerWhenIdle(AUnit repairer) {
        if (repairer.isMoving() || !repairer.isRepairing() || repairer.isIdle()) {
            int maxAllowedDistToRoam = Missions.getGlobalMission().isMissionDefend() ? 4 : 12;
            
            // Try finding any repairable and wounded unit nearby
            AUnit nearestWoundedUnit = Select.our().repairable(true)
                    .inRadius(maxAllowedDistToRoam, repairer).nearestTo(repairer);
            if (nearestWoundedUnit != null) {
                repairer.repair(nearestWoundedUnit);
                repairer.setTooltip("Help near " + nearestWoundedUnit.getShortName());
                return true;
            }
        }
        
        return false;
    }
    
    // =========================================================
    
    public static boolean isConstantBunkerRepairer(AUnit repairer) {
        return repairersConstantToBunker.containsKey(repairer);
    }
    
    public static boolean isUnitRepairer(AUnit repairer) {
        return repairersToUnit.containsKey(repairer);
    }
    
    public static AUnit getConstantBunkerToRepairFor(AUnit repairer) {
        return repairersConstantToBunker.get(repairer);
    }
    
    public static AUnit getUnitToRepairFor(AUnit repairer) {
        return repairersToUnit.get(repairer);
    }

    public static void removeConstantBunkerRepairer(AUnit repairer) {
        AUnit bunker = repairersConstantToBunker.get(repairer);
        if (bunker != null && bunkersToRepairers.containsKey(bunker)) {
            bunkersToRepairers.get(bunker).remove(repairer);

            repairer.stop();
            repairer.setTooltip("Stop");
        }
        repairersConstantToBunker.remove(repairer);
    }

    public static void removeUnitRepairer(AUnit repairer) {
        AUnit unitToRepair = repairersToUnit.get(repairer);
        if (unitToRepair != null && unitsToRepairers.containsKey(unitToRepair)) {
            unitsToRepairers.get(unitToRepair).remove(repairer);
        
            repairer.stop();
            repairer.setTooltip("Stop");
        }
        repairersToUnit.remove(repairer);
    }
    
    public static void addConstantBunkerRepairer(AUnit repairer, AUnit bunker) {
        if (!bunker.isBunker()) {
            System.err.println("Repair: this ain't no bunker dude (" + bunker + ")");
        }
        
        repairersConstantToBunker.put(repairer, bunker);
        if (!bunkersToRepairers.containsKey(bunker)) {
            bunkersToRepairers.put(bunker, new ArrayList<>());
        }
        bunkersToRepairers.get(bunker).add(repairer);
        
        AGame.sendMessage("Repairer for bunker");
    }
    
    public static void addUnitRepairer(AUnit repairer, AUnit unitToRepair) {
        repairersConstantToBunker.put(repairer, unitToRepair);
        if (!unitsToRepairers.containsKey(unitToRepair)) {
            unitsToRepairers.put(unitToRepair, new ArrayList<>());
        }
        unitsToRepairers.get(unitToRepair).add(repairer);
        AGame.sendMessage("Repairer for " + unitToRepair.getShortName());
    }

    public static int countConstantRepairersForBunker(AUnit bunker) {
        return bunkersToRepairers.containsKey(bunker) ? bunkersToRepairers.get(bunker).size() : 0;
    }

    public static int countRepairersForUnit(AUnit unit) {
        return unitsToRepairers.containsKey(unit) ? unitsToRepairers.get(unit).size() : 0;
    }

    public static boolean isRepairerOfAnyKind(AUnit worker) {
        return repairersConstantToBunker.containsKey(worker) || repairersToUnit.containsKey(worker);
    }

    public static Collection<AUnit> getConstantBunkerRepairers() {
        ArrayList<AUnit> result = new ArrayList<>();
        result.addAll(repairersConstantToBunker.keySet());
        return result;
    }

    public static Collection<AUnit> getUnitRepairers() {
        ArrayList<AUnit> result = new ArrayList<>();
        result.addAll(repairersToUnit.keySet());
        return result;
    }
    
}
