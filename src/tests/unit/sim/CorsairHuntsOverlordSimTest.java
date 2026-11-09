package tests.unit.sim;

import atlantis.combat.behaviors.protoss.corsair.CorsairSoloFlight;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;

/**
 * Corsair solo hunt on StarEngine (owner 2026-11-09): an idle Corsair
 * with a known Overlord on the board closes the distance and kills it
 * instead of standing on #NO_COMMAND. Runs the real solo path
 * ({@code CorsairSoloFlight.drive}) every tick.
 */
@Tag("combat")
@Tag("full-tier")
public class CorsairHuntsOverlordSimTest extends AbstractTestWithWorld {

    private static final int TILE = 32;

    @Override
    protected FakeUnit[] generateOur() {
        return null;
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return null;
    }

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @Override
    public Race initEnemyRace() {
        return Race.Zerg;
    }

    @Test
    public void soloCorsairClosesAndKillsOverlord() {
        FakeUnit corsair = fake(AUnitType.Protoss_Corsair, 20, 20);
        corsair.cooldown = 0;
        FakeUnit overlord = fake(AUnitType.Zerg_Overlord, 50, 20);
        overlord.setEnemy();
        int overlordMaxHp = overlord.hp;

        List<FakeUnit> ours = new ArrayList<>();
        ours.add(corsair);
        List<FakeUnit> foes = new ArrayList<>();
        foes.add(overlord);

        SimArena arena = new SimArena(130 * TILE, 130 * TILE);
        SimWorld world = new SimWorld(
            ours.toArray(new FakeUnit[0]), foes.toArray(new FakeUnit[0]), arena);

        try {
            atlantis.combat.BattleCommander.clearForTests();
            atlantis.information.enemy.EnemyUnitsUpdater.weDiscoveredEnemyUnit(overlord);

            int firstDamageFrame = -1;
            for (int i = 0; i < 2000; i++) {
                world.tick(CorsairSoloFlight::drive);
                if (overlord.hp < overlordMaxHp) {
                    firstDamageFrame = world.frame();
                    break;
                }
            }

            assertTrue(firstDamageFrame >= 0,
                "the solo Corsair engages the known Overlord instead of idling: corsair at "
                    + (int) corsair.tx() + "," + (int) corsair.ty()
                    + " overlord hp " + overlord.hp + "/" + overlordMaxHp);
        }
        finally {
            world.close();
            atlantis.combat.BattleCommander.clearForTests();
        }
    }
}
