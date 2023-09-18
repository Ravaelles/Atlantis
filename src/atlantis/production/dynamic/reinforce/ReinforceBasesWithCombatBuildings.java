package atlantis.production.dynamic.reinforce;

import atlantis.architecture.Commander;
import atlantis.game.A;
import atlantis.map.position.HasPosition;
import atlantis.production.dynamic.expansion.AllBasePositions;
import atlantis.production.dynamic.reinforce.terran.TerranReinforceBasesWithCombatBuildings;
import atlantis.production.dynamic.reinforce.terran.turrets.TurretsForMain;
import atlantis.production.dynamic.reinforce.terran.turrets.TurretsForNonMain;
import atlantis.util.We;

public abstract class ReinforceBasesWithCombatBuildings extends Commander {
    public ReinforceBasesWithCombatBuildings() {
    }

    public static ReinforceBasesWithCombatBuildings get() {
        if (We.terran()) return new TerranReinforceBasesWithCombatBuildings();

        return new ReinforceBasesWithCombatBuildings() {
            @Override
            protected void makeSureIsReinforced(HasPosition basePosition) {
                System.err.println("ReinforceBasesWithCombatBuildings not implemented for " + We.race());
            }
        };
    }

    @Override
    public boolean applies() {
        return true;
    }

    @Override
    protected void handle() {
        if (!applies()) return;

//        if (A.everyNthGameFrame(77)) {
//            (new TerranBunkersInMain()).handle();
//        }

        if (We.terran()) {
            (new TurretsForMain()).buildIfNeeded();
            (new TurretsForNonMain()).buildIfNeeded();
        }

        reinforceAllBasesIncludingUnfinishedOnes();
    }

    private void reinforceAllBasesIncludingUnfinishedOnes() {
        if (A.everyFrameExceptNthFrame(79)) return;

        for (HasPosition position : AllBasePositions.allBases(true, true)) {
            if (position != null) makeSureIsReinforced(position);
        }
    }

    protected abstract void makeSureIsReinforced(HasPosition basePosition);
}
