package tests.unit;

import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionOrderStatus;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.constructions.commanders.CancelTooLongConstructions;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A construction that is WAITING FOR RESOURCES must not be cancelled as "took
 * too long".
 *
 * <p>
 * <b>Measured (owner's 1209-second run, {@code out/openw/bot.log}):</b>
 * </p>
 *
 * <pre>
 *   ### Protoss: Nexus=1 Probe=25 Pylon=4 ###          &lt;- no Gateway at all
 *   Cancel construction: Gateway / Gateway took too long (43s) / at:[14,51] / buildable:true
 * </pre>
 *
 * <p>
 * The Gateway appears in the whole log exactly twice, both as that cancellation
 * - there was
 * never a single build attempt. The cause is two mechanisms that contradict
 * each other:
 * </p>
 *
 * <ul>
 * <li>{@code ShouldNotTravelToConstructYet} deliberately holds a worker back
 * until the
 * building is affordable (before 300s game time), so {@code NOT_STARTED} for a
 * while is
 * the DESIGNED state, not a stall;</li>
 * <li>{@code CancelTooLongConstructions.tookTooLong} measures from
 * {@code timeOrdered}
 * (construction creation) against a fixed {@code 14 + 14 + 2*distanceToMain}
 * seconds,
 * which does not know the difference between "the builder is stuck" and "we are
 * saving
 * up for it".</li>
 * </ul>
 *
 * <p>
 * When the timeout wins, the reaction is destructive:
 * {@code constructionCancelledRequestAgainBecauseItsImportant}
 * calls {@code CancelNotStarted.cancel(type, ...)}, which cancels every
 * not-started Gateway
 * and re-queues - so the Gateway restarts its clock, waits again, and is
 * cancelled again.
 * That is why Gateways can end at zero while the build order clearly lists
 * them.
 * </p>
 *
 * <p>
 * The contract pinned here: a construction whose builder is alive and assigned,
 * and which is
 * merely not affordable yet, is NOT overdue.
 * </p>
 */
public class CancelTooLongTest extends AbstractTestWithWorld {

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
    public void cleanConstructions() {
        ConstructionRequests.constructions.clear();
    }

    @Test
    public void aConstructionWaitingForAffordabilityIsNotCancelledForTakingTooLong() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 11);

        world(4000, units(nexus, probe), new FakeUnit[0], () -> {
            // Created early and asserted late, so the construction really is ~90 seconds old
            // and the timeout formula (14 + 14 + 2*distanceToMain, well under 90s) has fired.
            // `timeOrdered` is final, so the elapsed time has to be real, not faked.
            if (A.now() == 100) {
                construction = new Construction(AUnitType.Protoss_Gateway);
                construction.setBuilder(probe);
                construction.setPositionToBuild(APosition.create(14, 16));
                construction.setStatus(ConstructionOrderStatus.NOT_STARTED);
                ConstructionRequests.constructions.add(construction);
                return;
            }

            if (A.now() != 2800 || construction == null)
                return;

            assertTrue(A.now() - 100 >= 30 * 60,
                    "the construction must really be old enough for tookTooLong to fire");

            assertTrue(
                    ConstructionRequests.constructions.contains(construction),
                    "the fixture must still hold the construction before the call");

            CancelTooLongConstructions.cancelCauseTakingTooLongIfNeeded(
                    construction, AUnitType.Protoss_Gateway, AUnitType.Protoss_Gateway);

            assertTrue(
                    ConstructionRequests.constructions.contains(construction),
                    "a construction whose assigned builder is alive and which is only waiting to"
                            + " become affordable must NOT be cancelled as 'took too long' - that is"
                            + " the measured reason Gateways end at zero (build order says '9 - Gateway',"
                            + " the log shows 'Gateway took too long (43s) / buildable:true' instead)");
        });
    }

}
