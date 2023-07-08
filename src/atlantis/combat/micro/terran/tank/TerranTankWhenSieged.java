package atlantis.combat.micro.terran.tank;

import atlantis.combat.missions.Missions;
import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.managers.Manager;

public class TerranTankWhenSieged extends TerranTank {

    protected static Class[] managers = {
        TankRunning.class,
        WouldBlockChoke.class,
        DontThinkAboutUnsieging.class,
        SiegeHereDuringMissionDefend.class,
        UnsiegeToReposition.class,
        SiegeHereDuringMissionDefend.class,
    };

    public TerranTankWhenSieged(AUnit unit) {
        super(unit);
    }

//    public Manager handle() {
//        if (actWithSubmanagers() != null) {
//            return lastManager();
//        }
//
//        // Mission is CONTAIN
////        if (
////            Missions.isGlobalMissionContain()
////                && unit.squad().distToFocusPoint() < 7.9
////                && unit.lastAttackOrderLessThanAgo(7 * 30)
////        ) {
////            return false;
////        }
//
//        return null;
//    }
}
