package tests.unit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import atlantis.game.A;
import atlantis.information.enemy.UnitsArchive;
import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionOrderStatus;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.constructions.commanders.ConstructionStatusChanger;
import atlantis.units.AUnitType;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;

/**
 * WO-150b wiring: the demote path notes the warping building's HP on
 * every frame it sees one, so the progress signal accumulates in live
 * games without any new hook. The demote itself still refuses (the
 * presence shield is untouched - only the twin will ever read the
 * trail, and only after a live freeze proves the design).
 */
@Tag("production")
public class WarpHpTrailWiringTest extends AbstractTestWithWorld {

    @Override
    protected FakeUnit[] generateOur() {
        return null;
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return null;
    }

    @AfterEach
    public void cleanConstructions() {
        ConstructionRequests.constructions.clear();
        UnitsArchive.reset();
    }

    private Construction inProgressCoreAt(FakeUnit probe, int tx, int ty) {
        Construction construction = new Construction(AUnitType.Protoss_Cybernetics_Core);
        construction.setBuilder(probe);
        construction.setPositionToBuild(APosition.create(tx, ty));
        construction.setStatus(ConstructionOrderStatus.IN_PROGRESS);

        return construction;
    }

    private FakeUnit warpingCoreAt(int tx, int ty, int hp) {
        FakeUnit warping = fake(AUnitType.Protoss_Cybernetics_Core, tx, ty);
        warping.hp = hp;
        warping.completed = false;

        return warping;
    }

    @Test
    public void demotePathNotesWarpHp() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 10);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 12, 10);
        FakeUnit warping = warpingCoreAt(12, 10, 300);

        world(130, units(nexus, probe, warping), new FakeUnit[0], () -> {
            if (A.now() != 120) return;

            Construction construction = inProgressCoreAt(probe, 12, 10);

            assertFalse(
                ConstructionStatusChanger.demoteStalledInProgress(construction, probe),
                "warping entity on site: presence shields, demote refuses");
            assertFalse(
                ConstructionStatusChanger.demoteStalledInProgress(construction, probe),
                "still shielded on the second frame");

            assertTrue(
                construction.isWarpHpStatic(240, A.now()),
                "two frames of notes at hp 300: the trail accumulates through the demote path");
        });
    }

    @Test
    public void movingWarpHpReadsProgressing() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 10);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 12, 10);
        FakeUnit warping = warpingCoreAt(12, 10, 300);

        world(130, units(nexus, probe, warping), new FakeUnit[0], () -> {
            if (A.now() != 120) return;

            Construction construction = inProgressCoreAt(probe, 12, 10);

            ConstructionStatusChanger.demoteStalledInProgress(construction, probe);
            warping.hp = 340;
            ConstructionStatusChanger.demoteStalledInProgress(construction, probe);

            assertFalse(
                construction.isWarpHpStatic(240, A.now()),
                "hp moved between the two notes: progressing, never frozen");
        });
    }
}
