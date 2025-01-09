package atlantis.combat.micro.attack;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Selection;
import atlantis.game.player.Enemy;

public class AttackParamountUnitsInRange extends Manager {
    private AUnit crucialEnemy;

    public AttackParamountUnitsInRange(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isCombatUnit()) return false;
        if (!unit.type().hasAirWeapon()) return false;
        if (unit.enemiesNear().empty()) return false;
        if (unit.cooldown() >= 6) return false;

        return (crucialEnemy = crucialEnemyInRange()) != null;
    }

    @Override
    public Manager handle() {
        if (unit.attackUnit(crucialEnemy)) {
            return usedManager(this, "CrucialAttack");
        }

        return null;
    }

    private AUnit crucialEnemyInRange() {
        Selection enemies = null;

        if (Enemy.zerg()) {
            enemies = unit.enemiesNear().ofType(
                AUnitType.Zerg_Scourge,
                AUnitType.Zerg_Defiler
            );
        }
        else if (Enemy.protoss()) {
            enemies = unit.enemiesNear().ofType(
                AUnitType.Protoss_Observer,
                AUnitType.Protoss_Dark_Templar
            );
        }
        else if (Enemy.terran()) {
            return null;
//            enemies = unit.enemiesNear().tanks();
        }

        Selection targets = enemies
            .realUnits()
            .notDeadMan()
            .effVisible()
            .canBeAttackedBy(unit, 0.2);

        Selection closeTargets = targets.canBeAttackedBy(unit, -0.7);
        if (!closeTargets.isEmpty()) {
            return closeTargets.randomWithSeed(unit.id());
        }

        return targets.nearestTo(unit);
    }
}
