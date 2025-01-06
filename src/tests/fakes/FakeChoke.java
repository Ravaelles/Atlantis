package tests.fakes;


import atlantis.map.choke.AChoke;
import atlantis.map.position.APosition;

public class FakeChoke extends AChoke {
    private APosition position;
    private int width;

    public FakeChoke(APosition position, int width) {
        this.position = position;
        this.center = position;
        this.width = width;
    }

    @Override
    public APosition position() {
        return position;
    }

    @Override
    public int width() {
        return width;
    }
}
