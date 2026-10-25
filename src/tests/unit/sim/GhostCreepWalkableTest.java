package tests.unit.sim;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import atlantis.combat.BattleCommander;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import atlantis.battle.DuelMemory;
import atlantis.combat.cluster.FightOrFlight;
import atlantis.combat.strategy.GhostAdvance;
import atlantis.map.MapTiles;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import tests.fakes.FakeMapTiles;
import tests.fakes.FakeOrderSink;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;

/**
 * Ghost-creep walkability (owner side observation, Steamhammer game:
 * 12 moves ordered into unwalkable tiles, each refused safely by the
 * move door with a full stack trace, while the creep stalled on the
 * wall instead of sliding along it). The creep checks walkability
 * BEFORE ordering and slides to the nearest walkable tile.
 */
@Tag("combat")
public class GhostCreepWalkableTest extends tests.acceptance.WorldStubForTests {

    private void useArenaWalkability(final SimArena arena) {
        MapTiles.useSource(new MapTiles.Source() {
            @Override
            public boolean isWalkable(HasPosition at) {
                APosition p = at.position();

                return arena.isWalkablePx(p.x(), p.y());
            }

            @Override
            public boolean isExplored(HasPosition at) {
                return true;
            }

            @Override
            public boolean isVisible(HasPosition at) {
                return true;
            }

            @Override
            public boolean isBuildable(HasPosition at, boolean alsoCheckBuildings) {
                return true;
            }

            @Override
            public boolean hasPathBetween(HasPosition from, HasPosition to) {
                return true;
            }

            @Override
            public boolean canBuildHere(AUnit builder, AUnitType building, APosition at) {
                return true;
            }
        });
    }

    private static List<double[]> moveDests(FakeUnit u) {
        List<double[]> out = new ArrayList<>();
        Pattern p = Pattern.compile("\\[\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\]");
        for (FakeOrderSink.Record r : ((FakeOrderSink) u.orderSink()).orders()) {
            if (!r.operation.equals("move")) continue;
            Matcher m = p.matcher(r.detail);
            if (m.find()) out.add(new double[]{Integer.parseInt(m.group(1)) / 32.0, Integer.parseInt(m.group(2)) / 32.0});
        }

        return out;
    }

    @Test
    public void creepNeverOrdersIntoTheWall() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 10);
        FakeUnit d = fake(AUnitType.Protoss_Dragoon, 30, 20);
        d.hp = 120;
        d.shields = 80;

        // Vertical wall ahead: the ghost (last contact) sits inside it.
        SimArena arena = new SimArena(70 * 32, 40 * 32).block(38 * 32, 0, 2 * 32, 40 * 32);
        APosition ghost = APosition.create(39, 20);

        SimWorld world = new SimWorld(
            new FakeUnit[] { nexus, d }, new FakeUnit[0], arena);
        try {
            useArenaWalkability(arena);
            BattleCommander.clearForTests();
            GhostAdvance.clearForTests();

            for (int i = 0; i < 120; i++) {
                final int tick = i;
                world.tick(() -> {
                    DuelMemory.snapshotContact(d, ghost);
                    GhostAdvance.move(d, FightOrFlight.Tactic.FIGHT);
                    assertTrue(tick >= 0);
                });
                if (d.hp <= 0) break;
            }

            List<double[]> dests = moveDests(d);
            assertFalse(dests.isEmpty(), "the creep issues march orders toward the ghost");
            for (double[] m : dests) {
                assertTrue(arena.isWalkablePx((int) (m[0] * 32), (int) (m[1] * 32)),
                    "every creep dest is walkable - slide along the wall, never into it: "
                        + m[0] + "," + m[1]);
            }
        }
        finally {
            world.close();
            BattleCommander.clearForTests();
            GhostAdvance.clearForTests();
            FakeMapTiles.installAsSource();
        }
    }

    @Test
    public void slideOffWallKeepsWalkableDests() {
        SimArena arena = new SimArena(70 * 32, 40 * 32).block(38 * 32, 0, 2 * 32, 40 * 32);
        try {
            useArenaWalkability(arena);

            APosition open = APosition.create(30, 20);
            assertTrue(GhostAdvance.slideOffWall(open) == open || GhostAdvance.slideOffWall(open).equals(open),
                "a walkable dest passes through untouched");

            APosition walled = APosition.create(39, 20);
            APosition slid = GhostAdvance.slideOffWall(walled);
            assertTrue(slid == null || arena.isWalkablePx(slid.x(), slid.y()),
                "an unwalkable dest slides out or declines, never into the wall");
            assertTrue(GhostAdvance.slideOffWall(null) == null, "null never orders");
        }
        finally {
            FakeMapTiles.installAsSource();
        }
    }
}
