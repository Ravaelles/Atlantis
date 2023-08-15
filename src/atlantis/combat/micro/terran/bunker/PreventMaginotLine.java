package atlantis.combat.micro.terran.bunker;

import atlantis.architecture.Manager;
import atlantis.combat.micro.attack.AttackNearbyEnemies;
import atlantis.information.enemy.EnemyWhoBreachedBase;
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
        if (unit.enemiesNear().inRadius(5.99, unit).notEmpty()) return false;

        if (unit.hp() <= 20) return false;

        if (unit.lastActionLessThanAgo(30 * 4, Actions.LOAD)) return false;

        if (unit.enemiesNear().inRadius(3.2 + unit.id() % 2, unit).notEmpty()) return false;

        if (EnemyWhoBreachedBase.get() == null) return false;

        if (
            unit.hpMoreThan(21)
                && unit.enemiesNear().inRadius(4.2 + (unit.id() % 5 == 1 ? 2 : 0), unit).ranged().notEmpty()
        ) return true;

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
    protected Manager handle() {
        return preventFromActingLikeFrenchOnMaginotLine();
    }

    private Manager preventFromActingLikeFrenchOnMaginotLine() {
        // Loaded
        if (unit.isLoaded()) {
            unit.loadedInto().unloadAll();
            return usedManager(this);
        }

        if (!unit.isAttacking() && !unit.isMoving()) {
            (new AttackNearbyEnemies(unit)).invoke();
            return usedManager(this);
        }

        return null;
    }
}
