package atlantis.combat.micro.avoid.special;

import atlantis.architecture.Manager;
import atlantis.units.AUnit;


public class AvoidSpellsAndMines extends Manager {
    public AvoidSpellsAndMines(AUnit unit) {
        super(unit);
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            AvoidPsionicStorm.class,
            AvoidMines.class,
        };
    }
}
