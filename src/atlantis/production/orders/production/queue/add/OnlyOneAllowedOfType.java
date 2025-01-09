package atlantis.production.orders.production.queue.add;

import atlantis.game.A;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.util.We;

public class OnlyOneAllowedOfType {
    protected static boolean onlyOneAllowed(AUnitType type, HasPosition position) {
        if (We.protoss()) {
            if (type.equals(AUnitType.Protoss_Forge)) return Count.withPlanned(type) > 0;
            if (type.equals(AUnitType.Protoss_Citadel_of_Adun)) return Count.withPlanned(type) > 0;
            if (type.equals(AUnitType.Protoss_Cybernetics_Core)) return Count.withPlanned(type) > 0;
            if (type.equals(AUnitType.Protoss_Observatory)) return Count.withPlanned(type) > 0;
            if (type.equals(AUnitType.Protoss_Robotics_Facility)) {
                return Count.withPlanned(type) > (A.supplyUsed() <= 130
                    ? 0
                    : (Count.freeRoboticsFacility() == 0 ? 1 : 0));
            }
            if (type.equals(AUnitType.Protoss_Robotics_Support_Bay)) return Count.withPlanned(type) > 0;
            if (type.equals(AUnitType.Protoss_Fleet_Beacon)) return Count.withPlanned(type) > 0;
            if (type.equals(AUnitType.Protoss_Templar_Archives)) return Count.withPlanned(type) > 0;
        }
        else if (We.terran()) {
            if (type.equals(AUnitType.Terran_Engineering_Bay)) return Count.withPlanned(type) > 0;
            if (type.equals(AUnitType.Terran_Armory)) return Count.withPlanned(type) > 0;
        }

        return false;
    }
}
