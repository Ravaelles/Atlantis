package atlantis.combat.micro.terran.bunker;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;

public class MoveToBunkerWhenCantLoadIntoIt extends Manager {
    public MoveToBunkerWhenCantLoadIntoIt(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.isHealthy() && unit.noCooldown()) return false;

        if (Count.bunkers() == 0) return false;

        return unit.target() != null && unit.target().isBunker() && unit.isMarine();
    }

    @Override
    public Manager handle() {
        AUnit bunker = unit.target();

        if (bunker == null) {
            return null;
        }

        double unitDistToBunker = bunker.distTo(unit);

        if (unitDistToBunker <= 4.9) {
            return null;
        }

        if (unit.enemiesNearInRadius(2.5) > 0) {
            return null;
        }

        if (unit.isMissionDefend() || unit.enemiesNearInRadius(3.2) > 0) {
            unit.move(bunker, Actions.MOVE_FORMATION, "ClosaToBunka");
            return usedManager(this);
        }

        return null;
    }
}
