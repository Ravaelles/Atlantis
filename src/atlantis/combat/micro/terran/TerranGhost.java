package atlantis.combat.micro.terran;

import atlantis.combat.micro.Microable;
import atlantis.information.tech.ATech;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import bwapi.TechType;

public class TerranGhost extends Microable {

    private static AUnit unit;
    private static TechType lockdown = TechType.Lockdown;

    // =========================================================

    public static boolean update(AUnit unit) {
        TerranGhost.unit = unit;

        if (useLockdown()) {
            return true;
        }

        return false;
    }

    // =========================================================

    private static boolean useLockdown() {
        if (!unit.energy(100) || !ATech.isResearched(lockdown)) {
            return false;
        }

        AUnit goodTarget = unit.enemiesNear().ofType(
            AUnitType.Protoss_Carrier, AUnitType.Protoss_Reaver, AUnitType.Protoss_Shuttle, AUnitType.Protoss_Observer,
            AUnitType.Terran_Science_Vessel, AUnitType.Terran_Dropship, AUnitType.Terran_Valkyrie,
            AUnitType.Terran_Battlecruiser, AUnitType.Terran_Siege_Tank_Tank_Mode, AUnitType.Terran_Siege_Tank_Siege_Mode
        ).thatCanMove().nearestTo(unit);

        if (goodTarget != null) {
            unit.setTooltipTactical("Lockdown!");
            return unit.useTech(lockdown, goodTarget);
        }

        return false;
    }

}
