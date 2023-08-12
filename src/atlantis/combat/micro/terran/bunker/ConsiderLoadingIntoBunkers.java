package atlantis.combat.micro.terran.bunker;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.select.Count;

public class ConsiderLoadingIntoBunkers extends Manager {
    public ConsiderLoadingIntoBunkers(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.isLoaded()) return false;
        if (Count.bunkers() == 0) return false;
        if (!unit.isMarine() && !unit.isGhost()) return false;
        if (unit.isMissionDefend() && unit.hasCooldown()) return true;

        return true;
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            LoadIntoTheBunker.class,
        };
    }
}
