package atlantis.protoss.shuttle.transports.island_recover;

import atlantis.architecture.Manager;
import atlantis.combat.squad.AssignUnitToSquad;
import atlantis.combat.squad.squads.alpha.Alpha;
import atlantis.game.A;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.util.log.ErrorLog;

public class ProtossShuttleRecoverFromIslandsUnloadUnits extends Manager {
    public ProtossShuttleRecoverFromIslandsUnloadUnits(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isShuttle()
            && !unit.loadedUnits().isEmpty();
    }

    @Override
    protected Manager handle() {
        HasPosition dropTo = dropTo();
        if (dropTo == null) {
//            ErrorLog.printMaxOncePerMinute("ProtossShuttleRecoverFromIslandsUnloadUnits: dropTo == null");
            return null;
        }

        if (canUnloadHere(dropTo)) {
//            System.err.println("Trying to drop recovered units");
            for (AUnit loaded : unit.loadedUnits()){
//                System.err.println("Unloading recovered " + loaded + "...");
                if (unit.unload(loaded)) {
//                    System.err.println("Unloading! " + loaded);
                    AssignUnitToSquad.assignTo(loaded, Alpha.get());
                    return usedManager(this, "RecoverUnloading!");
                }
            }
            return usedManager(this, "DropIslandUnloadRecovered");
        }

        if (unit.isMoving() && unit.distTo(dropTo) <= (A.s % 6 <= 2 ? 8 : 3)) return null;

        unit.move(dropTo, Actions.MOVE_TRANSFER, "RecoverIslandMove");
        return usedManager(this);
    }

    private boolean canUnloadHere(HasPosition dropTo) {
        return unit.distTo(dropTo) <= 20
            && unit.position().isWalkable()
            && unit.position().regionsMatch(dropTo);
//            && unit.allUnitsNear().groundUnits().countInRadius(2, unit) == 0;
    }

    private HasPosition dropTo() {
        return Select.ourBuildings().nearestTo(unit).translateTilesTowards(-6, unit);
    }
}
