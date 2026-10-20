package tests.unit;

import atlantis.information.tech.UpgradeCompletionTracker;
import bwapi.UpgradeType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Upgrade completions must be observable (NEXT #85 needs L2/L3 proof the
 * way #72 needed its twin): exactly one row per level increase, silence
 * otherwise, upgrades tracked independently.
 */
@Tag("production")
public class UpgradeCompletionTrackerTest {

    @AfterEach
    public void clean() {
        UpgradeCompletionTracker.clearForTests();
    }

    @Test
    public void firstSightingRecordsBaselineWithoutFiring() {
        assertFalse(UpgradeCompletionTracker.note(UpgradeType.Protoss_Ground_Weapons, 0, 1, "0:00"));
        assertFalse(UpgradeCompletionTracker.note(UpgradeType.Protoss_Ground_Weapons, 0, 2, "0:00"));
    }

    @Test
    public void levelIncreaseFiresOnce() {
        UpgradeCompletionTracker.clearForTests();
        assertFalse(UpgradeCompletionTracker.note(UpgradeType.Protoss_Ground_Weapons, 1, 1, "0:00"));
        assertTrue(UpgradeCompletionTracker.note(UpgradeType.Protoss_Ground_Weapons, 2, 2, "0:00"));
        assertFalse(UpgradeCompletionTracker.note(UpgradeType.Protoss_Ground_Weapons, 2, 3, "0:00"));
    }

    @Test
    public void upgradesTrackedIndependently() {
        UpgradeCompletionTracker.clearForTests();
        assertFalse(UpgradeCompletionTracker.note(UpgradeType.Protoss_Ground_Weapons, 1, 1, "0:00"));
        assertFalse(UpgradeCompletionTracker.note(UpgradeType.Protoss_Ground_Armor, 0, 1, "0:00"));
        assertTrue(UpgradeCompletionTracker.note(UpgradeType.Protoss_Ground_Armor, 1, 2, "0:00"));
        assertFalse(UpgradeCompletionTracker.note(UpgradeType.Protoss_Ground_Weapons, 1, 3, "0:00"));
    }
}
