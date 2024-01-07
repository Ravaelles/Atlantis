package atlantis.terran.chokeblockers;

import atlantis.architecture.Manager;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;

public class ChokeBlockerManager extends Manager {
    private final AUnit otherBlocker;
    private final APosition blockChokePoint;

    public ChokeBlockerManager(AUnit unit) {
        super(unit);
        this.otherBlocker = ChokeBlockersAssignments.get().otherBlocker(unit);
        this.blockChokePoint = unit.specialPosition();
    }

    @Override
    public boolean applies() {
        return unit.enemiesNear().notEmpty()
            || unit.friendsNear().nonBuildings().inRadius(8, unit).atMost(14);
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            ChokeBlockerRepairOther.class,
            ChokeBlockerMoveAway.class,
            ChokeBlockerFight.class,
            ChokeBlockerMoveToBlock.class,
        };
    }

//    @Override
//    protected Manager handle() {
////        if (shouldMoveAway()) return moveAway();
//
//        if (otherBlocker == null || !otherBlocker.isAlive() || otherBlocker.isHealthy()) {
//            unit.move(blockChokePoint, Actions.MOVE_SPECIAL, "ChokeBlocker");
//        }
//        else {
//            unit.repair(otherBlocker, "RepairChoker");
//        }
//
//        return usedManager(this);
//    }
//
//    private boolean shouldMoveAway() {
//        if (blockChokePoint.distTo(unit) > 8) return false;
//
////        return unit.enemiesNear().inRadius(5, unit).empty()
//        return unit.enemiesNear().empty()
//            && unit.friendsNear().workers().notProtector().inRadius(6, unit).size() >= 2;
//    }
//
//    private Manager moveAway() {
//        unit.move(Select.mainOrAnyBuilding(), Actions.MOVE_SPACE, "Spacing...");
//        return usedManager(this);
//    }
}
