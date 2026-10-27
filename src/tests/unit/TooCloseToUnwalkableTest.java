package tests.unit;

import atlantis.map.position.APosition;
import atlantis.production.constructions.position.AbstractPositionFinder;
import atlantis.production.constructions.position.conditions.TooCloseToUnwalkable;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.fakes.TileMapSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pins the "a production building may not be boxed in by unwalkable terrain"
 * rule.
 *
 * <p>
 * The class is a pure predicate over a position and the tile map, so it is
 * tested
 * with {@link TileMapSource} rather than a game - which is the point: this
 * module
 * went a long time with no cheap tests and collected unit errors because of it.
 * </p>
 *
 * <p>
 * What it actually decides, read from the implementation and pinned below:
 * </p>
 * <ul>
 * <li>a base is never judged (it defines the ground it stands on);</li>
 * <li>a building that produces no land units is never judged;</li>
 * <li>otherwise a building whose <b>left and down</b> neighbours are both
 * unwalkable
 * is refused, and likewise when <b>up and right</b> are both unwalkable - the
 * "boxed into a corner" case, not "next to any obstacle";</li>
 * <li>and independently, the position must have walkable space around it
 * ({@code position.isWalkable(4)}).</li>
 * </ul>
 */
@Tag("map")
public class TooCloseToUnwalkableTest {

    private static final AUnitType GATEWAY = AUnitType.Protoss_Gateway;
    private static final AUnitType NEXUS = AUnitType.Protoss_Nexus;
    private static final AUnitType PYLON = AUnitType.Protoss_Pylon;

    private TileMapSource map;

    @BeforeEach
    public void setUp() {
        map = TileMapSource.everythingFine().install();
        AbstractPositionFinder._STATUS = "Init";
    }

    @AfterEach
    public void tearDown() {
        TileMapSource.uninstall();
    }

    /**
     * A Gateway on open ground is fine, and the status is left as the caller set
     * it.
     */
    @Test
    public void aGatewayOnOpenGroundIsAccepted() {
        assertFalse(
            TooCloseToUnwalkable.isTooCloseToUnwalkable(GATEWAY, APosition.create(50, 50)),
            "open ground is not too close to unwalkable terrain");
    }

    /**
     * The corner case the rule exists for: unwalkable on the left AND below means
     * the building has no approach from those two sides.
     *
     * <p>
     * The neighbours are probed at the building's own extent plus a couple of
     * tiles,
     * so the blocked tiles are placed generously rather than guessed at the exact
     * pixel: what matters is the <i>both-sides</i> semantics.
     * </p>
     */
    @Test
    public void unwalkableOnTheLeftAndBelowIsRefused() {
        map.blockRect(20, 48, 46, 80);

        assertTrue(
            TooCloseToUnwalkable.isTooCloseToUnwalkable(GATEWAY, APosition.create(50, 50)),
            "unwalkable on both the left and the down side must be refused");
        assertTrue(
            AbstractPositionFinder._STATUS.contains("unwalkable"),
            "the refusal must name unwalkable terrain, got: " + AbstractPositionFinder._STATUS);
    }

    /**
     * One blocked side is not enough: the rule is specifically about being closed
     * in
     * from a pair of sides. This is the assertion that stops someone "fixing" it
     * into
     * "any obstacle nearby", which would refuse most of a real map.
     */
    @Test
    public void unwalkableOnOnlyOneSideIsNotRefused() {
        map.blockRect(20, 48, 46, 52); // left only

        assertFalse(
            TooCloseToUnwalkable.isTooCloseToUnwalkable(GATEWAY, APosition.create(50, 60)),
            "one blocked side is not being boxed in - the rule needs a pair of sides");
    }

    /**
     * A base is never judged: it defines the terrain it sits on.
     */
    @Test
    public void aBaseIsNeverJudged() {
        map.defaultWalkable(false);

        assertFalse(
            TooCloseToUnwalkable.isTooCloseToUnwalkable(NEXUS, APosition.create(50, 50)),
            "a Nexus is the base itself and is exempt from this rule");
    }

    /**
     * A Pylon does not produce land units, so it is outside this rule's scope.
     * Worth
     * pinning because the Pylon is the building the OpenBW runs actually fail on,
     * and
     * a change that dragged it into the rule would be easy to make and hard to see.
     */
    @Test
    public void aBuildingThatProducesNoLandUnitsIsNotJudged() {
        map.defaultWalkable(false);

        assertFalse(
            TooCloseToUnwalkable.isTooCloseToUnwalkable(PYLON, APosition.create(50, 50)),
            "a Pylon produces no land units and is out of this rule's scope");
    }

    /**
     * The status is only written when the rule refuses, so an accept leaves it
     * alone.
     */
    @Test
    public void anAcceptedPositionDoesNotOverwriteTheStatus() {
        AbstractPositionFinder._STATUS = "Untouched";

        TooCloseToUnwalkable.isTooCloseToUnwalkable(GATEWAY, APosition.create(50, 50));

        assertEquals("Untouched", AbstractPositionFinder._STATUS,
            "an accepted position must not claim a status of its own");
    }
}
