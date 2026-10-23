package tests.unit;

import atlantis.util.log.LogKey;
import atlantis.util.log.LogLevel;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Severity policy, centralized in {@link LogKey}: E2E gates fail on
 * CRITICAL, `--min-level HIGH` filters friction, everything else is
 * MEDIUM chatter. Pinned so a new key lands in a set deliberately,
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
        assertEquals(LogLevel.MEDIUM, LogKey.BOARD.level());
        assertEquals(LogLevel.MEDIUM, LogKey.GAME_END.level());
        assertEquals(LogLevel.MEDIUM, LogKey.ADOPTED_CONSTRUCTION.level());
        assertEquals(LogLevel.MEDIUM, LogKey.WORKER_GATE.level());
        assertEquals(LogLevel.MEDIUM, LogKey.GAS_FIX.level());
        assertEquals(LogLevel.MEDIUM, LogKey.POSITION_REFRESH.level());
        assertEquals(LogLevel.MEDIUM, LogKey.CONSTRUCTION_PREPARING.level());
        assertEquals(LogLevel.MEDIUM, LogKey.SINGULARITY_APPLIES.level());
        assertEquals(LogLevel.MEDIUM, LogKey.DRAGOON_TRAIN.level());
        assertEquals(LogLevel.MEDIUM, LogKey.TRAIN_ATTEMPT.level());
        assertEquals(LogLevel.MEDIUM, LogKey.CLUSTER.level());
        assertEquals(LogLevel.MEDIUM, LogKey.CATALOGUE_SITE.level());
        assertEquals(LogLevel.MEDIUM, LogKey.KITE.level());
        assertEquals(LogLevel.MEDIUM, LogKey.HOLD.level());
        assertEquals(LogLevel.MEDIUM, LogKey.MISSION.level());
        assertEquals(LogLevel.MEDIUM, LogKey.HIT.level());
        assertEquals(LogLevel.MEDIUM, LogKey.SHOT.level());
        assertEquals(LogLevel.MEDIUM, LogKey.SUPPLY_BLOCKED.level());
        assertEquals(LogLevel.MEDIUM, LogKey.QUEUE_BASE.level());
        assertEquals(LogLevel.MEDIUM, LogKey.BOARD_FED.level());
    }}
