package bweb;

public class Races {
    public static final Race Protoss = new Race();
    public static final Race Terran = new Race();
    public static final Race Zerg = new Race();
    public static class Race {
        public BWAPI.UnitType getResourceDepot() { return UnitTypes.None; }
    }
} 