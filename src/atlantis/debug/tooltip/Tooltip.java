package atlantis.debug.tooltip;

import atlantis.game.AGame;
import atlantis.units.AUnit;


public class Tooltip {

    private final AUnit unit;
    private String tooltip;
    private int tooltipStartInFrames;

    public Tooltip(AUnit u, String text) {
        unit = u;
        setTooltip(text);
    }

    public AUnit getUnit() {
        return unit;
    }

    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
        this.tooltipStartInFrames = AGame.now();
    }

    public String getTooltip() {
//        if (AGame.getTimeFrames() - tooltipStartInFrames > 30) {
//            String tooltipToReturn = this.tooltip;
//            this.removeTooltip();
//            return tooltipToReturn;
//        } else {
        return tooltip;
//        }
    }

    public void removeTooltip() {
        this.tooltip = null;
    }

    public boolean hasTooltip() {
        return this.tooltip != null;
    }

    @Override
    public int hashCode() {
        return unit.id();
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || !(other instanceof Tooltip)) return false;

        return unit.id() == ((Tooltip) other).getUnit().id();

    }

    @Override
    public String toString() {
        return String.format(
            "Tooltip for (%d) %s %s: %s. Start @ frame %d",
            unit.id(),
            unit.type().name(),
            unit.position().toTilePosition(),
            hasTooltip() ? String.format("'%s'", tooltip) : "null",
            tooltipStartInFrames
        );
    }
}
