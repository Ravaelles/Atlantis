package atlantis.combat.micro.avoid.special;

import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;

public class SuicideAgainstScarabs {

    /**
     * If a unit is targeted by a scarab, it's almost a death sentence. Worth thing that can happen
     * is that if there are ANY FRIENDS NEAR. Go ahead and take the bullet.
     */
    protected static boolean update(AUnit unit) {
        if (unit.isAir() || unit.isBuilding()) {
            return false;
        }

        AUnit scarabAimedAtUnit = unit.enemiesNear().ofType(AUnitType.Protoss_Scarab).havingTarget(unit).nearestTo(unit);
        if (scarabAimedAtUnit == null) {
            return false;
        }

        Selection friendsNear = unit.friendsInRadius(2.1).groundUnits().nonBuildings();

        if (shouldMoveTowardsScarab(unit, scarabAimedAtUnit, friendsNear)) {
            moveNearbyFriendsIfNeeded(friendsNear, unit);
            return unit.move(scarabAimedAtUnit, Actions.MOVE_SPECIAL, "SUICIDE", false);
        }

        return false;
    }

    private static void moveNearbyFriendsIfNeeded(Selection friendsNear, AUnit unit) {
        for (AUnit nearUnit : friendsNear.list()){
            if (unit.distToLessThan(nearUnit, 2.1)) {
                nearUnit.runningManager().runFromAndNotifyOthersToMove(unit);
                nearUnit.setTooltip("SuicideNear");
            }
        }
    }

    private static boolean shouldMoveTowardsScarab(AUnit unit, AUnit scarab, Selection friendsNear) {
        if (friendsNear.count() == 0) {
            return false;
        }

        return true;
    }
}