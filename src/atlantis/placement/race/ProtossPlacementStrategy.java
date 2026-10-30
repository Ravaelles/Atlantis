package atlantis.placement.race;

import atlantis.placement.blocks.BlockTemplates;
import atlantis.placement.core.BuildBlock;
import atlantis.placement.core.PsiGating;
import atlantis.placement.core.RacePlacementStrategy;
import atlantis.placement.core.SupportRequest;
import atlantis.production.constructions.position.conditions.can_build_here.IsWorkerCovered;
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

        return psiGating.framesUntilPowered(tx, ty);
    }

    /**
     * Only a building that needs Psi waits. Everything else (a Nexus, a Pylon
     * itself) can start on any free tile.
     */
    @Override
    public boolean requiresAvailability(String buildableTypeId) {
        AUnitType type = AUnitType.getByName(buildableTypeId);

        return type != null && type.needsPower();
    }

    @Override
    public AvailabilityVerdict availabilityVerdict(int tx, int ty, String buildableTypeId) {
        // Standing workers cover the tile first: the builder assigned to it
        // would be refused and the site would die builderless (NEXT #145 -
        // the catalogue A/B storm). Checked before power so an unpowered
        // covered tile refuses outright instead of ordering a support Pylon
        // onto ground no builder can walk to. Same numbers as the builder
        // refusal, read not owned.
        if (IsWorkerCovered.check(atlantis.map.position.APosition.create(tx, ty))) {
            return AvailabilityVerdict.REFUSE;
        }

        if (psiGating == null) return AvailabilityVerdict.AVAILABLE;

        PsiGating.PowerVerdict verdict = psiGating.verdictFor(tx, ty);
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
     */
    @Override
    public SupportRequest requestAvailabilitySupport(int tx, int ty, String buildableTypeId) {
        return new SupportRequest(AUnitType.Protoss_Pylon, tx, ty);
    }
}
