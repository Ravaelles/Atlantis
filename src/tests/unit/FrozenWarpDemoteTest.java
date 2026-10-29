package tests.unit;

import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionOrderStatus;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.constructions.commanders.ConstructionStatusChanger;
import atlantis.information.enemy.UnitsArchive;
import atlantis.units.AUnitType;
import atlantis.util.log.EventLog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;

/**
 * A started warp that stops progressing with nobody working it must
 * re-arm (Wine 184529 cid 10: Pylon IN_PROGRESS 8+ minutes, builder
 * gone mining, supply frozen at 49/49 - the entity-present branch
 * twinned nothing and demoted nothing, and isOverdue never fires for
 * started constructions). Conservative: older than any legal warp
 * (2400f), hp proven static across the window (240f), not completed,
 * no live builder constructing it.
 */
@Tag("infra")
public class FrozenWarpDemoteTest extends AbstractTestWithWorld {

    @Override
    protected FakeUnit[] generateOur() {
        return null;
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return null;
    }

    @Override
    public bwapi.Race initRace() {
        return bwapi.Race.Protoss;
    }

    @AfterEach
    public void cleanConstructions() {
        ConstructionRequests.constructions.clear();
        UnitsArchive.reset();
        EventLog.clearForTests();
    }

    private static FakeUnit miner() {
        return new FakeUnit(AUnitType.Protoss_Probe, 30, 30) {
            @Override
            public boolean isGatheringMinerals() {
                return true;
            }
        };
    }

    private static FakeUnit constructor() {
        return new FakeUnit(AUnitType.Protoss_Probe, 12, 10) {
            @Override
            public boolean isConstructing() {
                return true;
            }
        };
    }

    private static FakeUnit warpingPylon(int hp) {
        FakeUnit warp = fake(AUnitType.Protoss_Pylon, 12, 10);
        warp.setCompleted(false);
        warp.hp = hp;

        return warp;
    }

    private static Construction inProgressPylonAt(FakeUnit builder, int tx, int ty) {
        Construction construction = new Construction(AUnitType.Protoss_Pylon);
        construction.setBuilder(builder);
        construction.setPositionToBuild(APosition.create(tx, ty));
        construction.setStatus(ConstructionOrderStatus.IN_PROGRESS);

        return construction;
    }

    /**
     * The demote twin may already have flushed to the log file (720f
     * cadence) in a long world, so the buffer alone is not the witness:
     * the flushed file tail counts too. Only this suite emits the
     * frozenWarp reason.
     */
    private static boolean frozenWarpRowPresent() {
        if (EventLog.toJsonLines().contains("\"reason\":\"frozenWarp\"")) return true;

        try {
            java.nio.file.Path path = java.nio.file.Paths.get("bwapi-data/write/EVENTS.jsonl");
            if (!java.nio.file.Files.exists(path)) return false;

            java.util.List<String> lines = java.nio.file.Files.readAllLines(path);
            int from = Math.max(0, lines.size() - 60);
            for (int i = from; i < lines.size(); i++) {
                if (lines.get(i).contains("\"key\":\"DEMOTE_STALLED\"")
                    && lines.get(i).contains("\"reason\":\"frozenWarp\"")) return true;
            }
        } catch (Exception ignored) {
        }

        return false;
    }

    private Construction frozen;
    private Construction progressing;
    private Construction young;
    private Construction worked;
    private Construction entityless;

    @Test
    public void frozenWarpWithMiningBuilderRearms() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10);
        FakeUnit builder = miner();
        FakeUnit warp = warpingPylon(100);

        world(2600, units(nexus, builder, warp), new FakeUnit[0], () -> {
            int now = A.now();
            if (now == 10) frozen = inProgressPylonAt(builder, 12, 10);
            if (now != 2400 && now != 2450 && now != 2500) return;

            Construction construction = frozen;
            boolean demoted = ConstructionStatusChanger.demoteStalledInProgress(construction, builder);
            if (now < 2450) {
                assertFalse(demoted,
                    "hp trail too short to prove a freeze at " + now + " (fail-open)");
            }
            else if (now == 2450) {
                assertTrue(demoted,
                    "cid-10 shape: 2440f old, hp static, builder mining - re-arm");
                assertEquals(ConstructionOrderStatus.NOT_STARTED, construction.status());
                assertTrue(frozenWarpRowPresent(),
                    "the frozen demote twins cid/type/tile/reason (buffer or flushed file)");
            }
            else {
                assertFalse(demoted,
                    "already re-armed at 2450: a NOT_STARTED construction never demotes again");
                assertEquals(ConstructionOrderStatus.NOT_STARTED, construction.status());
            }
        });
    }

    @Test
    public void progressingWarpNeverTrips() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10);
        FakeUnit builder = miner();
        FakeUnit warp = warpingPylon(100);

        world(2600, units(nexus, builder, warp), new FakeUnit[0], () -> {
            int now = A.now();
            if (now == 10) progressing = inProgressPylonAt(builder, 12, 10);
            if (now != 2400 && now != 2450 && now != 2500) return;

            if (now == 2450) warp.hp = 130;
            if (now == 2500) warp.hp = 160;

            Construction construction = progressing;

            assertFalse(
                ConstructionStatusChanger.demoteStalledInProgress(construction, builder),
                "moving hp is a progressing warp at " + now + ", never frozen");
            assertEquals(ConstructionOrderStatus.IN_PROGRESS, construction.status());
        });
    }

    @Test
    public void youngFrozenWarpKeepsInProgress() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10);
        FakeUnit builder = miner();
        FakeUnit warp = warpingPylon(100);

        world(1300, units(nexus, builder, warp), new FakeUnit[0], () -> {
            int now = A.now();
            if (now == 10) young = inProgressPylonAt(builder, 12, 10);
            if (now != 1100 && now != 1150 && now != 1200) return;

            Construction construction = young;

            assertFalse(
                ConstructionStatusChanger.demoteStalledInProgress(construction, builder),
                "static hp at 1190f old stays IN_PROGRESS - younger than any legal warp is impossible");
            assertEquals(ConstructionOrderStatus.IN_PROGRESS, construction.status());
        });
    }

    @Test
    public void constructingBuilderKeepsFrozenWarp() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10);
        FakeUnit builder = constructor();
        FakeUnit warp = warpingPylon(100);

        world(2600, units(nexus, builder, warp), new FakeUnit[0], () -> {
            int now = A.now();
            if (now == 10) worked = inProgressPylonAt(builder, 12, 10);
            if (now != 2400 && now != 2450 && now != 2500) return;

            Construction construction = worked;

            assertFalse(
                ConstructionStatusChanger.demoteStalledInProgress(construction, builder),
                "a live constructing builder works it (Terran shape) - static hp alone never demotes");
            assertEquals(ConstructionOrderStatus.IN_PROGRESS, construction.status());
        });
    }

    @Test
    public void miningBuilderWithoutEntityRearmsWhenOld() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10);
        FakeUnit builder = miner();

        world(300, units(nexus, builder), new FakeUnit[0], () -> {
            int now = A.now();
            if (now == 10) entityless = inProgressPylonAt(builder, 12, 10);
            if (now != 250) return;

            Construction construction = entityless;
            assertTrue(
                ConstructionStatusChanger.demoteStalledInProgress(construction, builder),
                "a builder back on minerals is not travelling - no entity, old, re-arm");
            assertEquals(ConstructionOrderStatus.NOT_STARTED, construction.status());
        });
    }
}
