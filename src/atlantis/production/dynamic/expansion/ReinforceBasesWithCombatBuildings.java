package atlantis.production.dynamic.expansion;

import atlantis.architecture.Commander;
import atlantis.combat.micro.terran.TerranBunkersInMain;
import atlantis.game.A;
import atlantis.map.position.HasPosition;
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
                (new TerranMissileTurretsForMain()).buildIfNeeded();
            }
            if (A.everyNthGameFrame(73)) {
                (new TerranMissileTurretsForNonMain()).buildIfNeeded();
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
