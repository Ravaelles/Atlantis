package atlantis.combat.micro.generic.managers;

import atlantis.architecture.Manager;
import atlantis.combat.squad.squads.alpha.Alpha;
import atlantis.game.A;
import atlantis.game.player.Enemy;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Selection;
import atlantis.util.log.ErrorLog;

public class AsAirAvoidAntiAir extends Manager {
    private HasPosition enemyAA;
    private boolean isGroundEnemy = true;
    private Selection enemies;

    public AsAirAvoidAntiAir(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isAir()) return false;
//        if (unit.woundHp() <= 8 && unit.eval() >= 1.6) return false;

        enemyAA = enemyAntiAirInRange();
        if (enemyAA == null) return false;
        if (unit.isProtoss() && unit.shieldWound() <= 3 && enemies.atMost(1)) return false;

        return true;
    }

    public Manager handle() {
        Manager manager = Manager.invokedFor(PreventAirCornerStucking.class, unit);
        if (manager != null && unit.isMoving()) {
            return usedManager(manager);
        }

        if (!isGroundEnemy && AsAirRunToCannon.shouldRunToCannonGeneric(unit)) {
            if (invokedManager(AsAirRunToCannon.class)) return usedManager(AsAirRunToCannon.class);
        }

        Selection enemies = unit.enemiesThatCanAttackMe(safetyMargin());
        if (isGroundEnemy && enemies.notEmpty()) {
            HasPosition runFrom = enemies.center();
            if (runFrom == null) runFrom = enemyAA;

            if (unit.moveAwayFrom(runFrom, 6, Actions.MOVE_FORMATION, "AirAvoidAA")) return usedManager(this);
        }

        if (!isGroundEnemy) {
            if (goToAlphaLeader()) return usedManager(this, "AARunToAlphaLeader");
            if (unit.moveToCannon(Actions.MOVE_SAFETY)) return usedManager(this, "AARunToCannon");
        }

        return null;
    }

    private double safetyMargin() {
        if (unit.shieldWound() <= 3) return 1.0;

        return 1.2 + unit.woundPercent() / 14.0;
    }

    private boolean goToAlphaLeader() {
        if (isGroundEnemy) return false;

        AUnit leader = Alpha.alphaLeader();
        if (leader == null) return false;

        if (unit.distTo(leader) <= 1.8) return false;
        if (Alpha.get().units().havingAntiAirWeapon().atMost(2)) return false;
        if (unit.woundPercent() <= 30) return false;

        return unit.move(leader, Actions.MOVE_SAFETY, "AARunToAlphaLeader");
    }

    private HasPosition enemyAntiAirInRange() {
        enemies = unit.enemiesNear().havingPosition();
        if (Enemy.zerg()) enemies = enemies.nonBuildings();

        enemies = enemies
            .havingAntiAirWeapon()
            .canAttack(unit, enemiesRange());

        if (!enemies.onlyGround()) {
            isGroundEnemy = false;
        }

        if (isGroundEnemy && enemies.size() == 1) {
            HasPosition position = enemies.first();
            position = position.randomizeByTiles(6, A.s % 6 + unit.id());

//            System.err.println(enemies.first().distTo(position));

            if (position == null) ErrorLog.printMaxOncePerMinutePlusPrintStackTrace("randomizeByTiles positionNull");
            else return position;
        }

        APosition center = enemies.center();
        if (center != null) return center;

        return enemies.first();
    }

    private double enemiesRange() {
        return 2.2
            + (unit.shieldWound() <= 4
                ? 0
                : (1 + unit.woundPercent() / 13.0)
            )
            + (unit.lastUnderAttackLessThanAgo(30 * 6) ? 2 : 0);
    }
}
