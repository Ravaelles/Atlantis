package atlantis.combat.micro.avoid.always;

import atlantis.architecture.Manager;
import atlantis.combat.micro.avoid.dont.protoss.DragoonDontAvoidEnemy;
import atlantis.combat.micro.avoid.dont.protoss.ZealotDontAvoidEnemy;
import atlantis.units.AUnit;
import atlantis.units.HasUnit;

public class ProtossAlwaysAvoidEnemy extends HasUnit {
    public ProtossAlwaysAvoidEnemy(AUnit unit) {
        super(unit);
    }

    public boolean applies() {
        if ((new DragoonAlwaysAvoidEnemy(unit).applies())) return true;

        return false;
    }
}
