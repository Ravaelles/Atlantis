package atlantis.production.dynamic.expansion.secure;

import atlantis.map.position.APosition;
import atlantis.production.constructing.position.base.NextBasePosition;

public class NewBaseIsSecured {
    public static boolean newBaseIsSecured() {
        return (new SecuringBase(nextBasePosition())).isSecure();
    }

    public static void secure() {
        (new SecuringBase(nextBasePosition())).secure();
    }

    private static APosition nextBasePosition() {
        return NextBasePosition.nextBasePosition();
    }
}
