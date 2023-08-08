package atlantis.production.dynamic.expansion;

import atlantis.architecture.Commander;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;

import java.util.ArrayList;

public abstract class ReinforceBaseWithCombatBuildings extends Commander {
    public ReinforceBaseWithCombatBuildings() {
    }

//    public static ReinforceBaseWithCombatBuildings get() {
//        if (We.terran()) return new TerranReinforceBaseWithCombatBuildings();
//
//        return null;
//    }

    public ArrayList<HasPosition> basesToReinforce() {
        HasPosition nextBase = PendingNextBase.basePositionOrNull();

        return;
    }

    @Override
    public void handle() {
        APosition basePosition = PendingNextBase.basePositionOrNull();

        if (basePosition != null) makeSureIsReinforced(basePosition);
    }

    protected abstract void makeSureIsReinforced(APosition basePosition);
}
