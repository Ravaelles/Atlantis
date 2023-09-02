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
        if (unit.hp() <= 20) return false;
        if (shouldStayLoaded()) return false;
        if (unit.enemiesNear().inRadius(2.7 + unit.id() % 3, unit).notEmpty()) return false;
        if (unit.lastActionLessThanAgo(30 * 4, Actions.LOAD)) return false;
        if (tooManyDragoonsNearby()) return false;

        if (shouldEngage()) return true;

        if (EnemyWhoBreachedBase.get() == null) return false;
        if (unit.enemiesNear().inRadius(5.99, unit).notEmpty()) return false;

//        if (
//            unit.hpMoreThan(21)
//                && unit.enemiesNear().inRadius(4.2 + (unit.id() % 5 == 1 ? 2 : 0), unit).ranged().notEmpty()
//        ) return true;

//        if (unit.isMissionDefend()) {
//            AUnit main = Select.main();
//            if (main != null && Select.enemyCombatUnits().inRadius(4, main).notEmpty()) {
//                return true;
//            }
//        }

        return true;
    }

    private boolean shouldEngage() {
        return unit.isHealthy()
            && unit.hasNotShotInAWhile()
            && !unit.isRunning()
            && unit.lastUnderAttackMoreThanAgo(30 * 7)
            && unit.enemiesNear().effVisible().inRadius(8, unit).atMost(2);
    }

    private boolean shouldStayLoaded() {
        if (!unit.isLoaded()) return false;

        AUnit enemy = unit.enemiesNear().nearestTo(unit);

        return enemy == null || !enemy.regionsMatch(unit);
    }

    private boolean tooManyDragoonsNearby() {
        int dragoons = unit.enemiesNear().ofType(AUnitType.Protoss_Dragoon).inRadius(7, unit).count();
        if (dragoons > 0) {
            if (GamePhase.isEarlyGame() && unit.friendsInRadiusCount(5) <= 4 * dragoons) {
                return true;
            }
        }
        return false;
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
