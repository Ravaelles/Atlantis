package atlantis.debug.tooltip;

import atlantis.AtlantisGame;
import atlantis.util.NameUtil;
import bwapi.Unit;

public class Tooltip {
	private Unit unit;
	private String tooltip;
	private int tooltipStartInFrames;

	
	public Tooltip(Unit u, String text){
		unit = u;
		setTooltip(text);
	}
    
	public Unit getUnit(){
		return unit;
	}
	
	public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
        this.tooltipStartInFrames = AtlantisGame.getTimeFrames();
    }

    public String getTooltip() {
        if (AtlantisGame.getTimeFrames() - tooltipStartInFrames > 30) {
            String tooltipToReturn = this.tooltip;
            this.removeTooltip();
            return tooltipToReturn;
        } else {
            return tooltip;
        }
    }

    public void removeTooltip() {
        this.tooltip = null;
    }

    public boolean hasTooltip() {
        return this.tooltip != null;
    }
    
    @Override
    public int hashCode(){
    	return unit.getID();
    }
    
    @Override
    public boolean equals(Object other){
    	if (other == null || ! (other instanceof Tooltip)) return false;
    	
    	return unit.getID() == ((Tooltip)other).getUnit().getID();
    	
    }
    
    @Override
    public String toString(){
    	return String.format(
			"Tooltip for (%d) %s %s: %s. Start @ frame %d", 
			unit.getID(),
			NameUtil.getShortName(unit.getType()),
			unit.getPosition().toTilePosition(), 
			hasTooltip() ? String.format("'%s'", tooltip) : "null", 
			tooltipStartInFrames
		);
    }
}
