package atlantis.combat.micro.avoid;

import atlantis.architecture.Manager;
import atlantis.combat.micro.attack.AttackNearbyEnemies;
import atlantis.combat.micro.avoid.zerg.ShouldAlwaysAvoidAsZerg;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Units;
import atlantis.util.Enemy;

public class WantsToAvoid extends Manager {
    private Avoid avoid;
    private ShouldAlwaysAvoidAsZerg shouldAlwaysAvoidAsZerg;

    public WantsToAvoid(AUnit unit) {
        super(unit);
        avoid = new Avoid(unit);
        shouldAlwaysAvoidAsZerg = new ShouldAlwaysAvoidAsZerg(unit);
    }

    @Override
    public boolean applies() {
        return true;
    }

    public Manager unitOrUnits(Units enemies) {
        if (enemies.isEmpty()) {
            return null;
        }

        if (shouldNeverAvoidIf(enemies)) {
            return null;
        }

        // =========================================================

        if (!attackInsteadAvoid(enemies)) {
//                APainter.paintCircle(10, Color.Green);
//                APainter.paintCircle(11, Color.Green);

//            unit.addFileLog("NOT shouldAlwaysAvoid");
//            unit.addLog("NOT shouldAlwaysAvoid");
            if ((new AttackNearbyEnemies(unit)).handleAttackNearEnemyUnits()) {
                return usedManager(this, "ButAttack");
            }
        }

        // =========================================================

//        if (unit.isDragoon()) {
//            A.printStackTrace();
//        }

        return avoid.singleUnit(enemies.first());

//        if (enemies.size() == 1) {
//            return Avoid.singleUnit(enemies.first());
//        }
//        else {
//            return Avoid.groupOfUnits(enemies);
//        }
    }

    // =========================================================

    private boolean attackInsteadAvoid(Units enemies) {
        if (!unit.hasAnyWeapon()) {
            return false;
        }

        if (shouldAlwaysAvoidAsZerg.shouldAlwaysAvoid()) {
            return true;
        }

//        if (unit.isWorker() || unit.isScout()) {
//            unit.addLog("AlwaysAvoid");
//            return true;
//        }
//
//        if (
//                unit.isMarine() && !A.isUms() && GamePhase.isEarlyGame() && unit.isRetreating()
//                && (unit.hp() >= 24 && unit.cooldownRemaining() >= 1)
//        ) {
//            unit.addLog("DearGod");
//            return true;
//        }
//
//        if (unit.isSquadScout() && unit.isWounded() && unit.friendsNear().inRadius(3, unit).isEmpty()) {
//            unit.addLog("SquadScoutAvoid");
//            return true;
//        }


        if (
            unit.isInfantry()
                && unit.enemiesNear().ofType(AUnitType.Zerg_Lurker).inRadius(7.2, unit).effUndetected().notEmpty()
        ) {
            unit.setTooltip("Aaa-LURKER!");
            return true;
        }

        if (
            unit.isInfantry()
                && unit.enemiesNear().ofType(AUnitType.Protoss_Reaver).inRadius(9.2, unit).effUndetected().notEmpty()
        ) {
            unit.setTooltip("Aaa-LURKER!");
            return true;
        }

        if (new FightInsteadAvoid(unit, enemies).shouldFight()) {
            unit.addLog("SHOULD FightInsteadAvoid");
            return false;
        }

        if (unit.hpLessThan(17) && !enemies.onlyMelee() && !Enemy.terran()) {
            if (!unit.isMelee() && !unit.isMissionDefendOrSparta()) {
                unit.addLog("AlmostDead");
                return true;
            }
        }

        return true;
    }

    private boolean shouldNeverAvoidIf(Units enemies) {
        if (unit.isWorker() && enemies.onlyMelee()) {
            return unit.hp() >= 40;
        }

        if (unit.isTank() && unit.cooldownRemaining() <= 0) {
            return true;
        }

        if (unit.isWorker() || unit.isAir()) {
            return false;
        }

        return false;
    }

}
