package tests.unit;

import atlantis.battle.BattleCommander;
import atlantis.battle.cluster.Alpha;
import atlantis.battle.cluster.ClusterRally;
import atlantis.game.AGame;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Formation orientation by expected direction (owner 2026-10-26): with
 * no visible enemy in reach the crescent still faces where the enemy is
 * expected - the last-seen address on UMS, the main choke at the home
 * base on melee - instead of collapsing into a clump facing nowhere.
 * Truly blind keeps the old null.
 */
@Tag("combat")
public class ClusterRallyTest extends WorldStubForTests {

    @BeforeEach
    public void clearRoster() {
        BattleCommander.clearForTests();
    }

    @AfterEach
    public void cleanRoster() {
        BattleCommander.clearForTests();
    }

    @Test
    public void umsFacesLastSeenEnemy() {
        // No visible enemy in reach (40 tiles off), but a fogged memory:
        // the crescent faces the remembered address, it does not clump.
        aGame.when(AGame::isUms).thenReturn(true);

        FakeUnit a = fake(AUnitType.Protoss_Dragoon, 20, 20);
        FakeUnit b = fake(AUnitType.Protoss_Dragoon, 21, 20);
        FakeUnit c = fake(AUnitType.Protoss_Dragoon, 20, 21);
        FakeUnit foe = fake(AUnitType.Protoss_Zealot, 60, 20);
        foe.setEnemy();
        foe.isVisibleUnitOnMap = false;

        world(3, fakeOurs(a, b, c), fakeEnemies(foe), () -> {
            if (atlantis.game.A.now() != 2) return;

            Alpha.get().addUnit(a);
            Alpha.get().addUnit(b);
            Alpha.get().addUnit(c);
            try {
                HasPosition slot = ClusterRally.formationSlotFor(a, a, null);
                assertNotNull(slot, "blind crescent faces the fogged address, not the leader");
                assertTrue(slot.distTo(foe.position()) < a.distTo(foe.position()),
                    "slot steps toward the expected enemy: " + slot);
            }
            finally {
                Alpha.get().removeUnit(a);
                Alpha.get().removeUnit(b);
                Alpha.get().removeUnit(c);
            }
        });
    }

    @Test
    public void meleeAwayKeepsOldNull() {
        // Melee away from home with nobody visible: no expected
        // direction, the old bunching fallback owns the unit as before.
        aGame.when(AGame::isUms).thenReturn(false);

        FakeUnit a = fake(AUnitType.Protoss_Dragoon, 20, 20);
        FakeUnit b = fake(AUnitType.Protoss_Dragoon, 21, 20);
        FakeUnit c = fake(AUnitType.Protoss_Dragoon, 20, 21);
        FakeUnit foe = fake(AUnitType.Protoss_Zealot, 60, 20);
        foe.setEnemy();
        foe.isVisibleUnitOnMap = false;

        world(3, fakeOurs(a, b, c), fakeEnemies(foe), () -> {
            if (atlantis.game.A.now() != 2) return;

            Alpha.get().addUnit(a);
            Alpha.get().addUnit(b);
            Alpha.get().addUnit(c);
            try {
                assertNull(ClusterRally.formationSlotFor(a, a, null),
                    "melee away from home keeps bunching when blind");
            }
            finally {
                Alpha.get().removeUnit(a);
                Alpha.get().removeUnit(b);
                Alpha.get().removeUnit(c);
            }
        });
    }

    @Test
    public void meleeHomeFacesMainChoke() {
        // At the home base the crescent faces the main choke, the door
        // the enemy must come through.
        aGame.when(AGame::isUms).thenReturn(false);

        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit a = fake(AUnitType.Protoss_Dragoon, 20, 21);
        FakeUnit b = fake(AUnitType.Protoss_Dragoon, 21, 20);
        FakeUnit c = fake(AUnitType.Protoss_Dragoon, 19, 20);

        atlantis.map.choke.AChoke choke = org.mockito.Mockito.mock(atlantis.map.choke.AChoke.class);
        org.mockito.Mockito.when(choke.center()).thenReturn(APosition.create(60, 20));
        try (org.mockito.MockedStatic<atlantis.map.choke.Chokes> chokes =
                org.mockito.Mockito.mockStatic(atlantis.map.choke.Chokes.class)) {
            chokes.when(atlantis.map.choke.Chokes::mainChoke).thenReturn(choke);

            world(3, fakeOurs(nexus, a, b, c), fakeEnemies(), () -> {
                if (atlantis.game.A.now() != 2) return;

                Alpha.get().addUnit(a);
                Alpha.get().addUnit(b);
                Alpha.get().addUnit(c);
                try {
                    HasPosition slot = ClusterRally.formationSlotFor(a, a, null);
                    assertNotNull(slot, "home crescent faces the main choke");
                    assertTrue(slot.distTo(choke.center()) < a.distTo(choke.center()),
                        "slot steps toward the choke: " + slot);
                }
                finally {
                    Alpha.get().removeUnit(a);
                    Alpha.get().removeUnit(b);
                    Alpha.get().removeUnit(c);
                }
            });
        }
    }
}
