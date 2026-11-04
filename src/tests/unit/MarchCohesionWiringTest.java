package tests.unit;

import atlantis.combat.formation.MarchCohesion;
import atlantis.map.position.APosition;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeChoke;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Cohesion wiring: a scattered cluster holds its front at the choke
 * fallback while the rear keeps its normal march orders. Deterministic
 * by construction (mocked choke path, fixed tiles).
 */
@Tag("combat")
public class MarchCohesionWiringTest extends WorldStubForTests {

    @BeforeEach
    public void clearCohesion() {
        MarchCohesion.clearForTests();
    }

    @AfterEach
    public void cleanCohesion() {
        MarchCohesion.clearForTests();
    }

    @Test
    public void frontHoldsRearAdvances() {
        FakeUnit front = fake(AUnitType.Protoss_Zealot, 55, 20);
        FakeUnit mid = fake(AUnitType.Protoss_Zealot, 30, 20);
        FakeUnit rear = fake(AUnitType.Protoss_Zealot, 10, 20);
        APosition anchor = APosition.create(100, 20);

        try (org.mockito.MockedStatic<atlantis.map.path.PathToEnemyBase> path =
                 org.mockito.Mockito.mockStatic(atlantis.map.path.PathToEnemyBase.class);
             org.mockito.MockedStatic<atlantis.map.choke.Chokes> chokes =
                 org.mockito.Mockito.mockStatic(atlantis.map.choke.Chokes.class)) {
            path.when(atlantis.map.path.PathToEnemyBase::chokesLeadingToEnemyBase)
                .thenReturn(new java.util.ArrayList<atlantis.map.choke.AChoke>());
            chokes.when(() -> atlantis.map.choke.Chokes.nearestChoke(
                    org.mockito.ArgumentMatchers.any()))
                .thenReturn(new FakeChoke(APosition.create(60, 20), 3));

            world(1, fakeOurs(front, mid, rear), fakeEnemies(), () -> {
                // Dists to the (60,20) choke: 5 / 30 / 50 - a snake.
                assertNotNull(
                    MarchCohesion.holdPointFor(front, anchor),
                    "the front waits at the fallback instead of marching on");
                assertNull(
                    MarchCohesion.holdPointFor(rear, anchor),
                    "the rear dresses instead of stepping back");
            });
        }
    }
}
