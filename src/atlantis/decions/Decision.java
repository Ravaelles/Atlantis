package atlantis.decions;

public enum Decision {

    ALLOWED,
    INDIFFERENT,
    FORBIDDEN;

    public boolean isAllowed() {
        return this.equals(ALLOWED);
    }

    public boolean isForbidden() {
        return this.equals(FORBIDDEN);
    }

    public boolean isIndifferent() {
        return this.equals(INDIFFERENT);
    }

    public boolean notIndifferent() {
        return !this.equals(INDIFFERENT);
    }

    public boolean toBoolean() {
        assert !isIndifferent();

        return isAllowed();
    }
}
