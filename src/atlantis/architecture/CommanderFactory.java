package atlantis.architecture;

/**
 * Stage C (see _AI/REVIEW.md §16): explicit construction of Commander children.
 *
 * <p>A {@code CommanderFactory} is a plain constructor reference
 * (e.g. {@code CombatCommander::new}). It replaces the former
 * {@code Class[]} + reflection in {@link BaseCommander}, so the per-game
 * commander tree is built without reflection and the execution order is
 * exactly the declaration order.</p>
 */
public interface CommanderFactory {
    Commander create();
}
