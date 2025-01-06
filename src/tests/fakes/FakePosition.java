package tests.fakes;

import atlantis.map.position.APosition;

public class FakePosition extends APosition {
    public FakePosition(APosition position) {
        super(position);
    }

    public FakePosition(int pixelX, int pixelY) {
        super(pixelX, pixelY);
    }

    @Override
    public boolean hasPathTo(APosition position) {
        return true;
    }
}