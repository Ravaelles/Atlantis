package atlantis.special;

import atlantis.combat.squad.ASquadManager;
import atlantis.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Select;
import atlantis.units.actions.UnitActions;

public class ProtossObserverManager {

    public static boolean update(AUnit observer) {
        if (detectInvisibleUnitsClosestToBase(observer)) {
            return true;
        }

        if (followSquads(observer)) {
            return true;
        }

        return false;
    }

    // =========================================================

    private static boolean followSquads(AUnit observer) {
        APosition goTo = ASquadManager.getAlphaSquad().getMedianUnitPosition();
        if (goTo != null) {
            observer.move(goTo, UnitActions.MOVE, "Follow");
            return true;
        }

        return false;
    }

    private static boolean detectInvisibleUnitsClosestToBase(AUnit observer) {
        if (Select.mainBase() == null) {
            return false;
        }

        AUnit dangerousInvisibleEnemy = enemyCloakableUnit();
        if (dangerousInvisibleEnemy != null) {
            observer.move(dangerousInvisibleEnemy.getPosition(), UnitActions.MOVE, "Reveal");
            return true;
        }

        return false;
    }

    private static AUnit enemyCloakableUnit() {
        AUnit invisibleUnit = Select.enemy().invisible().combatUnits().nearestTo(Select.mainBase());
        if (invisibleUnit != null) {
            return invisibleUnit;
        }

        return Select.enemy().ofType(
                AUnitType.Zerg_Lurker,
                AUnitType.Protoss_Dark_Templar
        ).nearestTo(Select.mainBase());
    }

}
