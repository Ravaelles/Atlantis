package tests.acceptance;

import tests.unit.FakeUnit;

public class NonAbstractTestFakingGame extends AbstractTestFakingGame {

    @Override
    protected FakeUnit[] generateOur() {
        return null;
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return null;
    }
}
