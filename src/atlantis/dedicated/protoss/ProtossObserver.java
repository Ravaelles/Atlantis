package atlantis.dedicated.protoss;

import atlantis.combat.squad.Squad;
import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import atlantis.units.actions.UnitActions;

public class ProtossObserver {

    private static AUnit observerForArmy = null;
    private static AUnit observerForSquadScout = null;
    private static AUnit observerForBase = null;

    public static boolean update(AUnit observer) {
        if (detectInvisibleUnitsClosestToBase(observer)) {
            return true;
        }

        if (handleSpreadOut(observer)) {
            return true;
        }

        if (followSquadScout(observer)) {
            return true;
        }

        return followArmy(observer);
    }

    // =========================================================

    private static boolean handleSpreadOut(AUnit observer) {
        Select<AUnit> observers = Select.ourOfType(AUnitType.Protoss_Observer).inRadius(17, observer).exclude(observer);
        if (observers.count() > 0) {
            AUnit otherObserver = observers.nearestTo(observer);
            observer.moveAwayFrom(otherObserver.getPosition(), 5, "Spread observers");
            return true;
        }

        return false;
    }

    private static boolean followSquadScout(AUnit observer) {
        if (!observer.is(observerForSquadScout)) {
            return false;
        }

        AUnit scout = Squad.getAlphaSquad().getSquadScout();
        if (scout != null) {
            observerForSquadScout = observer;
            if (scout.distTo(observer) > 1) {
                observerForSquadScout.move(scout, UnitActions.MOVE, "Follow scout");
            }
            return true;
        }

        return false;
    }

    private static boolean followArmy(AUnit observer) {
        if (!observer.is(observerForArmy)) {
            return false;
        }

        APosition goTo = Squad.getAlphaSquad().center();
        if (goTo != null) {
            observerForArmy = observer;
            if (goTo.distTo(observer) > 1) {
                observerForArmy.move(goTo, UnitActions.MOVE, "Follow army");
            }
            return true;
        }

        return false;
    }

    private static boolean detectInvisibleUnitsClosestToBase(AUnit observer) {
        if (Select.mainBase() == null) {
            return false;
        }

        if (!observer.equals(Select.ourOfType(AUnitType.Protoss_Observer).first())) {
            return false;
        }

        if (!observer.is(observerForBase)) {
            return false;
        }

        AUnit dangerousInvisibleEnemy = enemyDangerousHiddenUnit();
        if (dangerousInvisibleEnemy != null) {
            observerForBase = observer;
            if (observerForBase.distTo(dangerousInvisibleEnemy) > 0.2) {
                observerForBase.move(dangerousInvisibleEnemy.getPosition(), UnitActions.MOVE, "Reveal enemy in base");
            }
            return true;
        }

        return false;
    }

    private static AUnit enemyDangerousHiddenUnit() {
        AUnit invisibleUnit = Select.enemy().effCloaked().combatUnits().nearestTo(Select.mainBase());
        if (invisibleUnit != null) {
            return invisibleUnit;
        }

        AUnit lurker = Select.enemy().ofType(AUnitType.Zerg_Lurker).nearestTo(Select.mainBase());
        if (lurker != null) {
            return lurker;
        }

        AUnit terranCloaked = Select.enemy().effCloaked().ofType(AUnitType.Terran_Wraith, AUnitType.Terran_Ghost).nearestTo(Select.mainBase());
        if (terranCloaked != null) {
            return terranCloaked;
        }

        return Select.enemy().effCloaked().ofType(
            AUnitType.Protoss_Dark_Templar
        ).nearestTo(Select.mainBase());
    }

}
