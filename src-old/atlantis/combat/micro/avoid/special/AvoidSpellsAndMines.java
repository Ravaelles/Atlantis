package atlantis.combat.micro.avoid.special;

import atlantis.architecture.Manager;
import atlantis.architecture.ManagerFactory;
import atlantis.units.AUnit;

public class AvoidSpellsAndMines extends Manager {
    public AvoidSpellsAndMines(AUnit unit) {
        super(unit);
    }

    @Override
    protected ManagerFactory[] managers() {
        return new ManagerFactory[]{
            AvoidPsionicStorm::new,
            AvoidMines::new,
        };
    }
}
