package atlantis.combat.micro.early.protoss;

import atlantis.architecture.Manager;
import atlantis.information.generic.OurArmy;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Count;
import atlantis.units.select.Select;

public class ProtossForgeExpandStickToCannon extends Manager {
    private AUnit cannon;

    public ProtossForgeExpandStickToCannon(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (!unit.isCombatUnit()) return false;
        if (Count.cannons() <= 0) return false;
        if (unit.shieldWounded() && unit.isRunning()) return false;

        int goons = Count.dragoons();
        if (goons >= 3 || (goons >= 1 && OurArmy.strength() >= 120)) return false;

        if (unit.isDragoon() && unit.shotSecondsAgo() >= 4) return false;

        cannon = nearestCannon();

        if (asMeleeEnemyVeryClose()) return false;

        return isTooFarFromCannon();
    }

    @Override
    public Manager handle() {
        if (unit.move(cannon, Actions.MOVE_FORMATION, "HugCannon")) {
            return usedManager(this);
        }

        return null;
    }

    private boolean asMeleeEnemyVeryClose() {
        if (unit.isRanged()) return false;

        AUnit enemy = unit.enemiesNear().inRadius(2.2, unit).nearestTo(unit);
        if (enemy != null && enemy.distTo(cannon) <= 6.9) {
            return true;
        }

        return false;
    }

    private boolean isTooFarFromCannon() {
        return unit.distTo(cannon) > (unit.enemiesNear().ranged().empty() ? 5.2 : maxDistWhenRangedNear());
    }

    private double maxDistWhenRangedNear() {
        return 1.8 + (unit.isRunning() ? (3 + unit.woundPercent() / 40.0) : 0);
    }

    private AUnit nearestCannon() {
        AUnit cannon = unit.friendsNear().cannons().nearestTo(unit);
        if (cannon != null) {
            return cannon;
        }

        return Select.ourWithUnfinished(AUnitType.Protoss_Photon_Cannon).nearestTo(unit);
    }
}
