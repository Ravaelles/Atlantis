package atlantis.repair;

import atlantis.units.AUnit;
import java.util.ArrayList;
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
                repairer.setTooltip("Repair " + unitToRepair.getShortNamePlusId());
                repairer.repair(unitToRepair);
                return true;
            }
            else {
                repairer.setTooltip("Protect " + unitToRepair.getShortNamePlusId());
            }
        }
        else {
            repairer.setTooltip("Null unit2repair");
            ARepairManager.removeUnitRepairer(repairer);
        }
        
        return handleRepairerWhenIdle(repairer);
    }
    
    public static boolean updateBunkerRepairer(AUnit repairer) {
        AUnit bunker = ARepairManager.getConstantBunkerToRepairFor(repairer);
        if (bunker != null && bunker.isAlive()) {
            if (bunker.getHPPercent() < 100) {
                repairer.setTooltip("Repair " + bunker.getShortNamePlusId());
                repairer.repair(bunker);
                return true;
            }
            else {
                repairer.setTooltip("Protect " + bunker.getShortNamePlusId());
            }
        }
        else {
            repairer.setTooltip("Null bunker");
            ARepairManager.removeConstantBunkerRepairer(repairer);
        }
        
        return handleRepairerWhenIdle(repairer);
    }
    
    private static boolean handleRepairerWhenIdle(AUnit repairer) {
        if (!repairer.isRepairing() || repairer.isIdle()) {
            
            // Try finding any repairable and wounded unit nearby
            
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
        bunkersToRepairers.get(bunker).remove(repairer);
        repairersConstantToBunker.remove(repairer);
        
        repairer.stop();
        repairer.setTooltip("Stop");
    }

    public static void removeUnitRepairer(AUnit repairer) {
        AUnit unitToRepair = repairersToUnit.get(repairer);
        unitsToRepairers.get(unitToRepair).remove(repairer);
        repairersConstantToBunker.remove(repairer);
        
        repairer.stop();
        repairer.setTooltip("Stop");
    }
    
    public static void addConstantBunkerRepairer(AUnit repairer, AUnit bunker) {
        repairersConstantToBunker.put(repairer, bunker);
        if (!bunkersToRepairers.containsKey(bunker)) {
            bunkersToRepairers.put(bunker, new ArrayList<>());
        }
        bunkersToRepairers.get(bunker).add(repairer);
    }
    
    public static void addUnitRepairer(AUnit repairer, AUnit unitToRepair) {
        repairersConstantToBunker.put(repairer, unitToRepair);
        if (!unitsToRepairers.containsKey(unitToRepair)) {
            unitsToRepairers.put(unitToRepair, new ArrayList<>());
        }
        unitsToRepairers.get(unitToRepair).add(repairer);
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
    
}
