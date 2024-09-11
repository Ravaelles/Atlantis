package jfap.tweaks;

import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.HasUnit;
import atlantis.units.select.Selection;
import atlantis.util.Vector;

/**
 * JFAP has a bug where if there's a combat building (e.g. Sunken) 7.1 tiles away from a marine (Sunken has range 7),
 * it will think that the marine is perfectly safe. However, once the marine moves 7 tiles closer, the sunken
 * will kill the marine. We need to translate the Sunken position closer to the marine, so that JFAP can detect
 * the danger.
 */
public class MoveCombatBuildingsCloserToOurUnits extends HasUnit {
    private static final double MARGIN = 3.1;

    public MoveCombatBuildingsCloserToOurUnits(AUnit unit) {
        super(unit);
    }

    public Vector vectorTowardsOurUnits() {
//        Selection ours = unit.enemiesNear().inRadius(7 + MARGIN, unit);
        Selection ours = unit.enemiesNear();

//        System.err.println("unit = " + unit);
//        System.err.println("ours = " + ours.count());
        if (ours.empty()) return null;

        APosition ourCenter = ours.center();

        return new Vector(ourCenter.x - unit.x(), ourCenter.y - unit.y())
            .normalizeTo1()
            .multiplyVector(MARGIN);
    }
}
