package atlantis.protoss;

import atlantis.architecture.Manager;
import atlantis.combat.micro.generic.MobileDetector;
import atlantis.combat.squad.positioning.protoss.dragoon.ProtossDragoonSeparateFromMeleeEnemies;
import atlantis.combat.squad.positioning.protoss.dragoon.ProtossDragoonSeparateFromRangedEnemies;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;

public class ProtossDragoonCombatManager extends MobileDetector {
    public ProtossDragoonCombatManager(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return unit.isDragoon();
//            && unit.hasCooldown()
//            && unit.lastAttackFrameLessThanAgo(30)
//            && unit.meleeEnemiesNearCount(1.8) == 0;
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            ProtossDragoonSeparateFromMeleeEnemies.class,
            ProtossDragoonSeparateFromRangedEnemies.class,
        };
    }

    public AUnitType type() {
        return AUnitType.Protoss_Dragoon;
    }
}
