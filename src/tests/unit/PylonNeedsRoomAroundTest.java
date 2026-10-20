package tests.unit;

import atlantis.map.position.APosition;
import atlantis.production.constructions.position.protoss.PylonNeedsRoomAround;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeMapTiles;
import tests.fakes.FakeUnit;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * A Pylon warps power, not just a building (owner 2026-10-20, UMS 7th_rav:
 * a Pylon warped tight at the region edge, lighting no usable ground).
 * With 2+ supply free the four diagonal tiles around the site must be
 * walkable; poor supply keeps the old accept-anything behaviour.
 */
public class PylonNeedsRoomAroundTest extends WorldStubForTests {

    private static Set<String> blocked = new HashSet<>();

    private static final class EdgeMap extends FakeMapTiles {
        @Override
        public boolean isWalkable(atlantis.map.position.HasPosition at) {
            return !blocked.contains(at.tx() + "," + at.ty());
        }
    }

    @AfterEach
    public void clean() {
        blocked.clear();
        FakeMapTiles.installAsSource();
    }


    @Test
    public void openGroundPassesWithFreeSupply() {
        currentSupplyUsed = 10;
        currentSupplyTotal = 20;
        atlantis.map.MapTiles.useSource(new EdgeMap());

        world(1, fakeOurs(fake(AUnitType.Protoss_Nexus, 10, 10)), fakeEnemies(), () -> {
            AUnit builder = fake(AUnitType.Protoss_Probe, 30, 30);
            assertFalse(
                PylonNeedsRoomAround.isTooTight(builder, AUnitType.Protoss_Pylon, APosition.create(30, 30)),
                "open ground with free supply: nothing to refuse");
        });
    }

    @Test
    public void blockedDiagonalRefusesWithFreeSupply() {
        currentSupplyUsed = 10;
        currentSupplyTotal = 20;
        blocked.add("34,34");
        atlantis.map.MapTiles.useSource(new EdgeMap());

        world(1, fakeOurs(fake(AUnitType.Protoss_Nexus, 10, 10)), fakeEnemies(), () -> {
            AUnit builder = fake(AUnitType.Protoss_Probe, 30, 30);
            assertTrue(
                PylonNeedsRoomAround.isTooTight(builder, AUnitType.Protoss_Pylon, APosition.create(30, 30)),
                "one blocked diagonal (+4/+4) means the Pylon would sit on the edge");
        });
    }

    @Test
    public void poorSupplyBypassesTheRule() {
        currentSupplyUsed = 8;
        currentSupplyTotal = 9;
        blocked.add("34,34");
        atlantis.map.MapTiles.useSource(new EdgeMap());

        world(1, fakeOurs(fake(AUnitType.Protoss_Nexus, 10, 10)), fakeEnemies(), () -> {
            AUnit builder = fake(AUnitType.Protoss_Probe, 30, 30);
            assertFalse(
                PylonNeedsRoomAround.isTooTight(builder, AUnitType.Protoss_Pylon, APosition.create(30, 30)),
                "starving supply takes any powered tile - no room requirement");
        });
    }

    @Test
    public void nonPylonSkips() {
        currentSupplyUsed = 10;
        currentSupplyTotal = 20;
        blocked.add("34,34");
        atlantis.map.MapTiles.useSource(new EdgeMap());

        world(1, fakeOurs(fake(AUnitType.Protoss_Nexus, 10, 10)), fakeEnemies(), () -> {
            AUnit builder = fake(AUnitType.Protoss_Probe, 30, 30);
            assertFalse(
                PylonNeedsRoomAround.isTooTight(builder, AUnitType.Protoss_Gateway, APosition.create(30, 30)),
                "the rule is Pylon-only");
        });
    }
}
