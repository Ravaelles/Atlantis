package atlantis.combat.advance.focus;

import atlantis.architecture.Manager;
import atlantis.combat.missions.MissionManager;
import atlantis.combat.squad.positioning.protoss.ProtossTooLonely;
import atlantis.units.AUnit;

public class HandleFocusPointPositioning extends MissionManager {
    public HandleFocusPointPositioning(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
//        if (Count.ourCombatUnits() >= 6 && unit.isSquadScout()) return false;

//        if (A.now() >= 50 && unit.isDragoon()) {
//            System.err.println("@ " + A.now() + " - " + unit.idWithHash() + " - .........");
//        }

        return true;
//        return unit.enemiesNear().inRadius(7, unit).empty();
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
//            MakeSpaceForNearbyWorkers.class,
//            MakeSpaceForWrongSideOfFocusFriends.class,
//            OnWrongSideOfFocusPoint.class,

            ProtossTooLonely.class,
            TooCloseToFocusPoint.class,
            TooFarFromFocusPoint.class,
        };
    }
}
