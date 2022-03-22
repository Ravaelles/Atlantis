package atlantis.combat.micro.terran;

import atlantis.combat.micro.Microable;
import atlantis.information.tech.ATech;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;
import bwapi.TechType;

import static atlantis.units.AUnitType.Terran_Ghost;

public class TerranGhost extends Microable {

    private static final TechType lockdown = TechType.Lockdown;

    private AUnit unit;

    // =========================================================

    public boolean update(AUnit unit) {
        this.unit = unit;

        if (unit.lastTechUsedAgo() <= 4) {
            return true;
        }

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

        AUnit goodTarget = defineTarget();

        if (goodTarget != null) {
            unit.setTooltipTactical("Lockdown!");
            return unit.useTech(lockdown, goodTarget);
        }

        return false;
    }

    private AUnit defineTarget() {
        AUnit greatTarget = greatTarget();
        if (greatTarget != null) {
            return greatTarget;
        }

        return standardTarget();
    }

    private AUnit greatTarget() {
        Selection selection = unit.enemiesNear().mechanical().havingPosition().thatCanMove();

        if (unit.energy() < 200 && Select.ourOfType(Terran_Ghost).havingEnergy(100).atMost(1)) {
            selection = selection.ofType(
                AUnitType.Protoss_Carrier, AUnitType.Protoss_Reaver, AUnitType.Protoss_Shuttle, AUnitType.Protoss_Observer,
                AUnitType.Terran_Science_Vessel, AUnitType.Terran_Dropship, AUnitType.Terran_Valkyrie,
                AUnitType.Terran_Battlecruiser, AUnitType.Terran_Siege_Tank_Tank_Mode, AUnitType.Terran_Siege_Tank_Siege_Mode
            );
        }

        return selection.nearestTo(unit);
    }

    private AUnit standardTarget() {
        Selection selection = unit.enemiesNear().havingPosition().thatCanMove().inRadius(13, unit);
        selection = selection.mechanical();

        return selection.nearestTo(unit);
    }

}
