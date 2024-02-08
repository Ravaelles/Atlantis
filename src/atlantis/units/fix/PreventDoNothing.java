package atlantis.units.fix;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;

/**
 * Porblematic thing that happens for new units (e.g. Marines) when they come out and just stand outside Barracks.
 */
public class PreventDoNothing extends Manager {
    public PreventDoNothing(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
//        if (true) return false;

//        return unit.isActiveManager(DoNothing.class) || unit.isAction(Actions.INIT);

        if (!unit.isStopped()) return false;
        if (unit.action().equals(Actions.INIT)) return true;
//        if (unit.isActiveManager(DoNothing.class)) return true;

        if (unit.isSpecialAction()) return false;
        if (unit.isMissionSparta()) return false;

        return (unit.lastActionMoreThanAgo(30 * 3));
    }

    @Override
    public Manager handle() {
//        System.err.println("@ " + A.now() + " - " + unit.id() + " - PreventDoNothing");

//        System.err.println("SAquad: " + unit.squad());
//        (new NewUnitsToSquadsAssigner(unit)).possibleCombatUnitCreated();

//        ErrorLog.printMaxOncePerMinute(A.now() + " PreventDoNothing for " + unit);

//        unit.managerLogs().addMessage("Squad:" + (unit.squad() == null ? "NONE..." : unit.squad().name()), unit);

        Manager manager;
//        boolean isLeader = unit.squad().isLeader(unit);
//
//        if (!isLeader) {
//            if ((manager = new TooFarFromLeader(unit)).invoke(this) != null) return usedManager(manager);
//        }
//
//        if ((manager = new AttackNearbyEnemies(unit)).invoke(this) != null) return usedManager(manager);
//
//        if (unit.mission() == null) {
//            if ((manager = Missions.ATTACK.handleManagerClass(unit)) != null) return usedManager(manager);
//        }
//
////        if ((manager = new TooFarFromFocusPoint(unit)).invoke(this) != null) return usedManager(manager);
//        if ((manager = new TooFarFromFocusPoint(unit)).forceHandle() != null) return usedManager(manager);
//
//        if (!isLeader) {
//            if ((manager = new TooFarFromLeader(unit)).forceHandle() != null) return usedManager(manager);
//        }
////
////        if ((manager = Missions.globalMission().handleManagerClass(unit)) != null) return usedManager(manager);
////
//////
////        if ((manager = new Unfreezer(unit)).forceHandle() != null) return usedManager(manager);
////
//////        if (A.everyNthGameFrame(3)) {
//////            if ((new TooFarFromFocusPoint(unit)).forceHandle() != null) return usedManager(this);
//////        }
//
//        System.err.println("LOL NOTHING! " + unit);
//
//        if (A.now() % 8 <= 3) {
//            if ((new MissionAttackManager(unit)).forceHandle() != null) return usedManager(this);
//        }
//        else {
//            if ((new MissionDefendManager(unit)).forceHandle() != null) return usedManager(this);
//        }
//

        if (DoPreventFreezesLogic.handle(unit)) return usedManager(this);

//        System.err.println("OMFG STILL NOTHING! " + unit);

        return null;
    }
}
