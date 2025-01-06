package atlantis.combat.micro.early.protoss;

import atlantis.architecture.Manager;
import atlantis.combat.micro.early.protoss.stick.ProtossForceFightNearCannon;
import atlantis.combat.micro.early.protoss.stick.ProtossForgeExpandStickToCannon;
import atlantis.combat.micro.early.protoss.stick.ProtossForgeExpandStickToCannonSpecialized;
import atlantis.units.AUnit;
import atlantis.units.select.Selection;

public class ProtossEarlyGame extends Manager {
    private Selection enemies;

    public ProtossEarlyGame(AUnit unit) {
        super(unit);
    }

    @Override
    protected Class<? extends Manager>[] managers() {
        return new Class[]{
            ProtossAvoidEarlyGameLings.class,
//            ProtossForgeExpandStickToCannonSpecialized.class,
//            ProtossForgeExpandStickToCannon.class,
            ProtossForceFightNearCannon.class,
        };
    }
}

