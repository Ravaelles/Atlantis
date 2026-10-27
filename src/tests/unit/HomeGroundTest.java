package tests.unit;

import atlantis.combat.cluster.HomeGround;
import atlantis.map.choke.AChoke;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeChoke;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * HomeGround (NEXT #194): the main base plus the main choke, resolved
 * once per resolution for every anchor and rally instead of once per
 * seam. The contract is the old question every consumer asked on its
 * own - a home is a main with a position and a choke with a center -
 * so converting a seam to it changes no answer, only the owner of the
 * question. That is what pins here: the complete pair, each missing
 * half, the empty world, and the live resolution never throwing.
 */
@Tag("combat")
public class HomeGroundTest extends WorldStubForTests {

    @Test
    public void aMainWithAPositionAndAChokeWithACenterIsHome() {
        FakeUnit main = fake(AUnitType.Protoss_Nexus, 20, 20);
        AChoke choke = new FakeChoke(APosition.create(60, 20), 5);

        HomeGround home = HomeGround.resolve(main, choke);

        assertTrue(home.isComplete(), "this is exactly the old standing question");
        assertSame(home.main(), main, "the resolver hands over, never copies");
        assertSame(home.mainChoke(), choke, "the resolver hands over, never copies");
    }

    @Test
    public void noMainIsNoHome() {
        AChoke choke = new FakeChoke(APosition.create(60, 20), 5);

        assertFalse(
            HomeGround.resolve(null, choke).isComplete(),
            "every consumer returned null without a main - the resolver says so once"
        );
    }

    @Test
    public void noChokeIsNoHome() {
        FakeUnit main = fake(AUnitType.Protoss_Nexus, 20, 20);

        assertFalse(
            HomeGround.resolve(main, null).isComplete(),
            "every consumer returned null without a choke - the resolver says so once"
        );
    }

    @Test
    public void anEmptyWorldResolvesIncompleteInsteadOfThrowing() {
        FakeUnit main = fake(AUnitType.Protoss_Nexus, 20, 20);

        AUnit dead = null;
        assertFalse(
            HomeGround.resolve(dead, null).isComplete(),
            "nulls in, incomplete out, no throw - the live path leans on this"
        );
    }

    @Test
    public void liveResolutionInAStubWorldFindsHome() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);

        world(5, fakeOurs(nexus), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 2) return;

            // The stub world wires both halves (the main from our units,
            // the choke from the stub map), so live resolves complete -
            // and, more importantly, resolves at all instead of throwing.
            HomeGround home = HomeGround.resolve();
            assertTrue(home.isComplete(), "stub main + stub choke resolve live");
            assertSame(home.main(), nexus, "the live main is our Nexus");
        });
    }
}
