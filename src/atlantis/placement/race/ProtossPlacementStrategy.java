package atlantis.placement.race;

import atlantis.placement.blocks.BlockTemplates;
import atlantis.placement.core.BuildBlock;
import atlantis.placement.core.PsiGating;
import atlantis.placement.core.RacePlacementStrategy;

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

    /** Test seam: a null gate answers "available everywhere" (no Psi check). */
    public ProtossPlacementStrategy(PsiGating psiGating) {
        this.psiGating = psiGating;
    }

    /** The gate itself, for a caller that needs the pull-forward verdict. */
    public PsiGating gating() {
        return psiGating;
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
        atlantis.units.AUnitType type = atlantis.units.AUnitType.getByName(buildableTypeId);
        return type != null && type.needsPower();
    }
}
