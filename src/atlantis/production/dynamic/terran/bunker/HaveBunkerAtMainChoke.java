package atlantis.production.dynamic.terran.bunker;

import atlantis.game.A;
import atlantis.map.choke.Chokes;
import atlantis.map.position.HasPosition;
import atlantis.units.select.Count;
import atlantis.units.select.Have;
import atlantis.units.select.Select;

public class HaveBunkerAtMainChoke extends HaveBunkerAt {
    @Override
    public boolean applies() {
        if (Count.bunkersWithUnfinished() >= 2) return false;
        if (bunkerExistsAtPosition()) return false;

        return Have.barracks() && (Count.marines() >= 1 || A.hasMinerals(160));
    }

    @Override
    protected HasPosition atPosition() {
        return Chokes.mainChoke().translateTilesTowards(4, Select.mainOrAnyBuilding());
    }
}