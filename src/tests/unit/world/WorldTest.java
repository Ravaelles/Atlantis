package tests.unit.world;

import atlantis.core.world.UnitSnapshot;
import atlantis.core.world.World;
import atlantis.map.position.APosition;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Stage E: the read model is constructible and queryable with no engine.
 */
public class WorldTest {

    @Test
    void worldBuildsDirectlyFromSnapshots() {
        APosition position = APosition.createFromPixels(100, 200);

        World world = World.of(
            new UnitSnapshot(1, AUnitType.Terran_Marine, position, 40, 0),
            new UnitSnapshot(2, AUnitType.Zerg_Zergling, position, 35, 0)
        );

        assertEquals(2, world.size());
        assertEquals(AUnitType.Terran_Marine, world.snapshotFor(1).type());
        assertEquals(40, world.snapshotFor(1).hp());
        assertTrue(world.snapshotFor(1).isAlive());
        assertNull(world.snapshotFor(999));
    }

    @Test
    void worldSnapshotsFakeUnitsWithoutEngine() {
        FakeUnit marine = new FakeUnit(AUnitType.Terran_Marine, 20, 20);

        World world = World.snapshotOf(java.util.Collections.singletonList(marine));

        assertEquals(1, world.size());
        UnitSnapshot snapshot = world.snapshotFor(marine.id());
        assertEquals(AUnitType.Terran_Marine, snapshot.type());
        assertEquals(marine.hp(), snapshot.hp());
    }
}
