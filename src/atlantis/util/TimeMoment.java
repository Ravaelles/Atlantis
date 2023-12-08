package atlantis.util;

import atlantis.game.A;

public class TimeMoment {
    private int frames;

    public TimeMoment(int frames) {
        this.frames = frames;
    }

    public int moment() {
        return frames;
    }

    public boolean lessThanSecondsAgo(int secondsAgo) {
        return ago() <= 30 * secondsAgo;
    }

    public boolean lessThanAgo(int framesAgo) {
        return ago() <= framesAgo;
    }

    private int ago() {
        return A.now() - frames;
    }
}
