package tests.unit;

import atlantis.map.position.APosition;
import atlantis.production.constructions.position.protoss.PylonPosition;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * NEXT #214a: every nearTo branch owns a code, so a far-away Pylon warp
 * names the rule that chose its origin (board "PylonSite" key) instead
 * of leaving tile forensics. The supply-33-behind-natural case is
 * exactly what this answers.
 */
@Tag("production")
public class PylonNearToReasonTest extends WorldStubForTests {

    @Override
    public bwapi.Race initRace() {
        return bwapi.Race.Protoss;
    }

    @Override
    protected FakeUnit[] generateOur() {
        return null;
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return null;
    }

    @BeforeEach
    public void resetReason() {
        PylonPosition.clearForTests();
    }

    @AfterEach
    public void cleanReason() {
        PylonPosition.clearForTests();
    }

    @Test
    public void firstPylonNamesItsBranch() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);

        world(20, fakeOurs(nexus), new FakeUnit[] {}, () -> {
            if (atlantis.game.A.now() != 5) return;

            PylonPosition.nearToForPylon(null);

            assertEquals("first-pylon", PylonPosition.lastReason);
        });
    }

    @Test
    public void callerProvidedSurvivesUntouched() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit p1 = fake(AUnitType.Protoss_Pylon, 22, 22);
        FakeUnit p2 = fake(AUnitType.Protoss_Pylon, 24, 22);

        world(20, fakeOurs(nexus, p1, p2), new FakeUnit[] {}, () -> {
            if (atlantis.game.A.now() != 5) return;

            PylonPosition.nearToForPylon(APosition.create(40, 40));

            assertEquals("caller-provided", PylonPosition.lastReason);
        });
    }

    @Test
    public void supplyBlockedNamesItsBranch() {
        currentMinerals = 300;
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);

        world(20, fakeOurs(
            nexus,
            fake(AUnitType.Protoss_Pylon, 22, 22),
            fake(AUnitType.Protoss_Pylon, 23, 22),
            fake(AUnitType.Protoss_Pylon, 24, 22),
            fake(AUnitType.Protoss_Pylon, 25, 22),
            fake(AUnitType.Protoss_Pylon, 26, 22),
            fake(AUnitType.Protoss_Pylon, 27, 22)
        ), new FakeUnit[] {}, () -> {
            if (atlantis.game.A.now() != 5) return;
            initSupply(10, 11);

            PylonPosition.nearToForPylon(null);

            assertEquals("supply-blocked-main", PylonPosition.lastReason);
        });
    }
}
