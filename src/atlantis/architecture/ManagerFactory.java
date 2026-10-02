package atlantis.architecture;

import atlantis.units.AUnit;

/**
 * Stage C (see _AI/REVIEW.md §16): explicit construction of Manager children.
 *
 * <p>A {@code ManagerFactory} is a plain constructor reference
 * (e.g. {@code WorkerManager::new}). It replaces the former
 * {@code Class[]} + reflection in {@link BaseManager}, so the per-unit,
 * per-frame manager tree is built without reflection and the execution order
 * is exactly the declaration order.</p>
 */
public interface ManagerFactory {
    Manager create(AUnit unit);
}
