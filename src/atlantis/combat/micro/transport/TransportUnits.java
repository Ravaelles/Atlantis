package atlantis.combat.micro.transport;

import atlantis.debug.APainter;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.units.actions.UnitActions;
import bwapi.Color;

public class TransportUnits {

    public static boolean handleTransporting(AUnit transport, AUnit baby) {
        if (shouldLoadTheBaby(transport, baby)) {
            return loadTheBaby(transport, baby);
        }

        if (shouldDropTheBaby(transport, baby)) {
            return dropTheBaby(transport);
        }

        if (transport.hasCargo() && handleGoToSafety(transport, baby)) {
            return true;
        }

        if (followBaby(transport, baby)) {
            return true;
        }

        transport.setTooltip("Nothing");
        return false;
    }

    public static boolean loadRunningUnitsIntoTransport(AUnit unit) {
//        if (unit.cooldownRemaining() == 0) {
//            return false;
//        }

        if (unit.lastActionMoreThanAgo(8, UnitActions.LOAD)) {
            AUnit transport = Select.our().transports(true).inRadius(3, unit).nearestTo(unit);
            if (transport != null && transport.hasFreeSpaceFor(unit) && !transport.hasCargo()) {
                unit.load(transport);
                transport.load(unit);
                APainter.paintCircleFilled(unit, 7, Color.Blue);
                unit.setTooltip("Embark!");
                return true;
            }

            for (AUnit anotherTransport : Select.our().transports(true).inRadius(5, unit).list()) {
                if (anotherTransport.hasFreeSpaceFor(unit)) {
                    unit.load(anotherTransport);
                    anotherTransport.load(unit);
                    unit.setTooltip("Embark!");
                    return true;
                }
            }
        }

        return false;
    }

    // =========================================================

    private static boolean handleGoToSafety(AUnit transport, AUnit baby) {
        AUnit nearEnemy = Select.enemyCombatUnits().canAttack(baby, 5).nearestTo(transport);
        if (nearEnemy != null) {
            transport.moveAwayFrom(nearEnemy, 8, "ToSafety");
            APainter.paintLine(transport, transport.targetPosition(), Color.White);
            return true;
        }

        return false;
    }

    private static boolean isBabyInDanger(AUnit baby, boolean allowMoreDangerousBehavior) {
        double safetyMargin = (allowMoreDangerousBehavior ? 0.5 : 2.5) + baby.woundPercent() / 100;
        boolean enemiesNear = Select.enemyCombatUnits()
//                .inShootRangeOf((allowMoreDangerousBehavior ? 0.5 : 2.5) + baby.woundPercent() / 100, baby)
                .canAttack(baby, safetyMargin)
                .isNotEmpty();

        if (!allowMoreDangerousBehavior && baby.woundPercent() < 75 && enemiesNear) {
            return true;
        }

        if (baby.woundPercent() < 20 && enemiesNear) {
            return true;
        }

        return false;
    }

    private static boolean isTransportInDanger(AUnit transport) {
        if (transport.woundPercent() < 80) {
            return true;
        }

        return Select.enemyCombatUnits().canAttack(transport, 2.5).isNotEmpty();
    }

    private static boolean followBaby(AUnit transport, AUnit baby) {
        if (!baby.isLoaded() && (baby.isMoving() || transport.distToMoreThan(baby, 0.2))) {
            return transport.move(baby, UnitActions.MOVE, "Follow");
        }

        return false;
    }

    private static boolean shouldLoadTheBaby(AUnit transport, AUnit baby) {
//        System.out.println(baby.getID() + " baby.isUnderAttack(15) = " + baby.isUnderAttack(15));
        return !baby.isLoaded()
                && transport.hasFreeSpaceFor(baby)
//                && transport.lastActionMoreThanAgo(25, UnitActions.LOAD)
                && transport.lastActionMoreThanAgo(8, UnitActions.UNLOAD)
                && (baby.isUnderAttack(15))
//                && (baby.cooldownRemaining() > 0 && baby.lastStartedAttackMoreThanAgo(9) && baby.lastFrameOfStartingAttackMoreThanAgo(7))
                && (!isTransportInDanger(transport) && isBabyInDanger(baby, false));
    }

    private static boolean shouldDropTheBaby(AUnit transport, AUnit baby) {
        return baby.isLoaded()
                && transport.hasCargo()
//                && baby.cooldownRemaining() <= 8
                && transport.lastActionMoreThanAgo(25, UnitActions.LOAD)
                && (
                        isTransportInDanger(transport)
                        || transport.woundPercent() >= 87
                        || !isBabyInDanger(baby, false)
                        || transport.lastActionMoreThanAgo(30 * 12, UnitActions.LOAD)
                );
    }

    private static boolean loadTheBaby(AUnit transport, AUnit baby) {
        transport.load(baby);
        baby.load(transport);
        baby.runningManager().stopRunning();
        transport.setTooltip("LoadBaby");
        return true;
    }

    private static boolean dropTheBaby(AUnit transport) {
        AUnit baby = transport.loadedUnits().get(0);
        transport.unload(baby);
        baby.unload(transport);
        transport.setTooltip("DropBaby");
        return true;
    }

}
