package atlantis.combat.squad.positioning.protoss;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnitBreachedBase;
import atlantis.information.enemy.EnemyUnits;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.select.Have;
import atlantis.units.select.Selection;

public class ProtossTooFarFromLeader extends Manager {
    private double distToLeader;
    private AUnit leader;

    public ProtossTooFarFromLeader(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (true) return false;

//        if (!previousApplies()) return false;

//        if (unit.enemiesNear().inRadius(6, unit).notEmpty()) return false;
        if (EnemyUnitBreachedBase.notNull()) return false;
        if (unit.squad().isLeader(unit)) return false;
        if (unit.isRunning()) return false;
        if (unit.lastStartedRunningLessThanAgo(20)) return false;
        if (unit.enemiesNear().combatBuildingsAntiLand().notEmpty()) return false;

        this.leader = unit.squadLeader();
        if (this.leader == null) return false;

        if (A.supplyUsed() >= 170 && (
            unit.enemiesNear().empty() || EnemyUnits.discovered().buildings().atMost(1)
        )) return false;

        if (unit.isMissionSparta()) return false;

        if (
            unit.lastAttackFrameMoreThanAgo(30 * 10)
                && unit.enemiesThatCanAttackMe(4).empty()
        ) return false;

        distToLeader = unit.distTo(this.leader);
        boolean wayTooFarFromLeader = wayTooFarFromLeader();

//        if (distToLeader >= 30 && unit.isMissionDefend()) return false;
        if (wayTooFarFromLeader) return true;

        if (isDangerousToGoToLeaderAndWeArentRetreating()) return false;

        if (
            unit.enemiesNear().atLeast(9)
                || unit.enemiesNear().inRadius(5, unit).atLeast(2)
        ) return false;

//        if (unit.isDragoon() && unit.hp() <= 40) return false;

        if (unit.distToNearestChokeLessThan(4)) return false;

        if (distToLeader >= 5 && unit.lastPositionChangedMoreThanAgo(30)) return true;

        if (leaderIsOvercrowded()) return false;
        if (unitIsOvercrowded()) return false;

        return tooFarFromLeader();
    }

    private boolean isDangerousToGoToLeaderAndWeArentRetreating() {
        return leader != null
            && (leader.isRunning() || leader.isRetreating())
            && (
            unit.enemiesThatCanAttackMe(5).count() >= 3
                || leader.enemiesThatCanAttackMe(5).count() >= 3
        );
    }

    private boolean previousApplies() {
        return (A.supplyUsed() <= 100 || EnemyInfo.hasDiscoveredAnyBuilding())
            && (A.isUms() || EnemyUnitBreachedBase.noone())
//            && (!unit.isMissionDefendOrSparta() || unit.distToBase() <= 30)
            && unit.distToBase() <= 25
            && (unit.noCooldown() || unit.looksIdle() || unit.distToBase() >= 30)
//            && !unit.hasCooldown()
            && unit.isGroundUnit()
            && !unit.isDT()
            && (!unit.isDragoon() || unit.enemiesNearInRadius(5) == 0)
            && unit.lastStoppedRunningMoreThanAgo(10)
            && !tooDangerousBecauseOfCloseEnemies()
            && unit.friendsNear().combatUnits().inRadius(4, unit).atMost(10)
            && !unit.distToNearestChokeLessThan(5);
    }

    private boolean tooDangerousBecauseOfCloseEnemies() {
        if (unit.woundHp() <= 15 && unit.lastAttackFrameMoreThanAgo(30 * 6)) return false;

        Selection enemies = unit.enemiesNear()
            .inRadius(2.8 + unit.woundPercent() / 50.0, unit)
            .combatUnits();

        return enemies.atLeast(unit.shieldDamageAtMost(30) ? 2 : 1);
    }

    private boolean wayTooFarFromLeader() {
        int maxDistance = A.inRange(
            unit.isRanged() ? 5 : 2,
            (Have.dragoon() ? 5 : 3) + unit.squadSize() / 5,
            8
        );

        return distToLeader >= maxDistance;
    }

    private boolean unitIsOvercrowded() {
        return
//            unit.friendsNear().groundUnits().countInRadius(1, unit) >= 2
//            || (
            unit.friendsNear().groundUnits().countInRadius(1.5, unit) >= 5;
//                && unit.friendsNear().groundUnits().countInRadius(3, unit) >= 8
//        );

//        return unit.friendsNear().groundUnits().countInRadius(1, unit) >= 2
//            || (
//            unit.friendsNear().groundUnits().countInRadius(1.5, unit) >= 5
//                && unit.friendsNear().groundUnits().countInRadius(3, unit) >= 8
//        );
    }

    private boolean leaderIsOvercrowded() {
        return leader.isStuck()
            || unit.friendsNear().groundUnits().countInRadius(3, unit) >= 4
            || unit.friendsNear().groundUnits().countInRadius(5, unit) >= 9;
    }

    private boolean tooFarFromLeader() {
        return distToLeader > maxDistFromLeader();
    }

    private double maxDistFromLeader() {
        if (unit.squadSize() >= 30) return 15;

        return Math.min(7, 3 + unit.squadSize() / 4);
    }

    protected Manager handle() {
        if (leader == null) leader = unit.squadLeader();

        if (unit.distTo(leader) < 2) return null;

        if (!unit.isMoving() || A.everyNthGameFrame(5)) {
            if (unit.move(leader, Actions.MOVE_FORMATION, "Coordinate")) return usedManager(this);
        }

        return null;
    }
}
