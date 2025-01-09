package atlantis.combat.running;

import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.select.Selection;

public class SeparateEarlyFromFriends {
    public static APosition modifyPositionSlightly(HasPosition runTo, HasPosition runAwayFrom, ARunningManager running) {
        if (runTo == null) return null;

        if (true) return runTo.position();

        Selection nearFriends = running.unit().friendsNear().groundUnits().inRadius(0.4, running.unit());

        if (nearFriends.atLeast(1)) {
            AUnit nearestFriend = nearFriends.nearestTo(running.unit());
//            APainter.paintCircleFilled(running.unit, 6, Color.Green);
//            APainter.paintCircleFilled(nearestFriend, 6, Color.Teal);
//            System.err.println("PRE = " + runTo.toStringPixels());
            runTo = runTo.translatePercentTowards(nearestFriend, -20);
//            System.err.println("POST = " + runTo.toStringPixels());
        }

        return runTo.position();
    }
}
