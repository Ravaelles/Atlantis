package tests.unit;

import atlantis.battle.BattleCommander;
import atlantis.battle.escape.EscapeTuning;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * NEXT #172 step 5: escape scoring weights are tunable snapshots, same
 * recipe as CombatTuning. Defaults equal the hand-tuned scoring
 * constants, so registration alone changes no game; the tuner installs
 * derived snapshots per batch game.
 */
@Tag("combat")
public class EscapeTuningTest {

    @AfterEach
    public void resetTuning() {
        EscapeTuning.resetToDefaults();
    }

    @Test
    public void defaultsEqualTheHandTunedConstants() {
        EscapeTuning defaults = EscapeTuning.defaults();

        assertEquals(10.0, defaults.getStaticWeight());
        assertEquals(8.0, defaults.getCornerWeight());
        assertEquals(20.0, defaults.getPocketExtra());
        assertEquals(5.0, defaults.getEnemyWeight());
        assertEquals(3.0, defaults.getStaticRadiusTiles());
        assertEquals(20.0, defaults.getEnemyRadiusTiles());
    }

    @Test
    public void holderStartsAtDefaultsAndResetRestoresThem() {
        BattleCommander.clearForTests();

        assertEquals(10.0, EscapeTuning.current().getStaticWeight());

        EscapeTuning.install(EscapeTuning.current().withStaticWeight(12.5));
        assertEquals(12.5, EscapeTuning.current().getStaticWeight());

        BattleCommander.reset();
        assertEquals(10.0, EscapeTuning.current().getStaticWeight(),
                "per-game reset restores defaults before the tuner installs its snapshot");
    }

    @Test
    public void snapshotsAreImmutableCopies() {
        EscapeTuning base = EscapeTuning.defaults();
        EscapeTuning derived = base.withEnemyWeight(6.0);

        assertEquals(5.0, base.getEnemyWeight(), "deriving must not mutate the source");
        assertEquals(6.0, derived.getEnemyWeight());
        assertEquals(base.getStaticWeight(), derived.getStaticWeight(),
                "a derived snapshot carries every untouched value along");
    }

    @Test
    public void installIgnoresNull() {
        EscapeTuning before = EscapeTuning.current();

        EscapeTuning.install(null);

        assertSame(before, EscapeTuning.current(), "a null install must not clear the holder");
    }
}
