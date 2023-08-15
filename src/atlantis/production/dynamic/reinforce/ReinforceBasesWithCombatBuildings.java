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

        return null;
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
            if (A.everyNthGameFrame(71)) {
                (new TurretsForMain()).buildIfNeeded();
            }
            if (A.everyNthGameFrame(73)) {
                (new TurretsForNonMain()).buildIfNeeded();
            }
        }

        reinforceAllBasesIncludingUnfinishedOnes();
    }

    private void reinforceAllBasesIncludingUnfinishedOnes() {
        for (HasPosition position : AllBasePositions.allBases(true, true)) {
            if (position != null) makeSureIsReinforced(position);
        }
    }

    protected abstract void makeSureIsReinforced(HasPosition basePosition);
}
