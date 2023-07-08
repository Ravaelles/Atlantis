package atlantis.combat.managers;

import atlantis.combat.micro.avoid.special.AvoidCriticalUnits;
import atlantis.combat.micro.avoid.special.AvoidSpellsAndMines;
import atlantis.combat.micro.managers.DanceAfterShoot;
import atlantis.combat.micro.managers.StopAndShoot;
import atlantis.combat.micro.transport.TransportUnits;
import atlantis.combat.running.ShouldStopRunning;
import atlantis.units.AUnit;
import atlantis.units.interrupt.DontDisturbInterrupt;
import atlantis.architecture.Manager;

public class CombatManagerTopPriority extends Manager {

    public CombatManagerTopPriority(AUnit unit) {
        super(unit);
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            AvoidSpellsAndMines.class,
            AvoidCriticalUnits.class,
            DanceAfterShoot.class,
            StopAndShoot.class,
            DontDisturbInterrupt.class,
            TransportUnits.class,
//            Unfreezer.class,
            ShouldStopRunning.class,
        };
    }

//    private boolean handledTopPriority() {
//        if (AvoidSpellsAndMines.avoidSpellsAndMines()) {
//            return true;
//        }
//
//        if (AvoidCriticalUnits.update()) {
//            return true;
//        }
//
//        if (DanceAfterShoot.update()) {
//            return true;
//        }
//
//        if (StopAndShoot.update()) {
//            return true;
//        }
//
//        if (DontDisturbInterrupt.dontInterruptImportantActions()) {
//            return true;
//        }
//
//        if ((unit.isMoving() && !unit.isAttackingOrMovingToAttack()) && TransportUnits.handleLoad()) {
//            return true;
//        }
//
//        if (unit.isLoaded() && TransportUnits.unloadFromTransport()) {
//            return true;
//        }
//
//        // Handle units getting bugged by Starcraft
////        if (Unfreezer.handleUnfreeze()) {
////            return true;
////        }
//
//        if (unit.isRunning()) {
//            if (ShouldStopRunning.shouldStopRunning()) {
//                unit.runningManager().stopRunning();
//            }
//            //        if (unit.isRunning() && unit.lastStartedRunningLessThanAgo(2)) {
//            else if (A.everyNthGameFrame(3)) {
//                //            unit.setTooltip("Running(" + A.digit(unit.distTo(unit.getTargetPosition())) + ")");
//                //            return A.everyNthGameFrame(2) ? AAvoidUnits.avoidEnemiesIfNeeded() : true;
//                return AvoidEnemies.avoidEnemiesIfNeeded();
//            }
//        }
//
//        // Useful for testing and debugging of shooting/running
////        if (testUnitBehaviorShootAtOwnUnit()) { return true; };
//
//        return false;
//    }

//    public Manager handle() {
//        if () {
//            return usedManager(this);
//        }
//
//        return null;
//    }
}

