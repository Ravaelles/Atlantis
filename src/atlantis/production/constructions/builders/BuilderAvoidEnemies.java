package atlantis.production.constructions.builders;

import atlantis.architecture.Manager;
import atlantis.combat.micro.avoid.AvoidSingleEnemy;
import atlantis.information.enemy.EnemyUnits;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.fogged.FoggedUnit;

public class BuilderAvoidEnemies extends Manager {
    private AUnit enemy;

    public BuilderAvoidEnemies(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isBuilder()
            && (enemy = defineEnemy()) != null;
    }

    private AUnit defineEnemy() {
        AUnit enemyVisible = unit.enemiesThatCanAttackMe(safetyMargin()).combatUnits().nearestTo(unit);
        if (enemyVisible != null) return enemyVisible;

        FoggedUnit foggedEnemy = (FoggedUnit) EnemyUnits.foggedUnits()
            .combatUnits()
            .groundUnits()
            .canAttack(unit, 1.3 + unit.woundPercent() / 40.0)
            .havingAntiGroundWeapon()
            .lastSeenAtLeastMinuteAgo()
            .nearestTo(unit);

        return foggedEnemy;
    }

    private double safetyMargin() {
        return 2.8
            + unit.woundPercent() / 30.0
            + Math.min(2.5, unit.distToBase() / 30.0);
    }

    @Override
    public Manager handle() {
        if ((new AvoidSingleEnemy(unit, enemy)).forceHandle() != null) {
            return usedManager(this);
        }

        if (unit.runOrMoveAway(enemy, 6)) {
            return usedManager(this);
        }

        if (unit.moveToSafety(Actions.MOVE_SAFETY)) {
            return usedManager(this);
        }

        return null;
    }
}
