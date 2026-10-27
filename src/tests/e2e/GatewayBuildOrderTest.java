package tests.e2e;

import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionOrderStatus;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.workers.WorkerManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeOrderSink;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A builder standing next to its own buildable site must eventually ISSUE the
 * build order.
 *
 * <p>
 * <b>Measured (owner's report 2026-10-11,
 * {@code /sc-ai/BOTS/AtlantisOpenBW/E2E_LOGS/latest}):</b>
 * </p>
 *
 * <pre>
 *   Cancel construction: Gateway / Gateway took too long (43s) / at:[14,51] / buildable:true
 *   Builder diagnostic: unit=#126 Probe @[15,52] (BuilderManager)
 *       construction=Gateway pos=[14,51] managerHistory=Log{
 *         3473: GatherResources (Gatherer!),
 *         3599: BuilderManager,
 *       }
 * </pre>
 *
 * <p>
 * The worker is ONE tile from its Gateway, the tile is {@code buildable:true},
 * the minerals are
 * there - and 43 game-seconds later the construction is cancelled for "taking
 * too long". The
 * same tile is then re-requested and the cycle repeats, which is why Gateways
 * churn even in
 * runs that do build some.
 * </p>
 *
 * <p>
 * <b>The suspected gate</b> is {@code IssueBuildOrder.considerIssuingNow}'s
 * {@code unit.lastActionMoreThanAgo(20)}: the build is only attempted when the
 * worker's
 * CURRENT action is 20+ frames old, while
 * {@code TravelToConstruct.moveToConstruct} re-issues
 * the move every 13th frame and sets {@code MOVE_BUILD} - so the action is
 * refreshed before it
 * can ever age past 20 and the build order is never reached. That is the same
 * shape as the
 * already-fixed {@code asProtossMultiBuilderDoNotSwitchConstructions}
 * self-refreshing debounce.
 * </p>
 *
 * <p>
 * This test does not assert the fix's mechanism, only the OUTCOME the bug
 * violates: over a
 * long-enough window, the real worker manager chain must produce a
 * {@code build} order (or the
 * construction must not be cancelled for inactivity) for a builder parked on a
 * buildable site.
 * </p>
 */
public class GatewayBuildOrderTest extends AbstractTestWithWorld {

    private Construction construction;

    @Override
    protected FakeUnit[] generateOur() {
        return null;
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return null;
    }

    @AfterEach
    public void clean() {
        ConstructionRequests.constructions.clear();
    }

    @Test
    public void aBuilderOnItsOwnBuildableSiteEventuallyIssuesTheBuild() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 14, 10);

        // The stub world starts with no minerals, which makes the very first gate in
        // IssueBuildOrder (`A.canAfford`) false and the manager report `CantAffordGatewayYet`
        // - a harness artifact, NOT the live condition. The live log has 974 minerals on the
        // bank while the Gateway churns, so affordability is given here on purpose; otherwise
        // this test would "fail" for a reason the game never has.
        world(400, units(nexus, probe), new FakeUnit[0], () -> {
            currentMinerals = 800;

            if (A.now() == 5) {
                construction = new Construction(AUnitType.Protoss_Gateway);
                construction.setBuilder(probe);
                construction.setPositionToBuild(APosition.create(14, 10));
                construction.setStatus(ConstructionOrderStatus.NOT_STARTED);
                ConstructionRequests.constructions.add(construction);

                return;
            }

            if (A.now() < 100)
                return;

            AUnit myProbe = Select.ourWorkers().first();
            if (myProbe == null) return;

            // Drive the REAL manager chain, exactly as the production pipeline does.
            new WorkerManager(myProbe).forceHandle();

            FakeOrderSink sink = (FakeOrderSink) myProbe.orderSink();
            boolean built = sink.orders().stream()
                .anyMatch(o -> o.operation.equals("build"));

            assertTrue(built,
                "a builder one tile from its own buildable Gateway site must issue a build"
                    + " order within the window; measured instead: 43 game-seconds of"
                    + " GatherResources/BuilderManager alternating, no build, then"
                    + " 'Gateway took too long (43s) / buildable:true'. Orders seen: "
                    + sink.orders()
                    + " | manager history: " + myProbe.managerLogs());
        });
    }
}
