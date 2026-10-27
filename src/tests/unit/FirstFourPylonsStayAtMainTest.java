package tests.unit;

import atlantis.map.position.HasPosition;
import atlantis.production.constructions.position.protoss.PylonPosition;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import bwapi.Race;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Owner: the first four Pylons adjoin the main - never the natural.
 * The third/fourth Pylon fell through to
 * {@code ourBasesWithUnfinished().exclude(Bases.natural()).last()}, which
 * leaks the natural in whenever {@code Bases.natural()} is null with two
 * bases (custom maps, choke-detection gap): exclude(null) excludes
 * nothing and last() is the newest base - the natural Nexus.
 */
@Tag("production")
public class FirstFourPylonsStayAtMainTest extends WorldStubForTests {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @Override
    protected tests.fakes.FakeUnit[] generateOur() {
        return null;
    }

    @Override
    protected tests.fakes.FakeUnit[] generateEnemies() {
        return null;
    }

    @Test
    public void thirdPylonStaysAtMainWhenNaturalUnknown() {
        currentMinerals = 100;
        currentSupplyUsed = 30;
        currentSupplyTotal = 41;

        world(20, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Nexus, 60, 20),
            fake(AUnitType.Protoss_Pylon, 22, 22),
            fake(AUnitType.Protoss_Pylon, 24, 22),
            fake(AUnitType.Protoss_Pylon, 26, 22),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 5) return;

            HasPosition main = Select.mainOrAnyBuilding();
            HasPosition natural = Select.ourBasesWithUnfinished().list().get(1);

            HasPosition nearTo = PylonPosition.nearToForPylon(null);

            double toMain = nearTo.distTo(main);
            double toNatural = nearTo.distTo(natural);

            assertTrue(toMain < toNatural,
                "3rd Pylon must site at the main, not the natural (toMain="
                    + (int) toMain + ", toNatural=" + (int) toNatural + ")");
        });
    }
}
