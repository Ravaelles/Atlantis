package tests.unit.sim;

import atlantis.map.position.APosition;
import atlantis.units.AUnitType;
import tests.fakes.FakeUnit;

/**
 * One bot-issued order as a value, with the engine's answer attached.
 *
 * <p>The sim consumes {@code FakeOrderSink} records and turns each into
 * one of these; the validator answers ACCEPTED or REJECTED with the
 * reason in plain words ("footprint tile [x,y] not buildable" reads
 * like the engine, not like a null). Rejected orders never move sim
 * state - exactly like BWAPI dropping an illegal command. The scenario
 * test asserts the rejection log stays empty: a bot that issues orders
 * the engine would refuse is a finding, not a quirk.</p>
 */
public final class SimOrder {

    public enum Verb {
        BUILD,
        MOVE,
        TRAIN,
        RESEARCH,
        UPGRADE,
        GATHER
    }

    public enum Verdict {
        ACCEPTED,
        REJECTED
    }

    public final Verb verb;
    public final FakeUnit actor;
    public final AUnitType subject;
    public final APosition tile;
    public final Verdict verdict;
    public final String reason;

    private SimOrder(Verb verb, FakeUnit actor, AUnitType subject, APosition tile,
            Verdict verdict, String reason) {
        this.verb = verb;
        this.actor = actor;
        this.subject = subject;
        this.tile = tile;
        this.verdict = verdict;
        this.reason = reason;
    }

    public static SimOrder accepted(Verb verb, FakeUnit actor, AUnitType subject, APosition tile) {
        return new SimOrder(verb, actor, subject, tile, Verdict.ACCEPTED, "-");
    }

    public static SimOrder rejected(Verb verb, FakeUnit actor, AUnitType subject, APosition tile,
            String reason) {
        return new SimOrder(verb, actor, subject, tile, Verdict.REJECTED, reason);
    }

    public boolean accepted() {
        return verdict == Verdict.ACCEPTED;
    }

    @Override
    public String toString() {
        return verb + " " + (subject == null ? "-" : subject.name())
            + " @" + (tile == null ? "-" : "[" + tile.tx() + "," + tile.ty() + "]")
            + " -> " + verdict + (accepted() ? "" : " (" + reason + ")");
    }
}
