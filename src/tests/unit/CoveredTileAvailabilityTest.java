package tests.unit;

import atlantis.map.position.APosition;
import atlantis.placement.core.PsiGating;
import atlantis.placement.core.RacePlacementStrategy;
import atlantis.placement.race.ProtossPlacementStrategy;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static tests.unit.AbstractTestWithUnits.fake;

/**
 * Covered tiles are refused at rank time (NEXT #145): the silent catalogue
 * planner serves V2 LIVE siting straight from the candidate loop, so the
 * adapter-level gate never sees those tiles. A tile 4+ of our workers
 * stand on answers REFUSE - the builder would be refused and the site
 * would die builderless - while an uncovered tile keeps its psi answer.
 */
@Tag("placement")
public class CoveredTileAvailabilityTest extends WorldStubForTests {

    private static ProtossPlacementStrategy poweredStrategy() {
        PsiGating.PowerSource power = new PsiGating.PowerSource() {
            @Override
            public boolean isPowered(int tx, int ty) {
                return true;
            }

            @Override
            public List<Integer> incomingPowerFrames(int tx, int ty) {
                return Collections.emptyList();
            }

            @Override
            public boolean canBeCoveredByNewPylon(int tx, int ty) {
                return true;
            }
        };

        return new ProtossPlacementStrategy(new PsiGating(power));
    }

    @Test
    public void coveredTileIsRefused() {
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 28, 25),
            fake(AUnitType.Protoss_Probe, 25, 28),
            fake(AUnitType.Protoss_Probe, 31, 26),
            fake(AUnitType.Protoss_Probe, 26, 31)
        ), fakeEnemies(), () -> {
            APosition site = APosition.create(25, 25);

            assertEquals(RacePlacementStrategy.AvailabilityVerdict.REFUSE,
                poweredStrategy().availabilityVerdict(site.tx(), site.ty(), "Protoss_Gateway"),
                "4 workers within 12 stand on the tile - rank-time refuse, no serving");
        });
    }

    @Test
    public void uncoveredTileKeepsItsPsiAnswer() {
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 28, 25),
            fake(AUnitType.Protoss_Probe, 25, 28)
        ), fakeEnemies(), () -> {
            APosition site = APosition.create(25, 25);

            assertEquals(RacePlacementStrategy.AvailabilityVerdict.AVAILABLE,
                poweredStrategy().availabilityVerdict(site.tx(), site.ty(), "Protoss_Gateway"),
                "2 workers do not cover the tile - the psi answer stands");
        });
    }
}
