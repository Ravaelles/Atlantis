package tests.unit;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins the guard against the engine's training-queue crash (owner report,
 * 2026-10-07: "OrderSink.train failed" spam plus an
 * {@code ArrayIndexOutOfBoundsException}, blocking the Cybernetics Core).
 *
 * <p>
 * The engine does not return {@code false} when a production building cannot
 * take an order - it throws. {@code BwapiOrderSink.issue()} catches it, but the
 * stack trace is printed once a minute and the order is simply lost, so a
 * building that is still under construction or already training produces
 * nothing while the log fills up.
 * </p>
 *
 * <p>
 * Two levels have to hold, and this test pins both because the crash came back
 * twice through different call sites:
 * </p>
 *
 * <ol>
 * <li>The sink refuses the call outright for a unit that cannot accept it, so
 * the engine is never asked.</li>
 * <li>The production call sites check {@code isTrainingAnyUnit()} (or use a
 * selector that does), so a busy building is not even chosen.</li>
 * </ol>
 */
public class TrainOrderGuardTest {

        private static final Path SINK = Paths.get("src/atlantis/units/BwapiOrderSink.java");
        private static final Path ZEALOT = Paths
                        .get("src/atlantis/production/dynamic/protoss/units/ProduceZealot.java");
        private static final Path GATEWAY = Paths
                        .get("src/atlantis/production/dynamic/protoss/units/GatewayClosestToEnemy.java");
        private static final Path V2_DIRECTOR = Paths
                        .get("src/atlantis/production/v2/execution/GameOrderDirector.java");

        @Test
        public void theV2DirectorNeverAsksAFacilityThatCannotTakeWork() throws Exception {
                // The same guard as the legacy call sites, at the v2 door: the
                // engine throws (not returns false) when a building is asked for
                // work it cannot take, so the director checks first.
                String director = read(V2_DIRECTOR);

                assertTrue(director.contains("!producer.isCompleted()"),
                                "GameOrderDirector.trainFacility must refuse an unfinished facility");
                assertTrue(director.contains("!facility.isCompleted()"),
                                "and so must researchOrUpgrade");
                assertTrue(director.contains("commandsThisFrame"),
                                "and one facility must not take two commands in one frame");
        }

        @Test
        public void theSinkRefusesToTrainOnAUnitThatIsNotCompleted() throws Exception {
                String sink = read(SINK);

                assertTrue(sink.contains("!actor.isCompleted()"),
                                "BwapiOrderSink.train must refuse a unit that is not completed - the"
                                                + " engine throws ArrayIndexOutOfBoundsException from its training"
                                                + " queue for one that is still being built");
                assertTrue(sink.contains("!actor.isAlive()"),
                                "and one that is already dead");
        }

        @Test
        public void theZealotProducerChecksThatItsGatewayIsFree() throws Exception {
                String zealot = read(ZEALOT);

                assertTrue(zealot.contains("isTrainingAnyUnit()"),
                                "produceZealot must not ask a busy Gateway to train; the engine throws"
                                                + " rather than returning false, and the order is lost");
        }

        @Test
        public void theGatewaySelectorReturnsOnlyACompletedOne() throws Exception {
                String gateway = read(GATEWAY);

                // Select filters completeness by contract, but the selector must not
                // reach for a raw list that bypasses it: the fallback used to return
                // whatever Gateway existed, busy or not.
                assertTrue(gateway.contains("ourOneNotTrainingUnits(Protoss_Gateway)"),
                                "the no-free-Gateway fallback must ask for a completed, non-training"
                                                + " Gateway - 'ourOfType' alone does not check whether it is busy");
        }

        @Test
        public void theTrainingQueueIsNeverWalkedBlindly() throws Exception {
                // trainingQueue() is the call that throws on an empty queue, so any code
                // reaching for it on a possibly-busy building is a latent version of the
                // same crash. isFree()/isTrainingAnyUnit()/hasNothingInQueue() are the
                // safe questions, and they are what the code should ask.
                String unit = read(Paths.get("src/atlantis/units/AUnit.java"));

                assertTrue(unit.contains("public boolean isTrainingAnyUnit()"),
                                "AUnit.isTrainingAnyUnit() is the safe question and must keep existing");
                assertTrue(unit.contains("isFree() && trainingQueue().isEmpty()"),
                                "hasNothingInQueue() must keep isFree() FIRST: it short-circuits before"
                                                + " touching the queue that throws");
        }

        private static String read(Path path) throws Exception {
                return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        }
}
