package atlantis.combat.micro.avoid.special;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;

public class SuicideAgainstScarabs extends Manager {

    public SuicideAgainstScarabs(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isGroundUnit();
    }

    /**
     * If a unit is targeted by a scarab, it's almost a death sentence. Worth thing that can happen
     * is that if there are ANY FRIENDS NEAR. Go ahead and take the bullet.
     */
    protected boolean update() {
        if (unit.isAir() || unit.isABuilding()) return false;

        AUnit scarabAimedAtUnit = unit.allUnitsNear().ofType(AUnitType.Protoss_Scarab)
            .havingTargeted(unit).nearestTo(unit);
        if (scarabAimedAtUnit == null) return false;

//        System.err.println();
//        System.err.println("#### SCARAB SUICIDE unit   = " + unit);
//        System.err.println("#### SCARAB SUICIDE scarab = " + scarabAimedAtUnit);
//        System.err.println("#### SCARAB SUICIDE target = " + scarabAimedAtUnit.target());
//        System.err.println();

        Selection friendsNear = unit.friendsInRadius(2.1).groundUnits().nonBuildings();

        if (shouldMoveTowardsScarab(scarabAimedAtUnit, friendsNear)) {
            moveNearbyFriendsIfNeeded(friendsNear);
            return unit.move(scarabAimedAtUnit, Actions.MOVE_SPECIAL, "SUICIDE", false);
        }

        return false;
    }

    private void moveNearbyFriendsIfNeeded(Selection friendsNear) {
        for (AUnit nearUnit : friendsNear.list()) {
            if (unit.distToLessThan(nearUnit, 2.1)) {
                nearUnit.runningManager().runFromAndNotifyOthersToMove(unit, "SuicideNear");
            }
        }
    }

    private boolean shouldMoveTowardsScarab(AUnit scarab, Selection friendsNear) {
        if (friendsNear.count() == 0) return false;

        return true;
    }
}