package atlantis.decisions.board;

/**
 * The single door every issued decision walks through: log it, then hand
 * it to the registered domain executor. Nothing else in the codebase may
 * issue a production act past this point - that exclusivity is what makes
 * DECISIONS.log complete instead of illustrative.
 */
public class DecisionDispatcher {

    private static DecisionExecutor executor = decision -> false;

    private DecisionDispatcher() {
    }

    /** Wired once by the game root (domain owns the implementation). */
    public static void register(DecisionExecutor implementation) {
        executor = implementation;
    }

    /**
     * Ask one question and, on an affirmative answer, issue it through
     * the single door. Null answers (refusals) pass without effect.
     */
    public static boolean askAndIssue(AQuestion question, BoardState state) {
        ADecision decision = question.ask(state);
        if (decision == null) return false;
        return issue(decision);
    }

    /** Issue an already-made decision through the single door. */
    public static boolean issue(ADecision decision) {
        if (decision == null) return false;
        // Logged only when the act lands: DECISIONS.log answers "what did
        // the bot do and why", not "what did it consider" - refused attempts
        // stay with the WORKER_GATE / ORDER_NOT_READY twins.
        boolean done = executor.execute(decision);
        if (done) DecisionLog.record(decision);
        return done;
    }
}
