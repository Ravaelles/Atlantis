package atlantis.combat.squad.positioning.protoss.dragoon;

import atlantis.architecture.Manager;
import atlantis.game.A;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.units.actions.Actions;
import atlantis.units.range.OurDragoonRange;
import atlantis.units.select.Selection;
import atlantis.util.Enemy;
import atlantis.util.We;

public class ProtossDragoonSeparate extends Manager {
    public ProtossDragoonSeparate(AUnit unit) {
        super(unit);
    }

    @Override
    public boolean applies() {
        if (true) return false;

        if (unit.enemiesNear().empty()) return false;
        if (unit.shotSecondsAgo() >= 5) return false;

//        return unit.cooldown() >= 10 || unit.friendsNear().cannons().inRadius(2.8, unit).empty();
        return unit.cooldown() >= 10 || unit.hp() <= 60;
    }

//    @Override
//    protected Class<? extends Manager>[] managers() {
//        return new Class[]{
//            ProtossDragoonSeparateFromMeleeEnemies.class,
//            ProtossDragoonSeparateFromRangedEnemies.class,
//        };
//    }
}
