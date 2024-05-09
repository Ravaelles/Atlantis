package atlantis.combat.squad.positioning.too_lonely;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.information.enemy.EnemyInfo;
import atlantis.information.enemy.EnemyUnits;
import atlantis.information.enemy.EnemyWhoBreachedBase;
import atlantis.units.AUnit;
import atlantis.units.select.Selection;
import atlantis.util.We;

public class ProtossTooLonely extends Manager {
    public ProtossTooLonely(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return We.protoss()
            && unit.isCombatUnit()
            && (A.supplyUsed() <= 100 || EnemyInfo.hasDiscoveredAnyBuilding())
            && (A.isUms() || EnemyWhoBreachedBase.noone())
            && (!unit.isMissionDefendOrSparta() || unit.distToBase() <= 30)
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

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            ProtossTooFarFromLeader.class,
            ProtossTooLonelyGetCloser.class,
        };
    }

    private boolean tooDangerousBecauseOfCloseEnemies() {
        if (unit.woundHp() <= 15 && unit.lastAttackFrameMoreThanAgo(30 * 6)) return false;

        Selection enemies = unit.enemiesNear()
            .inRadius(2.8 + unit.woundPercent() / 50.0, unit)
            .combatUnits();

        return enemies.atLeast(unit.shieldDamageAtMost(30) ? 2 : 1);
    }
}
