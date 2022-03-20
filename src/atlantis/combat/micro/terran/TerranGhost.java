package atlantis.combat.micro.terran;

import atlantis.combat.micro.Microable;
import atlantis.information.tech.ATech;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;
import bwapi.TechType;

public class TerranGhost extends Microable {

    private static final TechType lockdown = TechType.Lockdown;

    private AUnit unit;

    // =========================================================

    public boolean update(AUnit unit) {
        this.unit = unit;

        if (useLockdown()) {
            return true;
        }

        return false;
    }

    // =========================================================

    private boolean useLockdown() {
        if (Enemy.zerg()) {
            return false; // I wish ;__:
        }

        if (!unit.energy(100) || !ATech.isResearched(lockdown)) {
            return false;
        }

        AUnit goodTarget;
        Selection selection = unit.enemiesNear().thatCanMove().inRadius(13, unit);

        if (unit.energy() < 190) {
            selection = selection.ofType(
                AUnitType.Protoss_Carrier, AUnitType.Protoss_Reaver, AUnitType.Protoss_Shuttle, AUnitType.Protoss_Observer,
                AUnitType.Terran_Science_Vessel, AUnitType.Terran_Dropship, AUnitType.Terran_Valkyrie,
                AUnitType.Terran_Battlecruiser, AUnitType.Terran_Siege_Tank_Tank_Mode, AUnitType.Terran_Siege_Tank_Siege_Mode
            );
        }
        else {
            selection = selection.mechanical();
        }

        goodTarget = selection.nearestTo(unit);

        if (goodTarget != null) {
            unit.setTooltipTactical("Lockdown!");
            return unit.useTech(lockdown, goodTarget);
        }

        return false;
    }

}
