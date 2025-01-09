package atlantis.terran.repair.protect;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.game.AGame;
import atlantis.terran.repair.CanAbandonUnitAssignedToRepair;
import atlantis.terran.repair.NewRepairer;
import atlantis.terran.repair.RepairAssignments;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ProtectorCommander extends Commander {
    private static final int HARD_MAX_PROTECTORS = 9;

    // =========================================================

    @Override
    protected void handle() {
        if (AGame.everyNthGameFrame(5)) {
            AssignNewBunkerProtectors.assignBunkerProtectorsIfNeeded();
        }
        if (AGame.everyNthGameFrame(13)) {
            assignUnitsProtectorsIfNeeded();
        }
        removeProtectorsIfNeeded();

        for (Iterator<AUnit> iterator = RepairAssignments.getProtectors().iterator(); iterator.hasNext(); ) {
            AUnit unit = iterator.next();
            (new ProtectorManager(unit)).invokeFrom(this);
        }
    }

    // =========================================================

    protected static void removeExcessiveProtectors(ArrayList<AUnit> existingProtectors, int howMany) {
        ArrayList<AUnit> toRemove = new ArrayList<>();
        int protectorsToRemove = Math.min(howMany, existingProtectors.size());
        for (int i = 0; i < protectorsToRemove; i++) {
            toRemove.add(existingProtectors.get(i));
        }

        for (AUnit protector : toRemove) {
            RepairAssignments.removeProtector(protector);
        }
    }

    protected static int maxProtectors() {
        int workers = Count.workers();

        if (workers <= 10 && !A.hasMinerals(13)) return 0;
        if (workers <= 10) return 1;

        if (!A.hasMinerals(3)) {
            return workers >= 20 ? 4 : 1;
        }

        if (!A.hasMinerals(20)) {
            return workers >= 14 ? 3 : 2;
        }

        if (workers <= 20) {
            if (!A.hasMinerals(1)) {
                return 4;
            }
//            else if (!A.hasMinerals(10)) {
//                return Math.min(workers / 2, MAX_PROTECTORS);
//            }
        }

        return (int) Math.min(workers / 2.3, HARD_MAX_PROTECTORS);
    }

    protected static boolean assignUnitsProtectorsIfNeeded() {
        if (Count.protectors() > maxProtectors()) return false;

        int maxProtectors = maxProtectors();
        int totalProtectors = RepairAssignments.countTotalProtectors();
        if (totalProtectors >= maxProtectors) return false;

        List<AUnit> tanks = Select.ourTanks().list();
        if (tanks.isEmpty()) return false;

        for (int i = 0; i < maxProtectors - totalProtectors; i++) {
            addProtectorsForUnit(tanks.get(i % tanks.size()), 1);
        }

        if (RepairAssignments.countTotalProtectors() == 0) {
            addProtectorsForUnit(Select.ourOfType(AUnitType.Terran_Medic).last(), 1);
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
//            int protectors = RepairAssignments.countProtectorsFor(firstVulture);
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
//            int protectors = RepairAssignments.countProtectorsFor(firstTank);
//            int lackingProtectors = 2 - protectors;
//            if (lackingProtectors > 0) {
//                assignProtectorsFor(firstTank, lackingProtectors);
//                return;
//            }
//        }
        return false;
    }

    private static boolean removeProtectorsIfNeeded() {
        int maxProtectors = maxProtectors();
        int protectorsToRemove = RepairAssignments.countTotalProtectors() - maxProtectors;

        if (protectorsToRemove > 0) {
            for (int i = 0; i < protectorsToRemove; i++) {
                AUnit protector = RepairAssignments.getProtectors().get(RepairAssignments.getProtectors().size() - 1);
                if (CanAbandonUnitAssignedToRepair.check(protector)) {
//                    System.err.println("Remove repairer / protector");
                    RepairAssignments.removeRepairer(protector);
                }
            }
            return true;
        }
        return false;
    }

    // =========================================================

    public static void addProtectorsForUnit(AUnit unitToProtect, int numberOfProtectorsToAssign) {
        if (unitToProtect == null || unitToProtect.isDead()) return;

        numberOfProtectorsToAssign = numberOfProtectorsToAssign - RepairAssignments.countProtectorsFor(unitToProtect);

        if (numberOfProtectorsToAssign <= 0) return;

//        System.err.println("numberOfProtectorsToAssign = " + numberOfProtectorsToAssign);
        for (int i = 0; i < numberOfProtectorsToAssign; i++) {
            AUnit worker = NewRepairer.repairerFor(
                unitToProtect,
                unitToProtect.isBunker() || unitToProtect.isTank()
            );

            if (worker != null) RepairAssignments.addProtector(worker, unitToProtect);
        }
    }
}
