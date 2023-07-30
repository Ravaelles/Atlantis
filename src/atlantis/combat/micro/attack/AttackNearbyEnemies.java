package atlantis.combat.micro.attack;

import atlantis.architecture.Manager;
import atlantis.combat.targeting.ATargeting;
import atlantis.units.AUnit;
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
                AttackNearbyEnemies instance = getInstance(unit);

                if (!instance.canAttackNow()) return false;

                AUnit enemy = instance.defineEnemyToAttackFor();
                if (enemy == null) return false;

                return processAttackUnit.processAttackOtherUnit(enemy);
            }
        );
    }

    protected AttackNearbyEnemies getInstance(AUnit unit) {
        return new AttackNearbyEnemies(unit);
    }

    private boolean canAttackNow() {
        return allowedToAttack.canAttackNow();
    }

    public boolean canAttackEnemiesNow() {

        return allowedToAttack.canAttackEnemiesNow();
    }

    public String canAttackEnemiesNowString() {
        return allowedToAttack.canAttackEnemiesNowString();
    }

    // =========================================================

    protected boolean allowedToAttack() {

        // @Problematic - Vultures dont attack from far
//        if (Count.ourCombatUnits() >= 5 && unit.outsideSquadRadius()) {
//            reasonNotToAttack = "Outside";
//            return false;
//        }

        return allowedToAttack.allowedToAttack();
    }

    protected AUnit defineEnemyToAttackFor() {
        return cache.getIfValid(
            "defineEnemyToAttackFor",
            2,
            () -> {
                reasonNotToAttack = null;

//                if (!allowedToAttack()) {
//                    return null;
//                }

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

//    public  boolean shouldNotAttack() {
////        if (AAvoidUnits.shouldAvoidAnyUnit()) {
////            return false;
////        }
//
//        return unit.isUnitUnableToDoAnyDamage()
//
//                // =========================================================
//                // =========================================================
//                // THESE SHOULDNT BE USED! RESPECT DECISIONS OF TOP SUPERIOR MANAGER!!!
////                || unit.lastActionLessThanAgo(4, UnitActions.ATTACK_UNIT)
////                || (unit.isTankUnsieged() && (!unit.isMoving() && unit.woundPercent() > 15));
//        ;
//    }
}
