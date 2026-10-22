package tests.unit;

import atlantis.util.log.LogKey;
import atlantis.util.log.LogLevel;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Severity policy, centralized in {@link LogKey}: E2E gates fail on
 * CRITICAL, `--min-level HIGH` filters friction, everything else is
 * NORMAL chatter. Pinned so a new key lands in a set deliberately,
 * not by falling through the default.
 */
@Tag("infra")
public class LogKeySeverityTest {

    @Test
    public void criticalIsTotalFailureOnly() {
        assertEquals(LogLevel.CRITICAL, LogKey.BUILD_BREAKDOWN.level());
        assertEquals(LogLevel.CRITICAL, LogKey.PLACEMENT_FAILED.level());
    }

    @Test
    public void economyDiseaseIsCritical() {
        assertEquals(LogLevel.CRITICAL, LogKey.SUPPLY_OVERSTOCK.level());
        assertEquals(LogLevel.CRITICAL, LogKey.PYLON_FLOOD.level());
        assertEquals(LogLevel.CRITICAL, LogKey.BANK_HOARD.level());
    }

    @Test
    public void highIsFriction() {
        assertEquals(LogLevel.HIGH, LogKey.REQUEST_REFUSED_DUPLICATE.level());
        assertEquals(LogLevel.HIGH, LogKey.BUILD_REFUSED.level());
        assertEquals(LogLevel.HIGH, LogKey.CONSTRUCTION_CANCEL.level());
        assertEquals(LogLevel.HIGH, LogKey.CLOSE_WARPS.level());
    }

    @Test
    public void theRestIsChatter() {
        assertEquals(LogLevel.NORMAL, LogKey.BOARD.level());
        assertEquals(LogLevel.NORMAL, LogKey.GAME_END.level());
        assertEquals(LogLevel.NORMAL, LogKey.ADOPTED_CONSTRUCTION.level());
        assertEquals(LogLevel.NORMAL, LogKey.WORKER_GATE.level());
        assertEquals(LogLevel.NORMAL, LogKey.GAS_FIX.level());
        assertEquals(LogLevel.NORMAL, LogKey.POSITION_REFRESH.level());
        assertEquals(LogLevel.NORMAL, LogKey.CONSTRUCTION_PREPARING.level());
        assertEquals(LogLevel.NORMAL, LogKey.SINGULARITY_APPLIES.level());
        assertEquals(LogLevel.NORMAL, LogKey.DRAGOON_TRAIN.level());
        assertEquals(LogLevel.NORMAL, LogKey.TRAIN_ATTEMPT.level());
        assertEquals(LogLevel.NORMAL, LogKey.CLUSTER.level());
        assertEquals(LogLevel.NORMAL, LogKey.CATALOGUE_SITE.level());
        assertEquals(LogLevel.NORMAL, LogKey.KITE.level());
        assertEquals(LogLevel.NORMAL, LogKey.HOLD.level());
        assertEquals(LogLevel.NORMAL, LogKey.MISSION.level());
        assertEquals(LogLevel.NORMAL, LogKey.HIT.level());
        assertEquals(LogLevel.NORMAL, LogKey.SHOT.level());
        assertEquals(LogLevel.NORMAL, LogKey.SUPPLY_BLOCKED.level());
        assertEquals(LogLevel.NORMAL, LogKey.QUEUE_BASE.level());
        assertEquals(LogLevel.NORMAL, LogKey.BOARD_FED.level());
    }}
