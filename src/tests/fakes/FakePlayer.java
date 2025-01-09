package tests.fakes;

import atlantis.game.player.APlayer;
import atlantis.units.AUnitType;
import bwapi.*;

public class FakePlayer extends APlayer {

    public static final FakePlayer OUR = new FakePlayer(true, false, false);
    public static final FakePlayer ENEMY = new FakePlayer(false, false, true);
    public static final FakePlayer NEUTRAL = new FakePlayer(false, true, false);

    private static int firstFreeId = 1;

    private int fakeId;
    private boolean our;
    private boolean neutral;
    private boolean enemy;

    // =========================================================

    public FakePlayer() {
        super();
        this.fakeId = ++firstFreeId;
        this.our = false;
    }

    public FakePlayer(boolean our, boolean neutral, boolean enemy) {
        super();
        this.fakeId = ++firstFreeId;
        this.our = our;
        this.neutral = neutral;
        this.enemy = enemy;
    }

    // =========================================================

    @Override
    public String toString() {
        return "FakePlayer{" +
            "#" + fakeId +
            (our ? ", our=true" : "") +
            (neutral ? ", neutral=true" : "") +
            (enemy ? ", enemy=true" : "") +
            '}';
    }

    @Override
    public int id() {
        return this.fakeId;
    }

    // =========================================================

    public int armor(AUnitType unit) {
        return 1;
    }

    public int getUpgradeLevel(UpgradeType upgrade) {
        return 1;
    }

    public boolean isEnemy(APlayer player) {
        return enemy != our;
    }

}
