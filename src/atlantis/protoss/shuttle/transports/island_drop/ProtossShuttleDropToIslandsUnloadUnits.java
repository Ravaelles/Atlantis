package atlantis.protoss.shuttle.transports.island_drop;

import atlantis.architecture.Manager;
import atlantis.combat.squad.AssignUnitToSquad;
import atlantis.combat.squad.squads.iota.Iota;
import atlantis.information.enemy.EnemyOnCloseIsland;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.map.position.helpers.NearestWalkable;
import atlantis.map.region.ARegion;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import bwapi.Color;

public class ProtossShuttleDropToIslandsUnloadUnits extends Manager {
    public ProtossShuttleDropToIslandsUnloadUnits(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isShuttle()
            && !unit.loadedUnits().isEmpty();
    }

    @Override
    protected Manager handle() {
        HasPosition enemy = EnemyOnCloseIsland.get();
//        System.err.println("enemy = " + enemy);
        if (enemy == null || enemy.noPosition()) return null;

        ARegion region = enemy.position().region();
//        System.err.println("region = " + region);
        if (region == null) return null;

        APosition center = region.center();
//        System.err.println("center = " + center);
        if (center == null) return null;

        APosition dropTo = NearestWalkable.to(center, unit, true);
        System.err.println("dropTo = " + dropTo + " / " + (dropTo == null ? "-" : dropTo.isWalkable()));
        if (dropTo == null || !dropTo.isWalkable()) return null;

        assignAllLoadedUnitsToIotaSquad();

        dropTo.paintCircle(22, Color.Orange);
        dropTo.paintCircle(20, Color.Orange);
        dropTo.paintCircle(18, Color.Orange);

        if (canUnload(dropTo)) {
            for (AUnit loaded : unit.loadedUnits()){
                if (unit.unload(loaded)) {
//                    System.err.println("Unloading " + loaded);
                    return usedManager(this, "DropIslandUnloading!");
                }
            }
        }

        unit.move(dropTo, Actions.MOVE_TRANSFER);
        return usedManager(this, "DropIslandUnload");
    }

    private boolean canUnload(APosition dropTo) {
        return unit.distTo(dropTo) <= 5
            && unit.regionsMatch(dropTo)
            && unit.position().isWalkable();
    }

    private void assignAllLoadedUnitsToIotaSquad() {
        for (AUnit loaded : unit.loadedUnits()) {
            AssignUnitToSquad.assignTo(loaded, Iota.get());
        }
    }
}
