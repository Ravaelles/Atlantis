package atlantis.combat.micro.terran.infantry.medic;

import atlantis.architecture.Manager;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;

public class MedicBodyBlock extends Manager {
    public double BODY_BLOCK_POSITION_ERROR_MARGIN = 0.2;
    private APosition posBetweenEnemyAndOurUnit;

    public MedicBodyBlock(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (unit.cooldownRemaining() >= 2) return false;
        if (unit.friendsInRadiusCount(1.2) <= 0) return false;

        Selection meleeEnemies = unit.enemiesNear().melee().inRadius(2.3, unit);
        if (meleeEnemies.count() == 0 || meleeEnemies.count() >= 2) return false;

        AUnit nearestFriend = unit.friendsNear()
            .groundUnits()
            .inRadius(4, unit)
            .excludeTypes(AUnitType.Terran_Medic)
            .notBeingHealed()
            .nearestTo(unit);

        if (nearestFriend == null) return false;

        AUnit nearestEnemy = meleeEnemies.visibleOnMap().inRadius(4, unit).nearestTo(unit);
        if (nearestEnemy == null) return false;

        APosition enemyTarget = nearestEnemy.hasTargetPosition()
            ? nearestEnemy.targetPosition() : nearestFriend.position();
        posBetweenEnemyAndOurUnit = enemyTarget.translateTilesTowards(0.4, nearestEnemy);
        if (unit.distToMoreThan(posBetweenEnemyAndOurUnit, BODY_BLOCK_POSITION_ERROR_MARGIN) || unit.isIdle()) {
            return true;
        }

        return false;
    }

    @Override
    public Manager handle() {
        unit.move(posBetweenEnemyAndOurUnit, Actions.MOVE_MACRO, "Block", false);
        return usedManager(this);
    }
}
