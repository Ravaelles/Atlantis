package atlantis.decisions.board;

/**
 * Executes one issued decision. Lives in the generic machinery as a
 * contract only: the implementation sits with the domain (production),
 * because only the domain may name engines, queues and builders
 * (CONVENTIONS §21 - machinery imports nothing project-specific).
 */
public interface DecisionExecutor {

    /**
     * Carry out an issued decision. Returns true when the act landed
     * (order accepted); false stays silent - the question simply asks
     * again next frame.
     */
    boolean execute(ADecision decision);
}
