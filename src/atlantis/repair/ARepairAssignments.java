package atlantis.repair;

import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ARepairAssignments {

    public static final int MODE_REPAIR_ONLY = 1;
    public static final int MODE_PROTECT = 2;
    public static final int MAX_REPAIRERS = 6;

    // Unit repairers
    protected static Map<AUnit, AUnit> repairersToUnit = new HashMap<>();
    protected static Map<AUnit, ArrayList<AUnit>> unitsToRepairers = new HashMap<>();
    protected static Map<AUnit, Integer> repairersToModes = new HashMap<>();

    // =========================================================

    public static boolean isBunkerProtector(AUnit repairer) {
        return repairersToModes.containsKey(repairer) && repairersToModes.get(repairer) == MODE_PROTECT;
    }

    public static AUnit getUnitToProtectFor(AUnit repairer) {
        return repairersToUnit.get(repairer);
    }

    public static AUnit getUnitToRepairFor(AUnit repairer) {
        return repairersToUnit.get(repairer);
    }

    public static AUnit getClosestRepairerAssignedTo(AUnit wounded) {
        if (unitsToRepairers.size() > MAX_REPAIRERS || unitsToRepairers.size() >= (0.5 * Select.ourWorkers().count())) {
            return null;
        }

        if (unitsToRepairers.containsKey(wounded)) {
            ArrayList<AUnit> repairers = unitsToRepairers.get(wounded);
            return Select.from(repairers).nearestTo(wounded);
//            if (repairers.size() >= 1) {
//                if (repairers.get(0).isAlive()) {
//                    return repairers.get(0);
//                }
//                else {
//                    removeRepairerOrProtector(repairers.get(0));
//                }
//            }
        }
        return null;
    }

    public static void removeRepairerOrProtector(AUnit repairer) {
        AUnit unitToRepair = repairersToUnit.get(repairer);
        if (unitToRepair != null && unitsToRepairers.containsKey(unitToRepair)) {
            unitsToRepairers.get(unitToRepair).remove(repairer);
            repairer.stop("No longer repairer");
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
            if (isBunkerProtector(repairer)) {
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
            if (isBunkerProtector(repairer)) {
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
