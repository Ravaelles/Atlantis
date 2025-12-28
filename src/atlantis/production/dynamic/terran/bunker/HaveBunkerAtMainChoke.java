package atlantis.production.dynamic.terran.bunker;

import atlantis.map.choke.Chokes;
import atlantis.map.position.HasPosition;
import atlantis.units.select.Count;
import atlantis.units.select.Select;

public class HaveBunkerAtMainChoke extends HaveBunkerAt {
    @Override
    public boolean applies() {
        if (Count.bunkersWithUnfinished() >= 2) return false;
        if (haveBunkerHere()) return false;

        return true;
    }

    @Override
    protected HasPosition atPosition() {
        return Chokes.mainChoke().translateTilesTowards(4, Select.mainOrAnyBuilding());
    }
}