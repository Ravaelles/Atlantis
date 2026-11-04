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
import atlantis.util.log.LogKey;
import atlantis.util.log.PostGameDigest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A STARTING row the demote shields miss must still name itself.
 * Pins {@code twinParkedStarting}: ancient order, live builder,
 * no progress - gathering, stale-constructing, or jittering so the
 * standstill clock never fills. Once per construction, never demotes.
 */
@Tag("infra")
public class ParkedStartingTwinTest extends AbstractTestWithWorld {

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

    private Construction startingNexusAt(FakeUnit probe, int tx, int ty) {
        Construction construction = new Construction(AUnitType.Protoss_Nexus);
        construction.setBuilder(probe);
        construction.setPositionToBuild(APosition.create(tx, ty));
        construction.setStatus(ConstructionOrderStatus.STARTING);

        return construction;
    }

    @Test
    public void ancientGatheringBuilderTwinsOnce() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 30, 30);

        final Construction[] held = new Construction[1];
        world(3000, units(nexus, probe), new FakeUnit[0], () -> {
            if (A.now() == 10) {
                held[0] = startingNexusAt(probe, 12, 10);
            }
            if (A.now() != 2500) return;

            Construction construction = held[0];
            EventLog.clearForTests();
            PostGameDigest.clearForTests();

            assertTrue(
                ConstructionStatusChanger.twinParkedStarting(construction, probe),
                "ancient STARTING with a live builder must name itself even when the demote shields hold");
            assertEquals(
                1, PostGameDigest.countFor("[MED] PARKED_STARTING"),
                "the witness twin fires, never silent (digest survives the 720f flush that clears the buffer)");
            assertFalse(
                ConstructionStatusChanger.twinParkedStarting(construction, probe),
                "once per construction - a parked row is a witness, not a heartbeat");
            assertEquals(
                ConstructionOrderStatus.STARTING, construction.status(),
                "twinning never demotes by itself");
        });
    }

    @Test
    public void youngStartingStaysSilent() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 12, 10);

        final Construction[] held = new Construction[1];
        world(300, units(nexus, probe), new FakeUnit[0], () -> {
            if (A.now() == 100) {
                held[0] = startingNexusAt(probe, 12, 10);
            }
            if (A.now() != 200) return;

            Construction construction = held[0];
            EventLog.clearForTests();

            assertFalse(
                ConstructionStatusChanger.twinParkedStarting(construction, probe),
                "a young row is travelling, not parked");
        });
    }

    @Test
    public void deadBuilderStaysSilent() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 12, 10);
        probe.setHp(0);

        final Construction[] held = new Construction[1];
        world(3000, units(nexus, probe), new FakeUnit[0], () -> {
            if (A.now() == 10) {
                held[0] = startingNexusAt(probe, 12, 10);
            }
            if (A.now() != 2500) return;

            Construction construction = held[0];

            assertFalse(
                ConstructionStatusChanger.twinParkedStarting(construction, probe),
                "a corpse is the re-arm path's case, not the parked witness");
        });
    }
}
