package atlantis.combat.micro.terran.infantry;

import atlantis.combat.micro.Microable;
import atlantis.information.tech.ATech;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.architecture.Manager;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;
import atlantis.util.TargetsForUnits;
import bwapi.TechType;

public class TerranGhost extends Manager {

    private  final TechType lockdown = TechType.Lockdown;

    public  TargetsForUnits lockdownTargets = new TargetsForUnits();

    // =========================================================

    public TerranGhost(AUnit unit) {
        super(unit);
    }

    // =========================================================

    public boolean update() {
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
//            System.err.println("USE LOCKDOWN ON " + lockdownTarget);
            unit.setTooltipTactical("Lockdown!");
            unit.useTech(lockdown, lockdownTarget);
            lockdownTargets.addTarget(lockdownTarget, unit);

//            lockdownTargets.print("lockdownTargets");
            return true;
        }

        return false;
    }

    public AUnit defineLockdownTarget() {
        AUnit greatTarget = greatLockdownTarget();
        if (greatTarget != null) {
            return greatTarget;
        }

        // Don't waste energy on standard units if we have little Ghosts with energy. They might be needed elsewhere.
        if (Select.ourOfType(AUnitType.Terran_Ghost).havingEnergy(100).atMost(1)) {
            return null;
        }

        return standardTarget();
    }

    private AUnit greatLockdownTarget() {
        Selection selection = possibleTargets()
            .ofType(
                AUnitType.Protoss_Carrier, AUnitType.Protoss_Reaver,
                AUnitType.Protoss_Shuttle, AUnitType.Protoss_Observer,
                AUnitType.Protoss_Arbiter,
                AUnitType.Terran_Science_Vessel, AUnitType.Terran_Dropship, AUnitType.Terran_Valkyrie,
                AUnitType.Terran_Battlecruiser, AUnitType.Terran_Siege_Tank_Tank_Mode,
                AUnitType.Terran_Siege_Tank_Siege_Mode
            )
            .exclude(lockdownTargets.targetsAcquiredInLast(45));

        return selection.nearestTo(unit);
    }

    private AUnit standardTarget() {
        Selection selection = possibleTargets()
            .excludeTypes(AUnitType.Protoss_Interceptor)
            .exclude(lockdownTargets.targetsAcquiredInLast(45));

        if (!Enemy.terran()) {
            selection = selection.havingAtLeastHp(50);
        }

        return selection.nearestTo(unit);
    }

    private Selection possibleTargets() {
        return unit.enemiesNear()
            .nonBuildings()
            .visibleOnMap()
            .effVisible()
            .mechanical()
            .havingPosition()
            .nonStasisedOrLockedDown();
    }

}
