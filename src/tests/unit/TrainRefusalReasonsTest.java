package tests.unit;

import atlantis.production.v2.ProductionItem;
import atlantis.production.v2.ProductionPlan;
import atlantis.production.v2.UnitProducible;
import atlantis.production.v2.execution.DispatchResult;
import atlantis.production.v2.execution.GameOrderDirector;
import atlantis.production.v2.execution.ProductionDispatcher;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.OrderFallback;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * A refused train names its door (measured live: bank 3800 behind 3
 * idle Gateways reading as one shape, 175 identical "no free facility"
 * rows). The dispatcher used to collapse resolve-failures, unfinished
 * facilities, same-frame doubles, training dedups and engine refusals
 * into one label - each branch below rows its own reason, behavior
 * unchanged.
 */
@Tag("production")
public class TrainRefusalReasonsTest extends AbstractTestWithWorld {

    @Override
    protected FakeUnit[] generateOur() {
        return null;
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return null;
    }

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @AfterEach
    public void dropSink() {
        OrderFallback.useNoSink();
    }

    private static FakeUnit gateway(double tx, double ty) {
        return fake(AUnitType.Protoss_Gateway, tx, ty);
    }

    private static List<DispatchResult> dispatch(GameOrderDirector director, AUnitType type, int producerId) {
        ProductionItem item = new ProductionItem(
            UnitProducible.of(type), 0, false, null, producerId);
        ProductionPlan plan = new ProductionPlan();
        plan.add(item);

        return new ProductionDispatcher(director).dispatch(plan, 3, 10);
    }

    private static String detailOf(List<DispatchResult> results) {
        assertEquals(1, results.size());

        return results.get(0).detail();
    }

    @Test
    public void missingFacilityNamesItself() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);

        world(5, fakeOurs(
            nexus,
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            List<DispatchResult> results = dispatch(
                new GameOrderDirector(), AUnitType.Protoss_Zealot,
                atlantis.production.v2.ProductionItem.NO_PRODUCER);

            assertFalse(results.get(0).issued());
            assertEquals("no such facility", detailOf(results));
        });
    }

    @Test
    public void unfinishedFacilityNamesItself() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit warping = gateway(22, 20);
        warping.setCompleted(false);

        world(5, fakeOurs(
            nexus,
            warping
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            List<DispatchResult> results = dispatch(
                new GameOrderDirector(), AUnitType.Protoss_Zealot, warping.id());

            assertFalse(results.get(0).issued());
            assertTrue(detailOf(results).startsWith("facility unfinished #"),
                "the unfinished warp names itself: " + detailOf(results));
        });
    }

    @Test
    public void secondOrderSameFrameNamesItself() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit free = gateway(22, 20);

        world(5, fakeOurs(
            nexus,
            free
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            OrderFallback.useSink(new OrderFallback.Sink() {
                @Override
                public boolean train(AUnitType unitToTrain) {
                    return true;
                }

                @Override
                public boolean cancelConstruction(AUnit unit) {
                    return false;
                }
            });

            try {
                GameOrderDirector director = new GameOrderDirector();
                List<DispatchResult> first = dispatch(director, AUnitType.Protoss_Zealot, free.id());

                assertTrue(first.get(0).issued());
                assertEquals("trained", first.get(0).detail());

                List<DispatchResult> second = dispatch(director, AUnitType.Protoss_Zealot, free.id());

                assertFalse(second.get(0).issued());
                assertTrue(detailOf(second).startsWith("already commanded #"),
                    "the same-frame double names itself: " + detailOf(second));
            } finally {
                OrderFallback.useNoSink();
            }
        });
    }

    @Test
    public void engineRefusalNamesItself() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit free = gateway(22, 20);

        world(5, fakeOurs(
            nexus,
            free
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            OrderFallback.useNoSink();

            List<DispatchResult> results = dispatch(
                new GameOrderDirector(), AUnitType.Protoss_Zealot, free.id());

            assertFalse(results.get(0).issued());
            assertTrue(detailOf(results).startsWith("engine refused #"),
                "the engine refusal names the facility: " + detailOf(results));
        });
    }
}
