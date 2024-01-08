package atlantis.information.decisions;

public class Decision {
    private boolean initialValue;
    private boolean currentValue;

    public Decision(boolean initialValue) {
        this.initialValue = initialValue;
        this.currentValue = initialValue;
    }

    public boolean currentValue() {
        return currentValue;
    }

    public void setCurrentValue(boolean currentValue) {
        this.currentValue = currentValue;
    }

    public boolean initialValue() {
        return initialValue;
    }

    public boolean isTrue() {
        return currentValue;
    }

    public boolean isFalse() {
        return !currentValue;
    }
}
