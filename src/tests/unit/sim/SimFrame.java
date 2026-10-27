package tests.unit.sim;

/**
 * One sim frame's shared state, handed to every engine (constructions,
 * training, mining, movement). Engines read through this instead of
 * each other, so none of them owns another's data.
 */
public final class SimFrame {

    private final SimDriver driver;
    private final int frame;

    SimFrame(SimDriver driver, int frame) {
        this.driver = driver;
        this.frame = frame;
    }

    public SimDriver driver() {
        return driver;
    }

    public int frame() {
        return frame;
    }

    public SimGround ground() {
        return driver.ground();
    }

    public SimBank bank() {
        return driver.bank();
    }

    public SimConfig config() {
        return driver.config();
    }
}
