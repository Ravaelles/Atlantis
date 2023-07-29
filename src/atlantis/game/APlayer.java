package atlantis.game;

import atlantis.units.AUnitType;
import bwapi.*;

import java.util.HashMap;
import java.util.Map;

public class APlayer implements Comparable<APlayer> {

    private static final Map<Player, APlayer> mapping = new HashMap<>();

    private final Player p;

    // =========================================================

    public static APlayer create(Player p) {
        if (mapping.containsKey(p)) {
            return mapping.get(p);
        }

        APlayer aPlayer = new APlayer(p);
        mapping.put(p, aPlayer);

        return aPlayer;
    }

    public APlayer() {
        p = null;
    }

    public APlayer(Player p) {
        this.p = p;
        mapping.put(p, this);
    }

    // =========================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof APlayer)) return false;
        APlayer aPlayer = (APlayer) o;
        return aPlayer.id() == id();
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id());
    }

    @Override
    public int compareTo(APlayer o) {
        return Integer.compare(o.id(), this.id());
    }

    // =========================================================

    public int id() {
        return p.getID();
    }

    public Race getRace() {
        return p.getRace();
    }

    public boolean isEnemy(APlayer player) {
        return p.isEnemy(player.p);
    }

    private boolean isObserver() {
        return p.isObserver();
    }

    private boolean isNeutral() {
        return p.isNeutral();
    }

    public int getUpgradeLevel(UpgradeType upgrade) {
        return p.getUpgradeLevel(upgrade);
    }

    public boolean hasResearched(TechType tech) {
        return p.hasResearched(tech);
    }

    public int armor(AUnitType type) {
        return p.armor(type.ut());
    }

    public Iterable<? extends Unit> getUnits() {
        return p.getUnits();
    }
}
