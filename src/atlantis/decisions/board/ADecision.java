package atlantis.decisions.board;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * One issued production act: what, why, from where, on what state.
 *
 * <p>Immutable value, engine-free by construction (strings and ints only):
 * a decision can be built from a stub state and pinned in milliseconds,
 * which is the whole point - the engine never enters the test. Producers
 * ({@link AQuestion}) emit these; exactly one dispatcher executes them
 * and logs them to DECISIONS.log. HTN nesting arrives through
 * {@link #children}: a decision decomposes into smaller decisions
 * (site, builder, order) without changing type.</p>
 */
public final class ADecision {

    /** What kind of act this is. Never a game type - the vocabulary stays fixed. */
    public enum Kind {
        TRAIN_WORKER,
        TRAIN_ARMY,
        BUILD,
        RESEARCH,
        EXPAND
    }

    /** Where the decision came from. */
    public enum Source {
        /** A memorized build-order line, executed verbatim. */
        BUILD_ORDER,
        /** A dynamic pump answering a live question. */
        DYNAMIC
    }

    private final Kind kind;
    private final String subject;
    private final String reason;
    private final Source source;
    private final int frame;
    private final int supplyUsed;
    private final int supplyTotal;
    private final int minerals;
    private final int gas;
    private final List<ADecision> children;
    /**
     * The demand-side order behind this act, when there is one (a queued
     * production row, a sited request). Null for orderless acts. Logged
     * as {@code oid} so every DECISIONS row joins its EVENTS chain
     * (REQUESTED/PREPARING/STARTED/CANCEL/ledger all carry cid+oid) -
     * without it repeats for one order read as one row each and the
     * cycle behind them is guesswork.
     */
    private final Integer orderId;

    /**
     * Engine-side payload (a base, an order, ...). Never logged, never
     * tested: it exists so the domain executor can act without
     * re-resolving what the caller already holds. Null in tests.
     */
    private final Object payload;

    private ADecision(Builder builder) {
        this.kind = builder.kind;
        this.subject = builder.subject;
        this.reason = builder.reason;
        this.source = builder.source;
        this.frame = builder.frame;
        this.supplyUsed = builder.supplyUsed;
        this.supplyTotal = builder.supplyTotal;
        this.minerals = builder.minerals;
        this.gas = builder.gas;
        this.payload = builder.payload;
        this.orderId = builder.orderId;
        this.children = Collections.unmodifiableList(new ArrayList<>(builder.children));
    }

    public Kind kind() {
        return kind;
    }

    public String subject() {
        return subject;
    }

    public String reason() {
        return reason;
    }

    public Source source() {
        return source;
    }

    public int frame() {
        return frame;
    }

    public int supplyUsed() {
        return supplyUsed;
    }

    public int supplyTotal() {
        return supplyTotal;
    }

    public int minerals() {
        return minerals;
    }

    public int gas() {
        return gas;
    }

    public List<ADecision> children() {
        return children;
    }

    public Integer orderId() {
        return orderId;
    }

    /**
     * Copy stamped with the issue frame. Questions answer against
     * snapshots (frame unknown there); the door stamps the clock.
     */
    public ADecision atFrame(int frame) {
        return new ADecision(kind, subject, reason, source,
            frame, supplyUsed, supplyTotal, minerals, gas, payload, children, orderId);
    }

    public Object payload() {
        return payload;
    }

    public ADecision withChild(ADecision child) {
        List<ADecision> grown = new ArrayList<>(children);
        grown.add(child);
        return new ADecision(kind, subject, reason, source,
            frame, supplyUsed, supplyTotal, minerals, gas, payload, grown, orderId);
    }

    public ADecision withOrderId(Integer orderId) {
        return new ADecision(kind, subject, reason, source,
            frame, supplyUsed, supplyTotal, minerals, gas, payload, children, orderId);
    }

    /**
     * The wrapper's precision pass: a question answers generically
     * ("a worker is wanted"), the call site names the concrete act
     * (which type, which order, BO or dynamic) without touching the
     * audited state snapshot. Returns a new decision either way.
     */
    public ADecision refine(String subject, String reason, Source source, Object payload) {
        return new ADecision(kind, subject, reason, source,
            frame, supplyUsed, supplyTotal, minerals, gas, payload, children, orderId);
    }

    private ADecision(Kind kind, String subject, String reason, Source source,
                      int frame, int supplyUsed, int supplyTotal,
                      int minerals, int gas, Object payload, List<ADecision> children,
                      Integer orderId) {
        this.kind = kind;
        this.subject = subject;
        this.reason = reason;
        this.source = source;
        this.frame = frame;
        this.supplyUsed = supplyUsed;
        this.supplyTotal = supplyTotal;
        this.minerals = minerals;
        this.gas = gas;
        this.children = Collections.unmodifiableList(new ArrayList<>(children));
        this.payload = payload;
        this.orderId = orderId;
    }

    public static Builder decide(Kind kind, String subject, String reason, Source source) {
        return new Builder(kind, subject, reason, source);
    }

    public static final class Builder {
        private final Kind kind;
        private final String subject;
        private final String reason;
        private final Source source;
        private int frame;
        private int supplyUsed;
        private int supplyTotal;
        private int minerals;
        private int gas;
        private Object payload;
        private Integer orderId;
        private final List<ADecision> children = new ArrayList<>();

        public Builder(Kind kind, String subject, String reason, Source source) {
            this.kind = kind;
            this.subject = subject;
            this.reason = reason;
            this.source = source;
        }

        public Builder frame(int frame) {
            this.frame = frame;
            return this;
        }

        public Builder supply(int used, int total) {
            this.supplyUsed = used;
            this.supplyTotal = total;
            return this;
        }

        public Builder bank(int minerals, int gas) {
            this.minerals = minerals;
            this.gas = gas;
            return this;
        }

        public Builder payload(Object payload) {
            this.payload = payload;
            return this;
        }

        public Builder orderId(Integer orderId) {
            this.orderId = orderId;
            return this;
        }

        public ADecision build() {
            return new ADecision(this);
        }
    }
}
