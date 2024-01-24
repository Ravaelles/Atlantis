package atlantis.protoss;

import atlantis.game.A;

public class ProtossFlags {
    public static boolean dragoonBeBrave() {
        return A.supplyUsed() <= 190;
    }
}
