package atlantis.repair;

import atlantis.AGame;
import atlantis.combat.micro.AAttackEnemyUnit;
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

    public static final int MAX_REPAIRERS = 6;

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
            if (distanceToRepairer > 1) {
                unit.setTooltip("Move to repair");
                unit.move(repairer.getPosition(), UnitActions.MOVE_TO_REPAIR);
                return true;
            }
            
            // We're very close to repairer, wait.
            else {
                boolean result = AAttackEnemyUnit.handleAttackEnemyUnits(unit);
                if (result) {
                    unit.setTooltip("Attack while repaired");
                }
                else {
                    unit.setTooltip("Wait for repair");
                    unit.stop();
                }
                return true;
//                unit.holdPosition();
//                return true;
            }
        }
        
        return false;
    }
    
    // =========================================================
    
    public static boolean updateRepairer(AUnit repairer) {
        AUnit target = ARepairManager.getUnitToRepairFor(repairer);
        if (target != null && target.isAlive()) {
            
            // Target is wounded
            if (target.isWounded()) {
                repairer.setTooltip("Repair " + target.getShortNamePlusId() + " " + repairer.getLastUnitOrderWasFramesAgo());
                repairer.repair(target);
                return true;
            }
            
            // Target is totally healthy
            else {
                repairer.setTooltip("Repaired!");
                removeRepairerOrProtector(repairer);
                return false;
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
                protector.setTooltip("Protect " + unit.getShortName());
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
        return repairersToModes.containsKey(repairer) && repairersToModes.get(repairer) == MODE_PROTECT;
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
        if (unitsToRepairers.size() > MAX_REPAIRERS || unitsToRepairers.size() >= (0.5 * Select.ourWorkers().count())) {
            return null;
        }

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
            repairer.setTooltip("Stahp");
        }
        repairersToUnit.remove(repairer);
        repairersToModes.remove(repairer);
    }
    
    public static void addProtector(AUnit protector, AUnit unit) {
        addRepairer(protector, unit);
        repairersToModes.put(protector, MODE_PROTECT);
    }
    
    public static void addRepairer(AUnit repairer, AUnit unitToRepair) {
        repairersToUnit.put(repairer, unitToRepair);
        repairersToModes.put(repairer, MODE_REPAIR_ONLY);
        if (!unitsToRepairers.containsKey(unitToRepair)) {
            unitsToRepairers.put(unitToRepair, new ArrayList<>());
        }
        unitsToRepairers.get(unitToRepair).add(repairer);
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

    public static int countTotalProtectors() {
        int protectors = 0;
        for (int mode : repairersToModes.values()) {
            if (mode == MODE_PROTECT) {
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
