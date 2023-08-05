package atlantis.combat.micro.attack;

import atlantis.architecture.Manager;
import atlantis.combat.targeting.ATargeting;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.util.cache.Cache;
import atlantis.util.log.ErrorLog;

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

    public Manager handle() {
        if (handleAttackNearEnemyUnits()) {
            return usedManager(this);
        }

        return null;
    }

    /**
     * Selects the best enemy unit and issues attack order.
     *
     * @return <b>true</b> if unit has found valid target and is currently busy with either starting
     * an attack or just attacking the enemy<br />
     * <b>false</b> if no valid enemy to attack could be found
     */
    public boolean handleAttackNearEnemyUnits() {
        return (boolean) cacheObject.getIfValid(
            "handleAttackNearEnemyUnits: " + unit.id(),
            4,
            () -> {
                if (unit.isAttacking() && (
                    unit.lastActionLessThanAgo(5, Actions.ATTACK_UNIT)
                        || unit.lastActionLessThanAgo(5, Actions.MOVE_ATTACK)
                )) {
                    return true;
                }

                AttackNearbyEnemies instance = getInstance(unit);

                if (!allowedToAttack.canAttackNow()) {
//                    ErrorLog.printMaxOncePerMinute("Not allowed to attack now (" + unit + ")");
                    return false;
                }

                AUnit enemy = instance.defineEnemyToAttackFor();

                if (enemy == null) {
                    return false;
                }

                return processAttackUnit.processAttackOtherUnit(enemy);
            }
        );
    }

    protected AttackNearbyEnemies getInstance(AUnit unit) {
        return new AttackNearbyEnemies(unit);
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
