package atlantis.production.dynamic.expansion;

import atlantis.map.base.Bases;
import atlantis.map.position.APosition;
import atlantis.units.select.Count;
import atlantis.util.We;

public class NewBaseIsSecured {
    public static boolean newBaseIsSecured() {
        if (Count.bases() == 1) return SecureNatural.secure();

        return true;
    }
}
