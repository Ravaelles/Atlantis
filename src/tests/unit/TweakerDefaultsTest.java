package tests.unit;

import atlantis.battle.BattleCommander;
import atlantis.battle.kite.CombatTuning;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * The kite coefficients' documented defaults are engine-faithful
 * (PurpleWave margins, jar ranges). The tuner installs derived snapshots
 * in-JVM, so an accidental default drift must fail loudly here instead of
 * silently shifting every proof game. Defaults pin against a fresh
 * instance - never against the shared holder, which a previous test may
 * have replaced.
 */
public class TweakerDefaultsTest {

    @Test
    public void kiteDefaultsAreEngineFaithful() {
        CombatTuning defaults = CombatTuning.defaults();

        assertEquals(5.0, defaults.getKiteEnemyTiles());
        assertEquals(0.5, defaults.getKiteGlueTiles());
        assertEquals(2.0, defaults.getKiteStepTiles());
        assertEquals(0.5, defaults.getKiteHuntedTiles());
        assertEquals(0.5, defaults.getKiteFacedTiles());
        assertEquals(1.0, defaults.getKiteMarginBaseTiles());
        assertEquals(4.0, defaults.getKiteMarginDyingTiles());
        assertEquals(0.25, defaults.getKiteMarginPerHitTiles());
        assertEquals(16.0, defaults.getKiteZealotPassDamage());
        assertEquals(0.5, defaults.getKiteStandoffInsetTiles());
        assertEquals(0.5, defaults.getKiteGlideDeadbandTiles());
        assertEquals(30, defaults.getKiteRideFrames());
        assertEquals(0.3, defaults.getKiteMinStepGainTiles());
        assertEquals(80, defaults.getDragoonPinchHp());
        assertEquals(2, defaults.getDragoonPinchMeleeCount());
        assertEquals(2.0, defaults.getDragoonPinchMeleeTiles());
    }

    @Test
    public void holderStartsAtDefaultsAndResetRestoresThem() {
        BattleCommander.clearForTests();

        assertEquals(5.0, BattleCommander.tuning().getKiteEnemyTiles());

        BattleCommander.installTuning(BattleCommander.tuning().withKiteEnemyTiles(4.2));
        assertEquals(4.2, BattleCommander.tuning().getKiteEnemyTiles());

        BattleCommander.reset();
        assertEquals(5.0, BattleCommander.tuning().getKiteEnemyTiles(),
                "per-game reset restores defaults before the tuner installs its snapshot");
    }

    @Test
    public void snapshotsAreImmutableCopies() {
        CombatTuning base = CombatTuning.defaults();
        CombatTuning derived = base.withKiteStepTiles(2.5);

        assertEquals(2.0, base.getKiteStepTiles(), "deriving must not mutate the source");
        assertEquals(2.5, derived.getKiteStepTiles());
        assertEquals(base.getKiteEnemyTiles(), derived.getKiteEnemyTiles(),
                "a derived snapshot carries every untouched value along");
    }

    @Test
    public void installIgnoresNull() {
        BattleCommander.clearForTests();
        CombatTuning before = BattleCommander.tuning();

        BattleCommander.installTuning(null);

        assertSame(before, BattleCommander.tuning(), "a null install must not clear the holder");
    }
}
