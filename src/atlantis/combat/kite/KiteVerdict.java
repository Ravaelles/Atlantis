package atlantis.combat.kite;

/**
 * What a per-unit kite policy decided, in one word.
 *
 * <p>A policy either answers (KITE = step back, STAND = hold and shoot)
 * or defers (DEFER = this policy has no opinion, fall through to the
 * shared margin envelope in {@link Kite#shouldKite}). Every unit type
 * speaks the same three verdicts, so a future Terran gun plugs into
 * the ladder without touching the existing policies.
 */
public enum KiteVerdict {
    KITE,
    STAND,
    DEFER
}
