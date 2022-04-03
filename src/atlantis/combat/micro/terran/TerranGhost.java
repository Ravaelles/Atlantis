package atlantis.combat.micro.terran;

import atlantis.combat.micro.Microable;
import atlantis.game.A;
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

        if (unit.lastTechUsedAgo() <= 5) {
//            System.out.println(A.now() + " DONT INTERRUPT GHOST ");
            return true;
        }

        if (useLockdown()) {
            return true;
        }

        return false;
    }

    // =========================================================

    private boolean useLockdown() {
//        if (Enemy.zerg()) {
//            return false; // I wish ;__:
//        }

        if (!unit.energy(100) || !ATech.isResearched(lockdown)) {
            return false;
        }

        AUnit lockdownTarget = defineLockdownTarget();
//        System.out.println("lockdownTarget = " + lockdownTarget);

        if (lockdownTarget != null) {
            System.err.println("USE LOCKDOWN ON " + lockdownTarget);
            unit.setTooltipTactical("Lockdown!");
            unit.useTech(lockdown, lockdownTarget);
            return true;
        }

        return false;
    }

    public AUnit defineLockdownTarget() {
        AUnit greatTarget = greatLockdownTarget();
        if (greatTarget != null) {
            return greatTarget;
        }

        return standardTarget();
    }

    private AUnit greatLockdownTarget() {
//        unit.enemiesNear().print();
//        unit.enemiesNear().visibleOnMap().print();
//        unit.enemiesNear().effVisible().mechanical().print();
//        unit.enemiesNear().effVisible().mechanical().havingPosition().print();
//        unit.enemiesNear().effVisible().mechanical().havingPosition().notStasisedOrLockedDown().print();
//        System.out.println("---");

        Selection selection = unit.enemiesNear().visibleOnMap()
            .effVisible().mechanical().havingPosition().notStasisedOrLockedDown();

//        if (unit.energy() < 200 && Select.ourOfType(Terran_Ghost).havingEnergy(100).atMost(2)) {
//        if (unit.energy() < 200) {
            selection = selection.ofType(
                AUnitType.Protoss_Carrier, AUnitType.Protoss_Reaver, AUnitType.Protoss_Shuttle, AUnitType.Protoss_Observer,
                AUnitType.Terran_Science_Vessel, AUnitType.Terran_Dropship, AUnitType.Terran_Valkyrie,
                AUnitType.Terran_Battlecruiser, AUnitType.Terran_Siege_Tank_Tank_Mode, AUnitType.Terran_Siege_Tank_Siege_Mode
            );
//        }

        return selection.nearestTo(unit);
    }

    private AUnit standardTarget() {
        Selection selection = unit.enemiesNear().havingPosition()
            .visibleOnMap().mechanical().effVisible().notStasisedOrLockedDown();

        return selection.nearestTo(unit);
    }

}
