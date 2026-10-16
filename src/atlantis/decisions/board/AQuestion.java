package atlantis.decisions.board;

/**
 * One production question: should we do X in this state?
 *
 * <p>The single output contract for every producer module: a module may
 * read anything as input, but it emits only decisions. An affirmative
 * answer is an {@link ADecision} naming what, why and from where; a
 * refusal is {@code null} - nothing happens, silently, and the question
 * is asked again next frame. No booleans, no side effects, no scattered
 * queue writes: the effect lives in the returned object, which is why a
 * question pins in a unit test and a veto chain never could.</p>
 */
public interface AQuestion {

    /**
     * Ask once against a snapshot. Returns the decision to issue, or null
     * to refuse (the frame passes without effect).
     */
    ADecision ask(BoardState state);
}
