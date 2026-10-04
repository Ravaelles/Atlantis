package atlantis.util.cache;

/**
 * Implemented by mutable values the {@link Cache} hands out as a copy, so a
 * caller that filters or sorts what it got cannot rewrite the cached entry for
 * everyone else.
 *
 * <p>Sibling of {@link ValidityCheck} and for the same reason: the cache is a
 * kernel class that must not name domain types, so it asks a question through a
 * kernel interface and the value answers it. It used to ask it about
 * {@code atlantis.units.select.Selection} by name - an {@code instanceof} plus a
 * {@code clone()} call, i.e. two frozen rule violations for one special case.</p>
 *
 * <p>Non-implementing values (Booleans, Strings, units, most scalars) are handed
 * out as they are stored: there is nothing to copy.</p>
 *
 * @param <T> the type this value copies itself into, so {@code copy()} is typed
 *            and the cache does not need a cast to answer its own type parameter.
 */
public interface Copyable<T> {

    T copy();
}