package atlantis.placement.race;

import atlantis.placement.blocks.BlockTemplates;
import atlantis.placement.core.BuildBlock;
import atlantis.placement.core.PsiGating;
import atlantis.placement.core.RacePlacementStrategy;
import atlantis.placement.core.SupportRequest;
import atlantis.units.AUnitType;

import java.util.ArrayList;
import java.util.List;

/**
 * Protoss placement: Psi power gates the tiles, and the block set is the full
 * Stardust table (`_AI/redesign/03_PLACEMENT.md` §4.1, §5.4 S6).
 *
 * <p>
 * This is the only fully implemented strategy - Protoss is the race the bot
 * plays
 * today, so it is where the placement knowledge lives.
 * </p>
 */
public final class ProtossPlacementStrategy implements RacePlacementStrategy {

    private final PsiGating psiGating;

    /**
     * Test seam: a null gate answers "available everywhere" (no Psi check).
     */
    public ProtossPlacementStrategy(PsiGating psiGating) {
        this.psiGating = psiGating;
    }

    @Override
    public List<BuildBlock.Spec> blockTemplates() {
        List<BuildBlock.Spec> specs = new ArrayList<>();
        specs.addAll(BlockTemplates.startBlocks()); // start blocks first: the anchor
        specs.addAll(BlockTemplates.normal());

        return specs;
    }

    @Override
    public int framesUntilAvailable(int tx, int ty, String buildableTypeId) {
        if (psiGating == null)
            return 0;

        AUnitType type = powerNeedingType(buildableTypeId);

        return type == null
            ? psiGating.framesUntilPowered(tx, ty)
            : psiGating.framesUntilPowered(tx, ty, type.getTilesWidth(), type.getTilesHeights());
    }

    /**
     * The footprint source of truth, in one place: only power-needing
     * buildings read the footprint center, everything else answers per
     * tile. Verdict and wait shift through it, so the two can never
     * disagree on which buildings the rule owns.
     */
    private static AUnitType powerNeedingType(String buildableTypeId) {
        AUnitType type = AUnitType.getByName(buildableTypeId);

        return type != null && type.needsPower() ? type : null;
    }

    /**
     * Only a building that needs Psi waits. Everything else (a Nexus, a Pylon
     * itself) can start on any free tile.
     */
    @Override
    public boolean requiresAvailability(String buildableTypeId) {
        return powerNeedingType(buildableTypeId) != null;
    }

    @Override
    public AvailabilityVerdict availabilityVerdict(int tx, int ty, String buildableTypeId) {
        if (psiGating == null) return AvailabilityVerdict.AVAILABLE;

        AUnitType type = powerNeedingType(buildableTypeId);
        PsiGating.PowerVerdict verdict = type == null
            ? psiGating.verdictFor(tx, ty)
            : psiGating.verdictFor(tx, ty, type.getTilesWidth(), type.getTilesHeights());
        switch (verdict) {
            case NEEDS_NEW_PYLON:
                return AvailabilityVerdict.NEEDS_SUPPORT;

            case REFUSE:
                return AvailabilityVerdict.REFUSE;

            case ACCEPT:
            default:
                return AvailabilityVerdict.AVAILABLE;
        }
    }

    /**
     * Protoss support is a Pylon: the one building that makes a good-but-unpowered
     * spot usable. Answered as data - the caller forwards it to production,
     * so the production engine places and builds it like any other goal.
     * The demander travels with the request (owner 2026-11-08: only a
     * Cannon demands support; the planner gates the rest).
     */
    @Override
    public SupportRequest requestAvailabilitySupport(int tx, int ty, String buildableTypeId) {
        return new SupportRequest(AUnitType.Protoss_Pylon, buildableTypeId, tx, ty);
    }
}
