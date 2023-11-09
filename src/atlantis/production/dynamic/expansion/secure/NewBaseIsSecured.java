package atlantis.production.dynamic.expansion.secure;

import atlantis.map.position.APosition;
import atlantis.production.constructing.position.base.NextBasePosition;

public class NewBaseIsSecured {
    public static boolean newBaseIsSecured() {
        APosition basePosition = nextBasePosition();

        if (basePosition == null) return false;

        return (new SecuringBase(basePosition)).isSecure();
    }

    public static void secure() {
        (new SecuringBase(nextBasePosition())).secure();
    }

    private static APosition nextBasePosition() {
        return NextBasePosition.nextBasePosition();
    }
}
