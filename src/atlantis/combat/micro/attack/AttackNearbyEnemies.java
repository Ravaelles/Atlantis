package atlantis.combat.micro.attack;

import atlantis.architecture.Manager;
import atlantis.combat.targeting.ATargeting;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.util.cache.Cache;

public class AttackNearbyEnemies extends Manager {
    public static final double MAX_DIST_TO_ATTACK = 25;
    private static Cache<AUnit> cache = new Cache<>();
    private static Cache<Object> cacheObject = new Cache<>();
    public static String reasonNotToAttack;
    private static ProcessAttackUnit processAttackUnit;
    private final AllowedToAttack allowedToAttack;

    // =========================================================

    public AttackNearbyEnemies(AUnit unit) {
        super(unit);
        processAttackUnit = (new ProcessAttackUnit(unit));
        allowedToAttack = (new AllowedToAttack(this, unit));
    }

    // =========================================================

    @Override
    public boolean applies() {
        return unit.hasAnyWeapon();
    }

    protected Manager handle() {
        if (this.equals(unit.manager()) && justHandledRecently()) {
            return usedManager(this);
        }

        if (handleAttackNearEnemyUnits()) {
            return usedManager(this);
        }

        return null;
    }

    private boolean justHandledRecently() {
        return unit.lastActionLessThanAgo(8, Actions.ATTACK_UNIT)
            || unit.lastActionLessThanAgo(8, Actions.MOVE_ATTACK);
    }

    /**
     * Selects the best enemy unit and issues attack order.
     *
     * @return <b>true</b> if unit has found valid target and is currently busy with either starting
     * an attack or just attacking the enemy<br />
     * <b>false</b> if no valid enemy to attack could be found
     */
    public boolean handleAttackNearEnemyUnits() {
//        return (boolean) cacheObject.getIfValid(
//            "handleAttackNearEnemyUnits: " + unit.id(),
//            4,
//            () -> {

        if (unit.isAttacking() && (
            unit.lastActionLessThanAgo(5, Actions.ATTACK_UNIT)
                || unit.lastActionLessThanAgo(5, Actions.MOVE_ATTACK)
        )) {
            if (unit.target() != null && unit.hasPosition() && !unit.looksIdle()) {
                if (unit.mission().allowsToAttackEnemyUnit(unit, unit.target())) {
                    return true;
                }
            }
        }

        if (!allowedToAttack.canAttackNow()) {
//                    ErrorLog.printMaxOncePerMinute("Not allowed to attack now (" + unit + ")");
            return false;
        }

        // =========================================================

        AUnit enemy = (new AttackNearbyEnemies(unit)).defineEnemyToAttackFor();

        if (enemy == null || !unit.canAttackTarget(enemy)) {
            return false;
        }

        // =========================================================

        if (unit.mission().allowsToAttackEnemyUnit(unit, enemy)) {
            return processAttackUnit.processAttackOtherUnit(enemy);
        }
        else {
            return false;
        }

//            }
//        );
    }

    public String canAttackEnemiesNowString() {
        return allowedToAttack.canAttackEnemiesNowString();
    }

    // =========================================================

    protected AUnit defineEnemyToAttackFor() {
        return cache.getIfValid(
            "defineEnemyToAttackFor",
            2,
            () -> {
                reasonNotToAttack = null;

                AUnit enemy = bestTargetToAttack();
//                System.out.println("enemy = " + enemy);

                if (enemy == null) {
                    return null;
                }
                if (!allowedToAttack.isValidTargetAndAllowedToAttackUnit(enemy)) {
//                    System.out.println("Not allowed to attack: " + enemy + " (" + reasonNotToAttack + ")");
                    return null;
                }

                return enemy;
            }
        );
    }

    protected AUnit bestTargetToAttack() {
        return ATargeting.defineBestEnemyToAttackFor(unit, MAX_DIST_TO_ATTACK);
    }
}
