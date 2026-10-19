package atlantis.production.dynamic.supply;

/**
 * Answers the single question every dynamic supply path asks first -
 * "is another supply provider actually needed right now" - so all of
 * them abort before any band math, foresight or queue scanning when
 * the answer is no. Race-specific answers live next to this contract
 * ({@code ProtossNeedDynamicSupply}); races without one keep legacy
 * behavior (fail open, never silently stricter).
 */
public interface NeedDynamicSupply {

    /**
     * @param supplyUsed       units of supply in use right now.
     * @param supplyTotal      total supply right now.
     * @param unfinishedProviders unfinished supply buildings (warping
     *                            or not started): one in flight Already
     *                            covers the block it was ordered for.
     */
    boolean needsSupply(int supplyUsed, int supplyTotal, int unfinishedProviders);

    /** Dispatch by race; non-Protoss stays legacy (D8: Protoss-only). */
    static NeedDynamicSupply forCurrentRace() {
        if (atlantis.util.We.protoss()) return new ProtossNeedDynamicSupply();
        return new PermissiveNeedDynamicSupply();
    }

    /** Fail-open for races without a tuned rule: legacy paths decide. */
    final class PermissiveNeedDynamicSupply implements NeedDynamicSupply {
        @Override
        public boolean needsSupply(int supplyUsed, int supplyTotal, int unfinishedProviders) {
            return true;
        }
    }
}
