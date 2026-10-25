package tests.unit;

import atlantis.combat.cluster.BlockingArmy;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Stardust rule (owner 2026-10-23): never run through a blocking army.
 * Pure corridor geometry - no game needed.
 */
@Tag("combat")
public class BlockingArmyTest {

    @Test
    public void enemyOnTheWayHomeBlocks() {
        assertTrue(BlockingArmy.blocksPath(0, 0, 100, 0, 50, 2, 4),
            "armed enemy 2 tiles off the home segment blocks");
    }

    @Test
    public void enemyBehindUsDoesNotBlock() {
        assertFalse(BlockingArmy.blocksPath(0, 0, 100, 0, -10, 0, 4),
            "pursuer behind the cluster is chased, not blocking");
    }

    @Test
    public void enemyPastHomeDoesNotBlock() {
        assertFalse(BlockingArmy.blocksPath(0, 0, 100, 0, 110, 0, 4),
            "enemy beyond home is irrelevant to the retreat");
    }

    @Test
    public void enemyFarOffTheCorridorDoesNotBlock() {
        assertFalse(BlockingArmy.blocksPath(0, 0, 100, 0, 50, 20, 4),
            "20 tiles off the corridor is not in the way");
    }

    @Test
    public void degenerateSegmentNeverBlocks() {
        assertFalse(BlockingArmy.blocksPath(7, 7, 7, 7, 7, 8, 4),
            "no segment, no block");
    }
}
