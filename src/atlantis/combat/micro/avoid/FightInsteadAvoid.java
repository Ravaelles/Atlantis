package atlantis.combat.micro.avoid;

import atlantis.architecture.Manager;
import atlantis.combat.micro.attack.enemies.AttackNearbyEnemies;
import atlantis.combat.micro.avoid.fight.ShouldAlwaysFightInsteadAvoid;
import atlantis.combat.micro.avoid.terran.avoid.TerranAlwaysAvoidEnemy;
import atlantis.combat.micro.avoid.zerg.ShouldAlwaysAvoidAsZerg;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.Units;
import atlantis.game.player.Enemy;

public class FightInsteadAvoid extends Manager {
    private TerranAlwaysAvoidEnemy shouldAlwaysAvoidAsTerran;
    private ShouldAlwaysAvoidAsZerg shouldAlwaysAvoidAsZerg;
    private final Units enemies;

    public FightInsteadAvoid(AUnit unit, Units enemies) {
        super(unit);
        shouldAlwaysAvoidAsTerran = new TerranAlwaysAvoidEnemy(unit);
        shouldAlwaysAvoidAsZerg = new ShouldAlwaysAvoidAsZerg(unit);
        this.enemies = enemies;
    }

    @Override
    public boolean applies() {
        if (unit.isDragoon()) return false; // @Remove

        if (!unit.hasAnyWeapon()) return false;
        if (unit.eval() <= 0.6) return false;

//        System.err.println(
//            "### " + unit.type() + "(" + unit.idWithHash() + ").combatEvalRelative() = " + unit.combatEvalRelative()
//        );

        if (new ShouldAlwaysFightInsteadAvoid(unit, enemies).shouldFight()) {
            unit.addLog("SHOULD FightInsteadAvoid");
            return true;
        }

        if (unit.eval() < 0.6) return false;
        if (shouldAlwaysAvoidAsTerran.shouldAlwaysAvoid()) return false;
        if (shouldAlwaysAvoidAsZerg.shouldAlwaysAvoid()) return false;

        if (
            unit.isInfantry()
                && unit.enemiesNear().ofType(AUnitType.Zerg_Lurker).inRadius(7.2, unit).effUndetected().notEmpty()
        ) {
            unit.setTooltip("AttackWorkersWhenItMakesSense-LURKER!");
            return false;
        }

        if (
            unit.isInfantry()
                && unit.enemiesNear().ofType(AUnitType.Protoss_Reaver).inRadius(9.2, unit).effUndetected().notEmpty()
        ) {
            unit.setTooltip("AttackWorkersWhenItMakesSense-LURKER!");
            return false;
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
        AttackNearbyEnemies attackNearbyEnemies = new AttackNearbyEnemies(unit);
        if (attackNearbyEnemies.handleAttackNearEnemyUnits()) {
            return usedManager(this, "AvoidButAttack");
        }

        return null;
    }
}
