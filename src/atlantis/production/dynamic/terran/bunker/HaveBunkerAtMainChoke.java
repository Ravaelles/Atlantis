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
        // atPosition() needs a main choke, and Chokes.mainChoke() is null until the
        // map analysis has run (it also stays null on maps without one). Being
        // asked "do we want a bunker at the main choke" and having no main choke is
        // not a reason to decide yes - and dereferencing null here took down whole
        // frames of the main loop.
        if (Chokes.mainChoke() == null) return false;
        if (Count.bunkersWithUnfinished() >= 2) return false;
        if (bunkerExistsAtPosition()) return false;

        return Have.barracks() && (Count.marines() >= 1 || A.hasMinerals(160));
    }

    @Override
    protected HasPosition atPosition() {
        return Chokes.mainChoke().translateTilesTowards(4, Select.mainOrAnyBuilding());
    }
}