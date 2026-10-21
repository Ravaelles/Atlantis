package tests.unit;

import atlantis.game.listeners.WarpedBuildingRecorder;
import atlantis.units.AUnitType;
import atlantis.util.log.EventLog;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;

import java.io.File;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Two same-type warps landing close together name themselves: at Pylon
 * cadence (a warp every 10-15 s) that proximity is a duplicate signal
 * (owner 2026-10-19). Without the twin the two ledger rows read as two
 * healthy warps. Pipeline overlap also lands close, so this is a
 * signal, not a verdict.
 */
@Tag("infra")
public class CloseWarpsTwinTest extends WorldStubForTests {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    private byte[] backup;
    private boolean hadFile;

    @BeforeEach
    public void backupAndClean() throws Exception {
        File out = new File(atlantis.game.util.BuildingLedger.OUTPUT_PATH);
        hadFile = out.exists();
        backup = hadFile ? Files.readAllBytes(out.toPath()) : null;
        EventLog.clearForTests();
        WarpedBuildingRecorder.clearForTests();
    }

    @AfterEach
    public void restore() throws Exception {
        EventLog.clearForTests();
        WarpedBuildingRecorder.clearForTests();
        atlantis.game.util.BuildingLedger.clearForTests();
        File out = new File(atlantis.game.util.BuildingLedger.OUTPUT_PATH);
        if (hadFile) {
            Files.write(out.toPath(), backup);
        }
        else if (out.exists()) {
            out.delete();
        }
    }

    @Test
    public void closeSameTypeWarpsNameThemselves() {
        world(400, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 30, 20)
        ), fakeEnemies(), () -> {
            int now = atlantis.game.A.now();
            if (now == 3) {
                WarpedBuildingRecorder.record(fake(AUnitType.Protoss_Pylon, 16, 46));
                assertFalse(EventLog.toJsonLines().contains("CLOSE_WARPS"),
                    "first warp of a type has nothing to pair with");
            }
            if (now == 50) {
                WarpedBuildingRecorder.record(fake(AUnitType.Protoss_Pylon, 18, 46));
                String rows = EventLog.toJsonLines();
                assertTrue(rows.contains("\"key\":\"CLOSE_WARPS\""), "got: " + rows);
                assertTrue(rows.contains("\"gap\":47"), "got: " + rows);
            }
        });
    }

    @Test
    public void distantWarpsStaySilent() {
        world(400, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 30, 20)
        ), fakeEnemies(), () -> {
            int now = atlantis.game.A.now();
            if (now == 3) {
                WarpedBuildingRecorder.record(fake(AUnitType.Protoss_Pylon, 16, 46));
            }
            if (now == 50) {
                WarpedBuildingRecorder.record(fake(AUnitType.Protoss_Pylon, 18, 46));
                EventLog.clearForTests();
            }
            if (now == 340) {
                // 290 frames after the previous warp: a healthy cadence.
                WarpedBuildingRecorder.record(fake(AUnitType.Protoss_Pylon, 20, 46));
                assertFalse(EventLog.toJsonLines().contains("CLOSE_WARPS"));
            }
        });
    }

    @Test
    public void otherTypesDoNotPair() {
        world(400, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 30, 20)
        ), fakeEnemies(), () -> {
            int now = atlantis.game.A.now();
            if (now == 3) {
                WarpedBuildingRecorder.record(fake(AUnitType.Protoss_Pylon, 16, 46));
            }
            if (now == 50) {
                WarpedBuildingRecorder.record(fake(AUnitType.Protoss_Gateway, 16, 46));
                assertFalse(EventLog.toJsonLines().contains("CLOSE_WARPS"));
            }
        });
    }
}
