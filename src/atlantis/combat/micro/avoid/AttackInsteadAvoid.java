package atlantis.combat.micro.avoid;

import atlantis.architecture.Manager;
import atlantis.combat.micro.attack.AttackNearbyEnemies;
import atlantis.combat.micro.avoid.fight.FightInsteadAvoid;
import atlantis.combat.micro.avoid.terran.ShouldAlwaysAvoidAsTerran;
import atlantis.combat.micro.avoid.zerg.ShouldAlwaysAvoidAsZerg;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Units;
import atlantis.util.Enemy;

public class AttackInsteadAvoid extends Manager {
    private ShouldAlwaysAvoidAsTerran shouldAlwaysAvoidAsTerran;
    private ShouldAlwaysAvoidAsZerg shouldAlwaysAvoidAsZerg;
    private final Units enemies;

    public AttackInsteadAvoid(AUnit unit, Units enemies) {
        super(unit);
        shouldAlwaysAvoidAsTerran = new ShouldAlwaysAvoidAsTerran(unit);
        shouldAlwaysAvoidAsZerg = new ShouldAlwaysAvoidAsZerg(unit);
        this.enemies = enemies;
    }

    @Override
    public boolean applies() {
        if (!unit.hasAnyWeapon()) return false;

//        System.err.println(
//            "### " + unit.type() + "(" + unit.idWithHash() + ").combatEvalRelative() = " + unit.combatEvalRelative()
//        );

        if (unit.combatEvalRelative() < 0.6) return false;
        if (shouldAlwaysAvoidAsTerran.shouldAlwaysAvoid()) return false;
        if (shouldAlwaysAvoidAsZerg.shouldAlwaysAvoid()) return false;

        if (
            unit.isInfantry()
                && unit.enemiesNear().ofType(AUnitType.Zerg_Lurker).inRadius(7.2, unit).effUndetected().notEmpty()
        ) {
            unit.setTooltip("Aaa-LURKER!");
            return false;
        }

        if (
            unit.isInfantry()
                && unit.enemiesNear().ofType(AUnitType.Protoss_Reaver).inRadius(9.2, unit).effUndetected().notEmpty()
        ) {
            unit.setTooltip("Aaa-LURKER!");
            return false;
        }

        if (new FightInsteadAvoid(unit, enemies).shouldFight()) {
            unit.addLog("SHOULD FightInsteadAvoid");
            return true;
        }

        if (unit.hpLessThan(17) && !enemies.onlyMelee() && !Enemy.terran()) {
            if (!unit.isMelee() && !unit.isMissionDefendOrSparta()) {
                unit.addLog("AlmostDead");
                return false;
            }
        }

        return false;
    }

    @Override
    protected Manager handle() {
        if (unit.combatEvalRelative() <= 0.6) return null;

        AttackNearbyEnemies attackNearbyEnemies = new AttackNearbyEnemies(unit);
        if (attackNearbyEnemies.handleAttackNearEnemyUnits()) {
            return usedManager(attackNearbyEnemies, "AvoidButAttack");
        }

        return null;
    }
}
