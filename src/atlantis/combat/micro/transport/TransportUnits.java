package atlantis.combat.micro.transport;

import atlantis.debug.APainter;
import atlantis.units.AUnit;
import atlantis.units.Select;
import atlantis.units.actions.UnitActions;
import atlantis.util.A;
import bwapi.Color;

public class TransportUnits {

    public static boolean transport(AUnit transport, AUnit baby) {
        if (A.everyNthGameFrame(10)) {
            if (shouldLiftTheBaby(transport, baby)) {
                return liftTheBaby(transport, baby);
            } else if (shouldDropTheBaby(transport, baby)) {
                return dropTheBaby(transport, baby);
            }
        }

        if (transport.hasCargo() && handleGoToSafety(transport, baby)) {
            return true;
        }

        if (followBaby(transport, baby)) {
            return true;
        }

        return false;
    }

    // =========================================================

    private static boolean handleGoToSafety(AUnit transport, AUnit baby) {
        AUnit nearEnemy = Select.enemyCombatUnits().canShootAt(baby, 5).nearestTo(transport);
        if (nearEnemy != null) {
            transport.moveAwayFrom(nearEnemy, 6, "Fly");
            APainter.paintLine(transport, transport.getTargetPosition(), Color.White);
            return true;
        }

        return false;
    }

    private static boolean isBabyInDanger(AUnit baby) {
        if (baby.woundPercent() < 75) {
            return false;
        }

        return Select.enemyCombatUnits().inShootRangeOf(1, baby).isNotEmpty();
    }

    private static boolean isTransportInDanger(AUnit transport) {
        if (transport.woundPercent() < 80) {
            return false;
        }

        return Select.enemyCombatUnits().inShootRangeOf(2, transport).isNotEmpty();
    }

    private static boolean followBaby(AUnit transport, AUnit baby) {
        if (!baby.isLoaded() && transport.distToMoreThan(baby, 0.2)) {
            transport.move(baby, UnitActions.MOVE, null);
            return true;
        }

        return false;
    }

    private static boolean shouldLiftTheBaby(AUnit transport, AUnit baby) {
        return !baby.isLifted()
                && transport.hasFreeSpaceFor(baby)
                && transport.lastActionMoreThanAgo(10, UnitActions.LOAD)
                && transport.lastActionMoreThanAgo(25, UnitActions.UNLOAD)
                && (!isTransportInDanger(transport) && isBabyInDanger(baby));
    }

    private static boolean shouldDropTheBaby(AUnit transport, AUnit baby) {
        return baby.isLifted()
                && transport.hasCargo()
                && transport.lastActionMoreThanAgo(150, UnitActions.LOAD)
                && transport.lastActionMoreThanAgo(40, UnitActions.UNLOAD)
                && (isTransportInDanger(transport) || !isBabyInDanger(baby));
    }

    private static boolean liftTheBaby(AUnit transport, AUnit baby) {
        transport.load(baby);
        baby.load(transport);
        transport.setTooltip("LiftBaby");
        System.out.println("LIFT " + baby.getID());
        return true;
    }

    private static boolean dropTheBaby(AUnit transport, AUnit baby) {
        transport.unload(transport.loadedUnits().get(0));
        transport.setTooltip("DropBaby");
        System.out.println("UNLOAD " + baby.getID());
        return true;
    }

}
