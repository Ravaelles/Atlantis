package atlantis.combat.micro.terran.bunker;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;

public class ContinueLoadingIntoBunker extends Manager {
    public ContinueLoadingIntoBunker(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        AUnit target = unit.target();

        return target != null && target.isBunker();
    }

    @Override
    public Manager handle() {
        AUnit target = unit.target();

        if (target != null && target.isBunker()) {
            if (unit.isMoving()) {
                unit.load(target);
                return usedManager(this);
            }
        }

        return null;
    }
}
