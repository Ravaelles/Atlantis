package atlantis.units.fix;

import atlantis.architecture.Manager;
import atlantis.architecture.generic.DoNothing;
import atlantis.combat.advance.AdvanceStandard;
import atlantis.combat.advance.focus.AFocusPoint;
import atlantis.combat.advance.focus.MoveToFocusPoint;
import atlantis.combat.advance.focus.TooFarFromFocusPoint;
import atlantis.combat.micro.attack.AttackNearbyEnemies;
import atlantis.combat.micro.generic.unfreezer.UnfreezeGeneric;
import atlantis.combat.micro.generic.unfreezer.Unfreezer;
import atlantis.combat.missions.Mission;
import atlantis.combat.missions.Missions;
import atlantis.combat.missions.attack.MissionAttack;
import atlantis.combat.squad.NewUnitsToSquadsAssigner;
import atlantis.combat.squad.TerranSquadCohesionManager;
import atlantis.combat.squad.positioning.SquadCohesion;
import atlantis.combat.squad.positioning.TooFarFromLeader;
import atlantis.game.A;
import atlantis.game.events.OnUnitCreated;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.util.log.ErrorLog;

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
        return unit.isActiveManager(DoNothing.class);
    }

    @Override
    public Manager handle() {
//        System.err.println("SAquad: " + unit.squad());
//        (new NewUnitsToSquadsAssigner(unit)).possibleCombatUnitCreated();

//        ErrorLog.printMaxOncePerMinute(A.now() + " PreventDoNothing for " + unit);

//        unit.managerLogs().addMessage("Squad:" + (unit.squad() == null ? "NONE..." : unit.squad().name()), unit);

        Manager manager;

        if ((manager = new AttackNearbyEnemies(unit)).invoke(this) != null) return usedManager(manager);

        if ((manager = new TooFarFromLeader(unit)).invoke(this) != null) return usedManager(manager);

        if (unit.mission() == null) {
            if ((manager = Missions.ATTACK.handleManagerClass(unit)) != null) return usedManager(manager);
        }

        if ((manager = new TooFarFromFocusPoint(unit)).invoke(this) != null) return usedManager(manager);

//
//        if ((manager = Missions.globalMission().handleManagerClass(unit)) != null) return usedManager(manager);
//
////
//        if ((manager = new Unfreezer(unit)).forceHandle() != null) return usedManager(manager);
//
////        if (A.everyNthGameFrame(3)) {
////            if ((new TooFarFromFocusPoint(unit)).forceHandle() != null) return usedManager(this);
////        }

        return null;
    }
}
