package atlantis.production.dynamic.reinforce;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.map.position.HasPosition;
import atlantis.information.generic.AllOfOurBasePositions;

/**
 * @Deprecated
 */
public abstract class ReinforceBasesWithCombatBuildings extends Commander {
    public ReinforceBasesWithCombatBuildings() {
    }

    public static ReinforceBasesWithCombatBuildings get() {
        return null;
//        if (We.protoss()) return new ProtossReinforceBaseWithCannons();
//        if (We.terran()) return new TerranReinforceBasesWithCombatBuildings();
//
//        return new ReinforceBasesWithCombatBuildings() {
//            @Override
//            protected void makeSureIsReinforced(HasPosition basePosition) {
//                System.err.println("ReinforceBasesWithCombatBuildings not implemented for " + We.race());
//            }
//        };
    }

    @Override
    protected void handle() {
//        if (A.everyNthGameFrame(77)) {
//            (new TerranBunkersInMain()).handle();
//        }

//        if (We.terran()) {
//            (new TurretsForMain()).buildIfNeeded();
//            (new TurretsForNonMain()).buildIfNeeded();
//        }
//
//        reinforceAllBasesIncludingUnfinishedOnes();
    }

    private void reinforceAllBasesIncludingUnfinishedOnes() {
        if (A.everyFrameExceptNthFrame(79)) return;

        for (HasPosition position : AllOfOurBasePositions.allBases(true, true)) {
            if (position != null) makeSureIsReinforced(position);
        }
    }

    protected abstract void makeSureIsReinforced(HasPosition basePosition);
}
