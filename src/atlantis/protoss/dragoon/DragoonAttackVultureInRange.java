package atlantis.protoss.dragoon;

import atlantis.architecture.Manager;
import atlantis.combat.micro.attack.ProcessAttackUnit;
import atlantis.game.player.Enemy;
import atlantis.units.AUnit;
import atlantis.units.range.OurDragoonRange;

public class DragoonAttackVultureInRange extends Manager {
    private AUnit vulture;

    public DragoonAttackVultureInRange(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        return Enemy.terran()
            && unit.isDragoon()
            && (vulture = vulture()) != null
            && unit.cooldown() <= 4;
    }

    @Override
    public Manager handle() {
        if ((new ProcessAttackUnit(unit)).processAttackOtherUnit(vulture)) {
            return usedManager(this);
        }

        return null;
    }

    private AUnit vulture() {
        return unit.enemiesNear()
            .vultures()
            .visibleOnMap()
            .havingAtLeastHp(1)
            .notDeadMan()
            .inRadius(OurDragoonRange.range() + (unit.cooldown() <= 1 ? 0.75 : 0), unit)
            .nearestTo(unit);
    }
}
