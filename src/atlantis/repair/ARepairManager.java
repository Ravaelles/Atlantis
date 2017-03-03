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
    
    public static final int MODE_REPAIR_ONLY = 1;
    public static final int MODE_PROTECT = 2;
    
    // Unit repairers
    protected static Map<AUnit, AUnit> repairersToUnit = new HashMap<>();
    protected static Map<AUnit, ArrayList<AUnit>> unitsToRepairers = new HashMap<>();
    protected static Map<AUnit, Integer> repairersToModes = new HashMap<>();
    
    // =========================================================
    
    public static boolean handleRepairedUnitBehavior(AUnit unit) {
        if (!unit.isWounded()) {
            return false;
        }
        
        // =========================================================
        
        AUnit repairer = getRepairerForUnit(unit);
        if (repairer != null) {
            double distanceToRepairer = repairer.distanceTo(unit);
            
            // Ignore repair if quite healthy and repairer is far
            if (unit.getHPPercent() > 50 && distanceToRepairer > 4) {
                return false;
            }
            
            // Go to repairer if he's close
            if (distanceToRepairer > 0.02) {
                unit.move(repairer.getPosition(), UnitActions.MOVE_TO_REPAIR);
                return true;
            }
            
            // We're very close to repairer, wait.
            else if (unit.isMoving() && !unit.isRunning()) {
                unit.holdPosition();
                return true;
            }
        }
        
        return false;
    }
    
    // =========================================================
    
    public static boolean updateRepairer(AUnit repairer) {
        AUnit unitToRepair = ARepairManager.getUnitToRepairFor(repairer);
        if (unitToRepair != null && unitToRepair.isAlive()) {
            if (unitToRepair.getHPPercent() < 80) {
                repairer.setTooltip("Repair " + unitToRepair.getShortName() + "(alive:" + unitToRepair.isAlive());
                repairer.repair(unitToRepair);
                return true;
            }
            else {
                double distanceToUnit = unitToRepair.distanceTo(repairer);
                if (distanceToUnit > 0.1) {
                    repairer.setTooltip("Go to " + unitToRepair.getShortNamePlusId());
                    repairer.move(unitToRepair.getPosition(), UnitActions.MOVE_TO_REPAIR);
                    return true;
                }
                else {
                    repairer.setTooltip("Repaired!");
                    removeRepairerOrProtector(repairer);
                    return true;
                }
            }
        }
        else {
            repairer.setTooltip("Null unit2repair");
            ARepairManager.removeRepairerOrProtector(repairer);
            return true;
        }
    }
    
    public static boolean updateProtector(AUnit protector) {
        AUnit unit = ARepairManager.getUnitToProtectFor(protector);
        if (unit != null && unit.isAlive()) {
            
            // Bunker WOUNDED
            if (unit.getHPPercent() < 100) {
                protector.setTooltip("Repair " + unit.getShortName());
                protector.repair(unit);
                return true;
            }
            
            // Bunker fully HEALTHY
            else {
                double distanceToUnit = unit.distanceTo(protector);
                if (distanceToUnit > 1 && !protector.isMoving()) {
                    protector.setTooltip("Go to " + unit.getShortName());
                    protector.move(unit.getPosition(), UnitActions.MOVE_TO_REPAIR);
                    return true;
                }
                else {
                    protector.setTooltip("Protect " + unit.getShortName());
                }
            }
        }
        else {
            protector.setTooltip("Null bunker");
            ARepairManager.removeRepairerOrProtector(protector);
            return true;
        }
        
        return handleRepairerWhenIdle(protector);
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
    
    public static boolean isProtector(AUnit repairer) {
        return repairersToModes.get(repairer) == MODE_PROTECT;
    }
    
    public static boolean isOnlyRepairer(AUnit repairer) {
        return repairersToUnit.containsKey(repairer);
    }
    
    public static AUnit getUnitToProtectFor(AUnit repairer) {
        return repairersToUnit.get(repairer);
    }
    
    public static AUnit getUnitToRepairFor(AUnit repairer) {
        return repairersToUnit.get(repairer);
    }
    
    public static AUnit getRepairerForUnit(AUnit wounded) {
        if (unitsToRepairers.containsKey(wounded)) {
            ArrayList<AUnit> repairers = unitsToRepairers.get(wounded);
            if (repairers.size() >= 1) {
                if (repairers.get(0).isAlive()) {
                    return repairers.get(0);
                }
                else {
                    removeRepairerOrProtector(repairers.get(0));
                }
            }
        }
        return null;
    }

    public static void removeRepairerOrProtector(AUnit repairer) {
        AUnit unitToRepair = repairersToUnit.get(repairer);
        if (unitToRepair != null && unitsToRepairers.containsKey(unitToRepair)) {
            unitsToRepairers.get(unitToRepair).remove(repairer);
        
            repairer.stop();
            repairer.setTooltip("Stop");
        }
        repairersToUnit.remove(repairer);
    }
    
    public static void addProtector(AUnit protector, AUnit unit) {
        addRepairer(protector, unit);
        repairersToModes.put(protector, MODE_PROTECT);
        
        AGame.sendMessage("Repairer for bunker");
    }
    
    public static void addRepairer(AUnit repairer, AUnit unitToRepair) {
        repairersToUnit.put(repairer, unitToRepair);
        repairersToModes.put(repairer, MODE_REPAIR_ONLY);
        if (!unitsToRepairers.containsKey(unitToRepair)) {
            unitsToRepairers.put(unitToRepair, new ArrayList<>());
        }
        unitsToRepairers.get(unitToRepair).add(repairer);
        
//        AGame.sendMessage("Repairer for " + unitToRepair.getShortName());
    }

    public static int countProtectorsFor(AUnit unit) {
        if (!unitsToRepairers.containsKey(unit)) {
            return 0;
        }
        
        int protectors = 0;
        for (AUnit repairer : unitsToRepairers.get(unit)) {
            if (isProtector(repairer)) {
                protectors++;
            }
        }
        
        return protectors;
    }

    public static int countRepairersForUnit(AUnit unit) {
        return unitsToRepairers.containsKey(unit) ? unitsToRepairers.get(unit).size() : 0;
    }

    public static boolean isRepairerOfAnyKind(AUnit worker) {
        return repairersToUnit.containsKey(worker);
    }

    public static Collection<AUnit> getProtectors() {
        ArrayList<AUnit> result = new ArrayList<>();
        for (AUnit repairer : repairersToUnit.keySet()) {
            if (isProtector(repairer)) {
                result.add(repairer);
            }
        }
        return result;
    }

    public static Collection<AUnit> getRepairers() {
        ArrayList<AUnit> result = new ArrayList<>();
        result.addAll(repairersToUnit.keySet());
        return result;
    }
    
}
