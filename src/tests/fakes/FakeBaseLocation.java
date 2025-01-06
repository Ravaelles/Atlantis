package tests.fakes;

import atlantis.map.base.ABaseLocation;
import atlantis.map.position.APosition;

public class FakeBaseLocation extends ABaseLocation {
    private final int tx;
    private final int ty;
    private final boolean startLocation;
    private final double distToMain;
    private final APosition position;

    public FakeBaseLocation(int tx, int ty, boolean startLocation, double distToMain) {
        super();
        this.tx = tx;
        this.ty = ty;
        this.startLocation = startLocation;
        this.distToMain = distToMain;
        this.position = new FakePosition(tx * 32, ty * 32);
    }

    @Override
    public boolean isStartLocation() {
        return startLocation;
    }

    @Override
    public int x() {
        return tx * 32;
    }

    @Override
    public int y() {
        return ty * 32;
    }

    @Override
    public APosition position() {
        return position;
    }

    public double distToMain() {
        return distToMain;
    }
}
