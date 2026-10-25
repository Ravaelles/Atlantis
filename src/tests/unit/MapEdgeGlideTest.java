package tests.unit;

import atlantis.combat.BattleCommander;
import atlantis.combat.cluster.FightOrFlight;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.map.position.PositionFinder;
import atlantis.map.region.ARegion;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;
import tests.fakes.FakeOrderSink;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.fakes.FakePositionFinder.installAsSource;

/**
 * A glide destination the map cannot host (corner pocket: the spiral
 * inside ensureWithinBounds exhausts and answers null - measured live
 * as error spam plus an idle Dragoon) must not hold the unit still.
 * The attack order below fires instead; the engine chases on its own.
 */
@Tag("combat")
public class MapEdgeGlideTest extends AbstractTestWithWorld {

    @Override
    protected FakeUnit[] generateOur() {
        return null;
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return null;
    }

    @AfterEach
    public void restorePositions() {
        installAsSource();
    }

    @Test
    public void exhaustedSpiralFallsThroughToAttackInsteadOfIdling() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 34.5, 20);
        dragoon.cooldown = 0;

        world(1, fakeOurs(dragoon), fakeEnemies(zealot), () -> {
            PositionFinder.useSource(new PositionFinder.Source() {
                @Override
                public APosition makeBuildable(HasPosition origin, int maxRadius) {
                    return origin.position();
                }

                @Override
                public APosition ensureWithinBounds(HasPosition origin) {
                    return null;
                }

                @Override
                public APosition makeValidFarFromBounds(HasPosition origin, int atLeastTilesAwayFromBounds) {
                    return origin.position();
                }

                @Override
                public APosition makeBuildableFarFromBounds(HasPosition origin, int atLeastTilesAwayFromBounds) {
                    return origin.position();
                }

                @Override
                public APosition makeWalkable(HasPosition origin, int maxRadius, int step, ARegion sameRegion) {
                    return origin.position();
                }

                @Override
                public APosition makeLandableFor(HasPosition origin, AUnit building) {
                    return origin.position();
                }

                @Override
                public APosition makeWalkableAndFreeOfAnyGroundUnits(
                    HasPosition origin, double maxRadius, double step, AUnit exceptUnit
                ) {
                    return origin.position();
                }
            });

            try {
                BattleCommander.act(
                    dragoon, FightOrFlight.Tactic.FIGHT, null, dragoon);

                assertTrue(
                    hasOrder(ordersOf(dragoon), dragoon.id(), "attackUnit"),
                    "no glide point: the attack order fires instead of idling: "
                        + ordersOf(dragoon));
                assertFalse(
                    hasOrder(ordersOf(dragoon), dragoon.id(), "move"),
                    "no glide move without a destination: " + ordersOf(dragoon));
            }
            finally {
                installAsSource();
            }
        });
    }

    private static boolean hasOrder(List<FakeOrderSink.Record> orders, int unitId, String operation) {
        for (FakeOrderSink.Record record : orders) {
            if (record.actorId == unitId && record.operation.equals(operation)) return true;
        }
        return false;
    }

    private static List<FakeOrderSink.Record> ordersOf(FakeUnit unit) {
        return ((FakeOrderSink) unit.orderSink()).orders();
    }
}
