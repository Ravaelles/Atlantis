package tests.fakes;

import atlantis.map.MapTiles;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.constructions.position.AbstractPositionFinder;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.util.We;

/**
 * The stub world's answers to the tile questions, moved out of the
 * {@code Env.isTesting()} branches that used to sit in {@code HasPosition}. Same
 * answers, one place: everything is walkable and visible, buildable means "no unit
 * within 1.98 tiles", explored is whatever the harness's flag says, every tile is
 * connected to every other, and "can I build here" is approximated from the units
 * the world has.
 */
public class FakeMapTiles implements MapTiles.Source {

    /**
     * The harness answers from its own rules and has no map behind it, so the
     * engine fallback in {@link MapTiles#canBuildHere} must not apply here.
     */
    @Override
    public boolean spawnsFromMapData() {
        return false;
    }

    /**
     * What {@link #isExplored} answers. Tests that need a position which has never
     * been seen set it to {@code false} for the length of their world;
     * {@code AbstractTestWithUnits.setUp()} puts it back, which is why one test
     * leaving it set can no longer decide the next test's answers.
     */
    public static boolean EXPLORED = false;

    public static void installAsSource() {
        MapTiles.useSource(new FakeMapTiles());
    }

    @Override
    public boolean isWalkable(HasPosition at) {
        return true;
    }

    @Override
    public boolean isExplored(HasPosition at) {
        return EXPLORED;
    }

    @Override
    public boolean isVisible(HasPosition at) {
        return true;
    }

    @Override
    public boolean isBuildable(HasPosition at, boolean alsoCheckBuildings) {
        return Select.all().countInRadius(1.98, at) == 0;
    }

    @Override
    public boolean hasPathBetween(HasPosition from, HasPosition to) {
        // Everything is one connected map, which is what let tests place two
        // bases 40 tiles apart before this question had a name.
        return true;
    }

    /**
     * The stub world has no engine to ask, so this approximates the answer from
     * the units it does have - the body that used to live in
     * {@code CanPhysicallyBuildHere.apprxForTesting}, an {@code Env.isTesting()}
     * branch that put the harness's rules in production.
     */
    @Override
    public boolean canBuildHere(AUnit builder, AUnitType building, APosition at) {
        if (We.protoss() && building.needsPower()) {
            if (Select.ourOfType(AUnitType.Protoss_Pylon).countInRadius(5.98, at) == 0) {
                AbstractPositionFinder._STATUS = "[Testing] No power";
                return false;
            }
        }

        if (!isBuildable(at, true)) {
            AbstractPositionFinder._STATUS = "[Testing] Not buildable";
            return false;
        }

        int countNearBuildings = Select.ourBuildingsWithUnfinished().inRadius(2.95, at).count();
        if (countNearBuildings == 0) {
            return true;
        }

        AbstractPositionFinder._STATUS = "[Testing] Can't physically build here";
        return false;
    }
}