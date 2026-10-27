package tests.unit.sim;

/**
 * The sim's wallet: mineral and gas stocks as doubles (income accrues
 * fractionally per frame), spent in whole units.
 *
 * <p>Spending more than the stock is a sim bug, not a loan: it throws.
 * The engine (BWAPI) refuses unfunded orders; the sim refuses unfunded
 * accepts the same way - a negative bank would silently fund the whole
 * opening and prove nothing.
 */
public final class SimBank {

    private double minerals;
    private double gas;

    public SimBank(double minerals, double gas) {
        this.minerals = minerals;
        this.gas = gas;
    }

    public void earn(double minerals, double gas) {
        this.minerals += minerals;
        this.gas += gas;
    }

    public boolean canAfford(int minerals, int gas) {
        return this.minerals >= minerals && this.gas >= gas;
    }

    /** Deducts a validated cost; throws when the stock does not cover it. */
    public void spend(int minerals, int gas, String what) {
        if (!canAfford(minerals, gas)) {
            throw new IllegalStateException(
                "sim overspent on " + what + ": bank=" + (int) this.minerals
                    + "/" + (int) this.gas + " cost=" + minerals + "/" + gas);
        }
        this.minerals -= minerals;
        this.gas -= gas;
    }

    public int minerals() {
        return (int) minerals;
    }

    public int gas() {
        return (int) gas;
    }

    /** Exact stocks, for parity assertions (rounded display lies by a frame). */
    public double mineralsExact() {
        return minerals;
    }

    public double gasExact() {
        return gas;
    }
}
