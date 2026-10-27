package tests.acceptance;

import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.production.constructions.position.protoss.ProtossDefineNearTo;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import tests.fakes.FakeChoke;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * NEXT #192: a non-Pylon building must stand in a psi field, so its
 * search centers on an existing Pylon - never on bare choke geometry
 * whose rings span both sides of the wall (live: 3rd Gateway searched
 * around the choke center, found [105,110] across the wall, guard-nulled
 * then order-cancelled in a loop). The anchor is the Pylon nearest the
 * main choke; with no Pylon at all the legacy choke center stands in.
 */
public class GatewayNearToPrefersPylonByChokeTest extends WorldStubForTests {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @Override
    public Race initEnemyRace() {
        return Race.Terran;
    }

    @Test
    public void anchorsOnPylonNearestTheChoke() {
        FakeChoke choke = new FakeChoke(APosition.create(30, 30), 5);
        FakeUnit nearPylon = fake(AUnitType.Protoss_Pylon, 40, 30);
        FakeUnit farPylon = fake(AUnitType.Protoss_Pylon, 20, 20);
        try (MockedStatic<Chokes> chokes = Mockito.mockStatic(Chokes.class)) {
            chokes.when(Chokes::mainChoke).thenReturn(choke);

            world(8, fakeOurs(
                fake(AUnitType.Protoss_Nexus, 10, 10),
                fake(AUnitType.Protoss_Probe, 9, 10),
                nearPylon,
                farPylon
            ), fakeEnemies(), () -> {
                if (atlantis.game.A.now() != 2) return;

                AUnit anchor = (AUnit) ProtossDefineNearTo.forProtoss(
                    AUnitType.Protoss_Gateway, null);

                assertNotNull(anchor, "a Pylon exists, so the anchor exists");
                assertSame(nearPylon, anchor,
                    "the Pylon nearest the choke centers the search, not the choke itself");
            });
        }
    }

    @Test
    public void fallsBackToChokeWithNoPylon() {
        FakeChoke choke = new FakeChoke(APosition.create(30, 30), 5);
        try (MockedStatic<Chokes> chokes = Mockito.mockStatic(Chokes.class)) {
            chokes.when(Chokes::mainChoke).thenReturn(choke);

            world(8, fakeOurs(
                fake(AUnitType.Protoss_Nexus, 10, 10),
                fake(AUnitType.Protoss_Probe, 9, 10)
            ), fakeEnemies(), () -> {
                if (atlantis.game.A.now() != 2) return;

                assertSame(choke, ProtossDefineNearTo.forProtoss(
                        AUnitType.Protoss_Gateway, null),
                    "no Pylon anywhere: legacy choke center stands in");
            });
        }
    }
}
