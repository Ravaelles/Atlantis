package tests.e2e;

import tests.unit.AbstractTestWithUnits;

/**
 * The one shared bank behind every sim harness: pushes sim income into the
 * test's fields (which the A.* stubs read) and answers the runner's loop
 * from the same numbers. Five identical anonymous classes died for this.
 */
final class SimBank implements OpeningRunner.BankAndSupply, tests.unit.sim.SimDriver.SimFields {

    private final AbstractTestWithUnits harness;

    SimBank(AbstractTestWithUnits harness) {
        this.harness = harness;
    }

    @Override
    public void setMinerals(int minerals) {
        harness.setBankMinerals(minerals);
    }

    @Override
    public void setGas(int gas) {
        harness.setBankGas(gas);
    }

    @Override
    public void setSupply(int used, int total) {
        harness.setBankSupply(used, total);
    }

    @Override
    public int minerals() {
        return harness.bankMinerals();
    }

    @Override
    public int supplyUsed() {
        return harness.bankSupplyUsed();
    }

    @Override
    public int gas() {
        return harness.bankGas();
    }
}
