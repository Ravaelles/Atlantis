package tests.unit;

import atlantis.game.A;
import atlantis.information.enemy.UnitsArchive;
import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionOrderStatus;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.constructions.commanders.ConstructionStatusChanger;
import atlantis.units.AUnitType;
import atlantis.util.log.EventLog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A STARTING construction whose live builder never issues must not hold
 * the site forever.
 *
 * <p><b>The defect this pins (NEXT.md #150, measured live in game
 * 20261102_003832):</b> a CyberneticsC STARTED f5154, then 0
 * completions / 0 cancels / 0 demotes in ~10k frames while 17 further
 * dispatches deduped to the same stuck row. Zero Dragoons all game
 * while gas banked to 1008; the zealot-only army was overrun. Every
 * demote path reads IN_PROGRESS only - STARTING with a live attached
 * builder has no watchdog at all (the dead-builder re-arm needs a
 * corpse; the IN_PROGRESS age-cap needs the flip that never comes).</p>
 *
 * <p>The fix demotes an ancient parked STARTING (builder alive but
 * neither moving, nor constructing, nor moved in 2400f, no recent
 * issue attempt) back to NOT_STARTED with its builder released, so
 * the travel/issue/cancel/requeue machinery recovers it. Healthy
 * travellers never trip: walking changes position every frame.</p>
 */
@Tag("infra")
public class StartingParkedDemoteTest extends AbstractTestWithWorld {

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
        UnitsArchive.reset();
    }

    private Construction startingCoreAt(FakeUnit probe, int tx, int ty) {
        Construction construction = new Construction(AUnitType.Protoss_Cybernetics_Core);
        construction.setBuilder(probe);
        construction.setPositionToBuild(APosition.create(tx, ty));
        construction.setStatus(ConstructionOrderStatus.STARTING);

        return construction;
    }

    @Test
    public void ancientParkedStartingDemotesAndReleasesBuilder() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 30, 30);

        world(130, units(nexus, probe), new FakeUnit[0], () -> {
            if (A.now() != 120) return;

            // A builder parked since long before this world: the
            // standstill clock reads 2500f without a 2600-frame run
            // (the EventLog buffer flushes every 720f, so the twin is
            // only assertable in a short world).
            probe.unitState().setLastPositionChanged(A.now() - 2500);
            Construction construction = startingCoreAt(probe, 12, 10);
            EventLog.clearForTests();

            assertTrue(
                ConstructionStatusChanger.demoteParkedStarting(construction, probe),
                "a live builder parked 2500f at a STARTING site that never"
                    + " issues must re-arm, not hold the tech forever"
                    + " (003832 Core shape)");
            assertEquals(
                ConstructionOrderStatus.NOT_STARTED, construction.status(),
                "demoting hands the travel/issue/cancel machinery back a live row");
            assertNull(
                construction.builder(),
                "the parked builder is released so the re-arm picks a fresh one");
            assertTrue(
                EventLog.toJsonLines().contains("DEMOTE_STALLED"),
                "the re-arm twins, never silent");
        });
    }

    @Test
    public void youngStartingKeepsTravelling() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 12, 10);

        world(130, units(nexus, probe), new FakeUnit[0], () -> {
            if (A.now() == 10) {
                probe.unitState().setLastPositionChanged(A.now());
            }

            if (A.now() == 120) {
                Construction construction = startingCoreAt(probe, 12, 10);

                assertFalse(
                    ConstructionStatusChanger.demoteParkedStarting(construction, probe),
                    "110f of travel is healthy - the walk to a far site takes longer than that");
                assertEquals(
                    ConstructionOrderStatus.STARTING, construction.status(),
                    "no demote means no status change");
            }
        });
    }

    @Test
    public void movingBuilderKeepsStarting() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 30, 30);
        probe.lastCommand = "Move";

        world(2600, units(nexus, probe), new FakeUnit[0], () -> {
            if (A.now() == 10) {
                probe.unitState().setLastPositionChanged(A.now());
            }

            if (A.now() == 2500) {
                Construction construction = startingCoreAt(probe, 12, 10);

                assertFalse(
                    ConstructionStatusChanger.demoteParkedStarting(construction, probe),
                    "a builder still closing distance is travelling, not parked");
                assertEquals(
                    ConstructionOrderStatus.STARTING, construction.status(),
                    "no demote means no status change");
            }
        });
    }

    @Test
    public void constructingBuilderKeepsStarting() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 12, 10);
        probe.setConstructing(true);

        world(2600, units(nexus, probe), new FakeUnit[0], () -> {
            if (A.now() == 2500) {
                Construction construction = startingCoreAt(probe, 12, 10);

                assertFalse(
                    ConstructionStatusChanger.demoteParkedStarting(construction, probe),
                    "issuing right now is the advancer's case, not the demoter's");
            }
        });
    }

    @Test
    public void deadBuilderBelongsToTheRearmPath() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 12, 10);
        probe.setHp(0);

        world(2600, units(nexus, probe), new FakeUnit[0], () -> {
            if (A.now() == 2500) {
                Construction construction = startingCoreAt(probe, 12, 10);

                assertFalse(
                    ConstructionStatusChanger.demoteParkedStarting(construction, probe),
                    "a corpse is the dead-builder re-arm's case, not a parked live builder");
            }
        });
    }

    @Test
    public void recentIssueAttemptHoldsFire() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 12, 10);

        world(2600, units(nexus, probe), new FakeUnit[0], () -> {
            if (A.now() == 2500) {
                Construction construction = startingCoreAt(probe, 12, 10);
                construction.setLastBuildAttemptNow();

                assertFalse(
                    ConstructionStatusChanger.demoteParkedStarting(construction, probe),
                    "an issue attempt just went out - the warp may still be settling");
            }
        });
    }

    private Construction chronicStall;
    private FakeUnit chronicProbe;

    @Test
    public void chronicAttemptsDoNotHoldFireForever() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10);
        chronicProbe = fake(AUnitType.Protoss_Probe, 30, 30);

        world(2600, units(nexus, chronicProbe), new FakeUnit[0], () -> {
            if (A.now() == 10) {
                chronicProbe.unitState().setLastPositionChanged(A.now());
                // Old attempts plus a fresh one: the live shape is a
                // refused build re-issued every few frames for the whole
                // game (measured: Nexus [105,115] 1031 attempts, every
                // frame inside the 150f hold-fire, natural never online).
                chronicStall = startingCoreAt(chronicProbe, 12, 10);
                chronicStall.setLastBuildAttemptNow();
            }

            if (A.now() == 2490) {
                chronicStall.setLastBuildAttemptNow();
            }

            if (A.now() == 2500) {
                assertTrue(
                    ConstructionStatusChanger.demoteParkedStarting(chronicStall, chronicProbe),
                    "attempts since frame 10 with no issue: the settle window"
                        + " expired 2340f ago, the parked row must re-arm");
                assertEquals(
                    ConstructionOrderStatus.NOT_STARTED, chronicStall.status(),
                    "the chronic hold-fire expires instead of suppressing the watchdog");
            }
        });
    }
}
