package tests.unit.sim;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import atlantis.combat.BattleCommander;
import atlantis.battle.cluster.Alpha;
import atlantis.combat.cluster.FightOrFlight;
import atlantis.units.AUnitType;
import tests.fakes.FakeUnit;
import tests.unit.AbstractTestWithUnits;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;

/**
 * Sim repro of the live glue (NEXT #190): TauCross PvsZ VICTORY 9450f,
 * Dragoons #165 (48 stuck-no-step + 16 hits f8855-9024, died f9033) and
 * #173 (126 stuck-no-step + 19 hits f8870-9152, died f9162) - cut off
 * from the army with chewers on them, the cutoff verdicts fight in
 * place (FIGHT_CUTOFF has no odds check) and lose the DPS race
 * standing. canCmd+canMove all true: validation side, not refusal.
 *
 * <p>Shape: a dragoon detached from its army, backed into a pocket
 * mouth with four zerglings chewing, FLEE tactic. The current code
 * fights the hopeless race in place (red): near-zero displacement
 * and/or death in the pocket. The run-escalation fix turns this green
 * by marching home instead of donating.
 */
@Tag("combat")
public class PocketGluedDragoonTest extends AbstractTestWithUnits {

    private static final int TILE = 32;

    @Test
    public void cutoffSwampedDragoonRunsHome() {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 13, 19);
        dragoon.hp = 120;
        dragoon.shields = 0;
        dragoon.cooldown = 10;

        // Army far east: the test dragoon is cut off from it.
        FakeUnit a = fake(AUnitType.Protoss_Dragoon, 50, 20);
        FakeUnit b = fake(AUnitType.Protoss_Dragoon, 51, 20);
        FakeUnit c = fake(AUnitType.Protoss_Dragoon, 52, 20);

        FakeUnit l1 = fake(AUnitType.Zerg_Zergling, 18, 18);
        FakeUnit l2 = fake(AUnitType.Zerg_Zergling, 18, 19);
        FakeUnit l3 = fake(AUnitType.Zerg_Zergling, 18, 20);
        FakeUnit l4 = fake(AUnitType.Zerg_Zergling, 19, 19);
        FakeUnit[] lings = new FakeUnit[] { l1, l2, l3, l4 };
        for (FakeUnit ling : lings) ling.setEnemy();

        // Home base far east: a RALLY verdict marches far, not in place.
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 55, 25);

        // Pocket mouth: walls west/north/south, open east toward home.
        SimArena arena = new SimArena(70 * TILE, 40 * TILE);
        arena.block(10 * TILE, 17 * TILE, 1 * TILE, 5 * TILE);
        arena.block(10 * TILE, 17 * TILE, 5 * TILE, 1 * TILE);
        arena.block(10 * TILE, 21 * TILE, 5 * TILE, 1 * TILE);
        useArenaWalkability(arena);

        int startX = dragoon.position.x();
        int startY = dragoon.position.y();
        int deathTick = -1;

        FakeUnit[] ours = new FakeUnit[] { dragoon, a, b, c, nexus };
        SimWorld world = new SimWorld(ours, lings, arena);
        try {
            BattleCommander.clearForTests();
            atlantis.util.log.EventLog.clearForTests();
            Alpha.gather();

            for (int i = 0; i < 400; i++) {
                world.tick(() -> {
                    if (dragoon.hp > 0) {
                        BattleCommander.act(
                            dragoon, FightOrFlight.Tactic.FLEE, null, dragoon);
                    }
                    ZombiePolicies.chaseNearest(
                        Arrays.asList(lings),
                        Collections.singletonList(dragoon));
                });
                if (dragoon.hp <= 0 && deathTick < 0) deathTick = i;
            }

            double displacement = Math.hypot(
                dragoon.position.x() - startX,
                dragoon.position.y() - startY) / TILE;
            assertTrue(displacement >= 5.0,
                "a swamped cutoff dragoon runs home instead of donating in the pocket, got displacement "
                    + displacement + (deathTick >= 0 ? ", died tick " + deathTick : ", survived"));
        }
        finally {
            for (FakeUnit u : ours) Alpha.get().removeUnit(u);
            world.close();
            BattleCommander.clearForTests();
            atlantis.util.log.EventLog.clearForTests();
        }
    }

    private void useArenaWalkability(final SimArena arena) {
        atlantis.map.MapTiles.useSource(new atlantis.map.MapTiles.Source() {
            @Override
            public boolean isWalkable(atlantis.map.position.HasPosition at) {
                atlantis.map.position.APosition p = at.position();

                return arena.isWalkablePx(p.x(), p.y());
            }

            @Override
            public boolean isExplored(atlantis.map.position.HasPosition at) {
                return true;
            }

            @Override
            public boolean isVisible(atlantis.map.position.HasPosition at) {
                return true;
            }

            @Override
            public boolean hasPathBetween(atlantis.map.position.HasPosition from, atlantis.map.position.HasPosition to) {
                return true;
            }

            @Override
            public boolean isBuildable(atlantis.map.position.HasPosition at, boolean alsoCheckBuildings) {
                return arena.isWalkablePx(at.position().x(), at.position().y());
            }

            @Override
            public boolean canBuildHere(atlantis.units.AUnit builder, atlantis.units.AUnitType building, atlantis.map.position.APosition at) {
                return arena.isWalkablePx(at.position().x(), at.position().y());
            }
        });
    }
}
