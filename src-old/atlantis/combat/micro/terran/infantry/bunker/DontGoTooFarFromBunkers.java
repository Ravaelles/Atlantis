package atlantis.combat.micro.terran.infantry.bunker;

import atlantis.architecture.Manager;
import atlantis.information.enemy.EnemyUnitBreachedBase;
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
        if (EnemyUnitBreachedBase.get() != null) return false;

        if (unit.isMissionDefend() && GamePhase.isEarlyGame() && Count.bunkers() > 0) {
            if (unit.meleeEnemiesNearCount(3.3) >= 1) return false;

            return true;
        }

        return false;
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

        if (bunker == null) return false;

        if (unit.distTo(bunker) >= 22 && unit.lastUnderAttackLessThanAgo(30 * 15)) {
            if (!unit.lastActionLessThanAgo(3)) {
                unit.move(bunker, Actions.MOVE_SAFETY, "CloserToBunker");
            }
            return true;
        }

        return false;
    }
}
