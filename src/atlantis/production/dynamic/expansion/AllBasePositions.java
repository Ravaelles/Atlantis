package atlantis.production.dynamic.expansion;

import atlantis.config.AtlantisConfig;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.production.constructing.Construction;
import atlantis.production.constructing.ConstructionRequests;
import atlantis.units.select.Select;

import java.util.ArrayList;

public class AllBasePositions {
    public static ArrayList<HasPosition> allBases(boolean includeNotStarted, boolean includeNotFinished) {
        ArrayList<HasPosition> positions = new ArrayList<>();

        if (includeNotStarted) {
            HasPosition nextBase = PendingNextBase.basePositionOrNull();
            positions.add(nextBase);
        }

        if (includeNotFinished) {
            for (Construction construction : ConstructionRequests.notStartedOfType(AtlantisConfig.BASE)) {
                APosition position = construction.buildPosition();
                positions.add(position);
            }
        }

        positions.addAll(Select.ourBases().list());

        return positions;
    }
}
