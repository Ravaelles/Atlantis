package atlantis.combat.micro.avoid;

import atlantis.combat.micro.avoid.terran.fight.ShouldAlwaysFightInsteadAvoid;
import atlantis.units.AUnit;
import atlantis.units.HasUnit;
import atlantis.units.Units;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;

public class ShouldNotAvoid extends HasUnit {
    private final Units enemiesDangerouslyClose;

    public ShouldNotAvoid(AUnit unit, Units enemiesDangerouslyClose) {
        super(unit);
        this.enemiesDangerouslyClose = enemiesDangerouslyClose;
    }

    public boolean shouldNotAvoid() {
        if (enemiesDangerouslyClose.isEmpty()) return true;
        if (unit.isLoaded()) return true;
        if (unit.enemiesNear().canAttack(unit, 6).empty()) return true;
        if (unit.lastActionLessThanAgo(50, Actions.SPECIAL)) return true;
        if (isUnitCloakedAndRelativelySafe()) return true;
        if (isAlmostDeadMeleeSoGoKamikaze()) return true;
        if (onlyEnemyWorkersNearby()) return true;

        if ((new ShouldAlwaysFightInsteadAvoid(unit, enemiesDangerouslyClose)).shouldFight()) return true;

        if (unit.enemiesNear().combatBuildings(false).notEmpty()) return false;

        // @Needed?
//        if (
//            unit.lastActionLessThanAgo(5, Actions.ATTACK_UNIT)
//                && unit.lastStartedAttackMoreThanAgo(8)
//        ) {
//            unit.setTooltipTactical("StartAttack");
//            return true;
//        }

        return false;
    }

    private boolean isAlmostDeadMeleeSoGoKamikaze() {
        int hpThreshold = 16;

        return unit.isMelee()
            && unit.hp() <= hpThreshold
            && unit.isCombatUnit()
            && unit.enemiesNear().groundUnits().havingAntiGroundWeapon().inRadius(1, unit).notEmpty()
            && unit.addLog("Kamikaze");
    }

    private boolean onlyEnemyWorkersNearby() {
//        if (enemiesDangerouslyClose == null) return false;

        if (
            unit.hpPercent() >= 70
                && !enemiesDangerouslyClose.isEmpty()
                && Select.from(enemiesDangerouslyClose).workers().size() == enemiesDangerouslyClose.size()
        ) {
            unit.addLog("FightWorkers");
            return true;
        }

        return false;
    }

    private boolean isUnitCloakedAndRelativelySafe() {
        if (!unit.effUndetected()) return false;

        return unit.hp() >= 23
            && (
            unit.lastUnderAttackMoreThanAgo(30 * 15)
                ||
                (
                    unit.enemiesNear().combatBuildingsAnti(unit).inRadius(9, unit).empty()
                        && unit.enemiesNear().detectors().inRadius(11, unit).empty()
                )
        );
    }
}
