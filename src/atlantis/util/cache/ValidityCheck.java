package atlantis.util.cache;

/**
 * Implemented by values whose cached copies can go stale while their TTL is
 * still running (e.g. a unit that died, a focus point that expired).
 * Lets {@link Cache} validate entries without knowing their types.
 */
public interface ValidityCheck {

    boolean isValid();
}
