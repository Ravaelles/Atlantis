package tests.unit;

import atlantis.battle.BattleCommander;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The kite coefficients' documented defaults are engine-faithful
 * (PurpleWave margins, jar ranges). The tuner mutates these in-JVM,
 * so an accidental default drift must fail loudly here instead of
 * silently shifting every proof game.
 */
public class TweakerDefaultsTest {

    @Test
    public void kiteDefaultsAreEngineFaithful() {
        assertEquals(5.0, BattleCommander.KITE_ENEMY_TILES);
        assertEquals(0.5, BattleCommander.KITE_GLUE_TILES);
        assertEquals(2.0, BattleCommander.KITE_STEP_TILES);
        assertEquals(0.5, BattleCommander.KITE_HUNTED_TILES);
        assertEquals(0.5, BattleCommander.KITE_FACED_TILES);
        assertEquals(1.0, BattleCommander.KITE_MARGIN_BASE_TILES);
        assertEquals(4.0, BattleCommander.KITE_MARGIN_DYING_TILES);
        assertEquals(0.25, BattleCommander.KITE_MARGIN_PER_HIT_TILES);
        assertEquals(16.0, BattleCommander.KITE_ZEALOT_PASS_DAMAGE);
        assertEquals(0.5, BattleCommander.KITE_STANDOFF_INSET_TILES);
        assertEquals(0.5, BattleCommander.KITE_GLIDE_DEADBAND_TILES);
        assertEquals(30, BattleCommander.KITE_RIDE_FRAMES);
        assertEquals(0.3, BattleCommander.KITE_MIN_STEP_GAIN_TILES);
    }
}
