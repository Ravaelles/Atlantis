package atlantis.production.dynamic.supply;

/**
 * Protoss answer to "is another Pylon needed right now" (owner: never
 * more than 8 free supply below 40 used - measured 10+ free from
 * overlapping BO + dynamic Pylons; one Pylon at a time is what keeps
 * the headroom tight).
 *
 * <p>Two vetoes, cheapest first: an unfinished Pylon already covers the
 * block it was ordered for (reordering behind it only stockpiles), and
 * free supply above the band means the cap is not near. Bands by used
 * supply (owner): free &le; 6 at 60 or below, &le; 10 at 100 or below,
 * &le; 14 above that.</p>
 */
public final class ProtossNeedDynamicSupply implements NeedDynamicSupply {

    @Override
    public boolean needsSupply(int supplyUsed, int supplyTotal, int unfinishedProviders) {
        // One at a time below 40 used (owner): an unfinished Pylon already
        // covers the block, ordering another only stockpiles free supply.
        // At 40+ the pipeline pair stays - legacy in-flight logic decides.
        if (supplyUsed < 40 && unfinishedProviders > 0) return false;

        int free = supplyTotal - supplyUsed;
        if (supplyUsed <= 60) return free <= 6;
        if (supplyUsed <= 100) return free <= 10;

        return free <= 14;
    }
}
