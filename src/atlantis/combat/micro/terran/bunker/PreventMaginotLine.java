package atlantis.combat.micro.terran.bunker;

import atlantis.information.strategy.GamePhase;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.architecture.Manager;

public class PreventMaginotLine extends Manager {

    public PreventMaginotLine(AUnit unit) {
        super(unit);
    }

    @Override
    public Manager handle() {
        if (preventFromActingLikeFrenchOnMaginotLine()) return usedManager(this);

        return null;
    }

    private boolean preventFromActingLikeFrenchOnMaginotLine() {
        if (
            unit.hpLessThan(21)
                && unit.enemiesNear().inRadius(4.9 + (unit.idIsOdd() ? 2 : 0), unit).ranged().notEmpty()
        ) {
            return false;
        }

        if (unit.enemiesNear().inRadius(3.5, unit).notEmpty()) {
            return false;
        }

        int dragoons = unit.enemiesNear().ofType(AUnitType.Protoss_Dragoon).inRadius(7, unit).count();
        if (dragoons > 0) {
            if (GamePhase.isEarlyGame() && unit.friendsInRadiusCount(5) <= 4 * dragoons) {
                return false;
            }
        }

        return true;

//        if (unloadFromBunkers.preventEnemiesFromAttackingNearBuildingsWithoutConsequences()) {
//            return true;
//        }
//
//        if (unloadFromBunkers.preventEnemiesFromAttackingWorkersWithoutConsequences()) {
//            return true;
//        }
//
//        return false;
    }
}