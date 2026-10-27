package tests.unit;

import atlantis.battle.cluster.Alpha;
import atlantis.combat.BattleCommander;
import atlantis.combat.cluster.ClusterAnchor;
import atlantis.map.base.BaseLocations;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Baseless ATTACK must explore instead of idling (owner: on UMS duel
 * maps the mission anchor is meaningless without a base, and the old
 * code returned a null anchor - measured Wine 1Drag_v_1Zeal: one
 * Dragoon standing still a whole game). With no main the army's own
 * center is the reference for the nearest unexplored start.
 */
@Tag("combat")
public class BaselessAttackExploresTest extends AbstractTestWithWorld {

    @Override
    protected FakeUnit[] generateOur() {
        return null;
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return null;
    }

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @AfterEach
    public void clean() {
        BattleCommander.clearForTests();
    }

    @Test
    public void baselessClusterMarchesFromItsOwnCenter() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);

        world(1, fakeOurs(dragoon), fakeEnemies(), () -> {
            APosition unexploredStart = APosition.create(90, 20);
            Alpha.gather();

            try (MockedStatic<BaseLocations> map = Mockito.mockStatic(BaseLocations.class)) {
                map.when(() -> BaseLocations.nearestUnexploredStartingLocation(
                    org.mockito.ArgumentMatchers.any())).thenReturn(unexploredStart);

                HasPosition anchor = ClusterAnchor.attackAnchor();

                assertNotNull(anchor,
                    "no main, no known enemy - the anchor must still exist "
                        + "(the old code returned null and the cluster idled)");
                assertEquals(unexploredStart, anchor,
                    "the army marches on the nearest unexplored start");
            }
        });
    }

    @Test
    public void basedGameKeepsTheOldReference() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 10);
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 30, 20);

        world(1, fakeOurs(nexus, dragoon), fakeEnemies(), () -> {
            APosition unexploredStart = APosition.create(90, 20);

            try (MockedStatic<BaseLocations> map = Mockito.mockStatic(BaseLocations.class)) {
                final HasPosition[] askedFrom = new HasPosition[1];
                map.when(() -> BaseLocations.nearestUnexploredStartingLocation(
                        org.mockito.ArgumentMatchers.any()))
                    .thenAnswer(inv -> {
                        askedFrom[0] = inv.getArgument(0);

                        return unexploredStart;
                    });

                assertNotNull(ClusterAnchor.attackAnchor());
                assertNotNull(askedFrom[0],
                    "with a base the reference stays the main");
                assertEquals(nexus.position(), askedFrom[0],
                    "based games keep the old reference exactly");
            }
        });
    }
}
