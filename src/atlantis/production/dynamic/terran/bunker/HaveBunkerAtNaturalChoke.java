package atlantis.production.dynamic.terran.bunker;

import atlantis.architecture.Commander;
import atlantis.map.base.BaseLocations;
import atlantis.map.choke.Chokes;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnitType;
import atlantis.units.select.Count;
import atlantis.util.We;

public class HaveBunkerAtNaturalChoke extends HaveBunkerAt {
    @Override
    public boolean applies() {
        if (Count.bunkersWithUnfinished() >= 3) return false;
        if (haveBunkerHere()) return false;

        return true;
    }

    @Override
    protected HasPosition atPosition() {
        return Chokes.natural().translateTilesTowards(4, BaseLocations.natural());
    }
}
