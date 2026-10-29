package tests.unit;

import atlantis.config.env.Env;
import main.Main;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Per-run enemy-race override (owner 2026-10-29): a PvP proof game passes
 * ENEMY_RACE=Protoss in its own --env= file instead of anyone flipping
 * Main's source. The source default stays Zerg; unknown values clear
 * back to it instead of silently re-racing a game.
 */
@Tag("infra")
public class EnemyRaceOverrideTest {

    @AfterEach
    public void clearOverride() {
        Main.setEnemyRaceOverride(null);
    }

    @Test
    public void defaultIsZerg() {
        assertEquals("Zerg", Main.enemyRace(), "the source default never moves");
    }

    @Test
    public void overrideWinsUntilCleared() {
        Main.setEnemyRaceOverride("Protoss");

        assertEquals("Protoss", Main.enemyRace(), "a PvP vehicle reads Protoss");

        Main.setEnemyRaceOverride(null);

        assertEquals("Zerg", Main.enemyRace(), "clearing returns the default");
    }

    @Test
    public void knownRacesCanonicalize() {
        assertEquals("Protoss", Env.canonicalEnemyRace("protoss"));
        assertEquals("Protoss", Env.canonicalEnemyRace("  Protoss  "));
        assertEquals("RandomTPZ", Env.canonicalEnemyRace("randomtpz"));
        assertEquals("Terran", Env.canonicalEnemyRace("TERRAN"));
    }

    @Test
    public void unknownRaceClearsToDefault() {
        assertNull(Env.canonicalEnemyRace("tau"), "a typo is not a race");
        assertNull(Env.canonicalEnemyRace(""), "blank is not a race");
        assertNull(Env.canonicalEnemyRace(null), "absent is not a race");

        Main.setEnemyRaceOverride(Env.canonicalEnemyRace("tau"));

        assertEquals("Zerg", Main.enemyRace(), "junk in ENV keeps the default game a Zerg game");
    }
}
