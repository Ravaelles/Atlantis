package atlantis.combat.micro.terran.bunker;

import atlantis.architecture.Manager;
import atlantis.information.strategy.GamePhase;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;

public class PreventMaginotLine extends Manager {
    public PreventMaginotLine(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.lastActionLessThanAgo(30 * 3, Actions.LOAD)) {
            return false;
        }

        if (
            unit.hpLessThan(21)
                && unit.enemiesNear().inRadius(4.9 + (unit.idIsOdd() ? 2 : 0), unit).ranged().notEmpty()
        ) {
            return false;
        }

        if (unit.enemiesNear().inRadius(2.4 + unit.id() % 3, unit).notEmpty()) {
            return false;
        }

        int dragoons = unit.enemiesNear().ofType(AUnitType.Protoss_Dragoon).inRadius(7, unit).count();
        if (dragoons > 0) {
            if (GamePhase.isEarlyGame() && unit.friendsInRadiusCount(5) <= 4 * dragoons) {
                return false;
            }
        }

//        if (unit.isMissionDefend()) {
//            AUnit main = Select.main();
//            if (main != null && Select.enemyCombatUnits().inRadius(4, main).notEmpty()) {
//                return true;
//            }
//        }

        return true;
    }

    @Override
    public Manager handle() {
        if (preventFromActingLikeFrenchOnMaginotLine()) return usedManager(this);

        return null;
    }

    private boolean preventFromActingLikeFrenchOnMaginotLine() {
        // Loaded
        if (unit.isLoaded()) {
            unit.loadedInto().unloadAll();
            return true;
        }

        // Not loaded
//        else {
//            if (unit.lastStartedAttackMoreThanAgo(30 * 5) && !unit.looksIdle()) {
//                return true;
//            }
//        }

        return false;
    }
}
