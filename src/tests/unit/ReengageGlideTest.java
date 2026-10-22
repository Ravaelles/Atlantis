package tests.unit;

import atlantis.battle.BattleCommander;
import atlantis.battle.cluster.FightOrFlight;
import atlantis.map.MapTiles;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeMapTiles;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Reengage glide discipline (owner 2026-10-26): the glide destination
 * is pure geometry with no walkability promise, and the engine refuses
 * unwalkable moves - live a Dragoon glided into [43,53] every frame
 * while being chewed, the move refused, the wait pinned it standing.
 * An unwalkable glide is no glide: the frame chases with an attack
 * order instead of waiting into the wall.
 */
@Tag("combat")
public class ReengageGlideTest extends WorldStubForTests {

    @Test
    public void glideIntoWallChasesInsteadOfWaiting() {
        // Dragoon at (20,20), zealot at (28,20): 8 tiles out (inside
        // the 9-tile acquisition, past the 4-tile range, no kite
        // threat) - the glide owns the frame. Its destination
        // ((28 - (8 - 3.8)), 20) sits inside the wall below, so the
        // glide must not issue and the attack must chase instead.
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 20, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 28, 20);
        zealot.setEnemy();

        MapTiles.useSource(new FakeMapTiles() {
            @Override
            public boolean isWalkable(HasPosition at) {
                return !(at.tx() >= 23 && at.tx() <= 24 && at.ty() >= 19 && at.ty() <= 21);
            }
        });
        try {
            world(3, fakeOurs(dragoon), fakeEnemies(zealot), () -> {
                if (atlantis.game.A.now() != 2) return;

                BattleCommander.clearForTests();
                BattleCommander.act(dragoon, FightOrFlight.Tactic.FIGHT, null, dragoon);

                String journal = dragoon.commandHistory().toString();
                assertFalse(journal.contains("ClusterReengage"),
                    "no glide into the wall: " + journal);
                assertTrue(journal.contains("Attack:"),
                    "chase with an attack instead of standing: " + journal);
            });
        }
        finally {
            FakeMapTiles.installAsSource();
        }
    }
}
