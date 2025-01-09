package atlantis.terran.repair.protect;

import atlantis.debug.painter.AAdvancedPainter;
import atlantis.game.A;
import atlantis.terran.repair.OptimalNumOfBunkerRepairers;
import atlantis.terran.repair.RepairAssignments;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import bwapi.Color;

import java.util.ArrayList;

public class AssignNewBunkerProtectors {
    public static boolean assignBunkerProtectorsIfNeeded() {
        if (Count.protectors() > ProtectorCommander.maxProtectors()) return false;

        for (AUnit bunker : Select.ourOfType(AUnitType.Terran_Bunker).list()) {

            // No enemies + bunker healthy
            ArrayList<AUnit> existingProtectors = RepairAssignments.protectorsFor(bunker);
            int desiredBunkerProtectors = OptimalNumOfBunkerRepairers.forBunker(bunker);
            int howMany = desiredBunkerProtectors - existingProtectors.size();

//            if (desiredBunkerProtectors > 0) {
//                System.out.println(A.minSec() + " - BUNKER protectors = " + desiredBunkerProtectors + " / HOW=" + howMany);
//            }

            // Remove some (or all) existing protectors
            if (howMany < 0) {
                ProtectorCommander.removeExcessiveProtectors(existingProtectors, -howMany);
            }

            // Bunker damaged or enemies nearby
            else if (howMany > 0) {
                AAdvancedPainter.paintTextCentered(bunker, howMany + "", Color.Orange);
                ProtectorCommander.addProtectorsForUnit(bunker, howMany);
            }

            Color color = howMany > 0 ? Color.Orange : Color.Grey;
            AAdvancedPainter.paintTextCentered(bunker, howMany + "", color);
        }

        return true;
    }
}
