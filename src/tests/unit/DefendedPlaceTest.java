package tests.unit;

import atlantis.battle.cluster.DefendedPlace;
import atlantis.map.position.APosition;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeChoke;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * NEXT #194: the mission names one defended ground in a fixed priority
 * (breach, sparta, main, natural, base); the formation decides how to
 * stand on it. Skeleton pin: order first, consumers later.
 */
@Tag("combat")
public class DefendedPlaceTest {

    private static final APosition BASE = APosition.create(100, 100);

    @Test
    public void declarationOrderIsThePriority() {
        DefendedPlace.Kind[] kinds = DefendedPlace.Kind.values();

        assertEquals(5, kinds.length);
        assertEquals(DefendedPlace.Kind.BREACH, kinds[0]);
        assertEquals(DefendedPlace.Kind.SPARTA_CHOKE, kinds[1]);
        assertEquals(DefendedPlace.Kind.MAIN_CHOKE, kinds[2]);
        assertEquals(DefendedPlace.Kind.NATURAL_CHOKE, kinds[3]);
        assertEquals(DefendedPlace.Kind.BASE, kinds[4]);
    }

    @Test
    public void breachOutranksEveryChoke() {
        APosition breach = APosition.create(105, 105);

        DefendedPlace.Answer answer = DefendedPlace.defendGround(
            breach,
            new FakeChoke(APosition.create(60, 60), 3),
            new FakeChoke(APosition.create(70, 70), 3),
            new FakeChoke(APosition.create(80, 80), 3),
            BASE);

        assertNotNull(answer);
        assertEquals(DefendedPlace.Kind.BREACH, answer.kind);
        assertSame(breach, answer.ground);
    }

    @Test
    public void chokesRankSpartaMainNatural() {
        DefendedPlace.Answer sparta = DefendedPlace.defendGround(
            null, new FakeChoke(APosition.create(60, 60), 3), null, null, BASE);
        assertEquals(DefendedPlace.Kind.SPARTA_CHOKE, sparta.kind);

        DefendedPlace.Answer main = DefendedPlace.defendGround(
            null,
            null,
            new FakeChoke(APosition.create(70, 70), 3),
            new FakeChoke(APosition.create(80, 80), 3),
            BASE);
        assertEquals(DefendedPlace.Kind.MAIN_CHOKE, main.kind);

        DefendedPlace.Answer natural = DefendedPlace.defendGround(
            null, null, null, new FakeChoke(APosition.create(80, 80), 3), BASE);
        assertEquals(DefendedPlace.Kind.NATURAL_CHOKE, natural.kind);
    }

    @Test
    public void baseIsTheLastResort() {
        DefendedPlace.Answer answer = DefendedPlace.defendGround(null, null, null, null, BASE);

        assertNotNull(answer);
        assertEquals(DefendedPlace.Kind.BASE, answer.kind);
        assertSame(BASE, answer.ground);
    }

    @Test
    public void nothingQualifiesIsNoOpinion() {
        assertNull(
            DefendedPlace.defendGround(null, null, null, null, null),
            "UMS/baseless: the caller falls back, same as the old null path");
    }
}
