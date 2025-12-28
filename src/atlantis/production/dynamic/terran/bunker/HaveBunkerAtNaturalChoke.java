package atlantis.production.dynamic.terran.bunker;

import atlantis.game.A;
import atlantis.map.base.BaseLocations;
import atlantis.map.choke.Chokes;
import atlantis.map.position.HasPosition;
import atlantis.units.select.Count;
import atlantis.units.select.Have;

public class HaveBunkerAtNaturalChoke extends HaveBunkerAt {
    @Override
    public boolean applies() {
        if (Count.basesWithUnfinished() <= 1) return false;
        if (Count.bunkersWithUnfinished() >= 3) return false;
        if (bunkerExistsAtPosition()) return false;

        return Count.marines() >= 2 || A.hasMinerals(160);
    }

    @Override
    protected HasPosition atPosition() {
        return Chokes.natural().translateTilesTowards(4, BaseLocations.natural());
    }
}
