package atlantis.combat.micro.terran.bunker;

import atlantis.architecture.Manager;
import atlantis.information.strategy.GamePhase;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;
import atlantis.units.select.Select;

public class DontGoTooFarFromBunkers extends Manager {

    public DontGoTooFarFromBunkers(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return GamePhase.isEarlyGame() && unit.isMissionDefend() && Count.bunkers() > 0;
    }

    @Override
    protected Manager handle() {
        if (shouldNotLeaveBunker()) {
            return usedManager(this);
        }

        return handleSubmanagers();
    }

    public boolean shouldNotLeaveBunker() {
        AUnit bunker = Select.our().bunkers().nearestTo(unit);

        if (bunker == null) {
            return false;
        }


        if (unit.distTo(bunker) >= 15) {
            if (!unit.lastActionLessThanAgo(3)) {
                unit.move(bunker, Actions.MOVE_SAFETY, "CloserToBunker");
            }
            return true;
        }

        return false;
    }
}
