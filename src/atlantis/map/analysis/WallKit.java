package atlantis.map.analysis;

import atlantis.units.AUnitType;

/**
 * One race's wall vocabulary: which pieces plug a choke and how wide each
 * is, in tiles. Wall geometry (slots, door, fit) is race-neutral and lives
 * in {@code WallPieces}; what differs per race lives here, so a Terran
 * depot wall or a Zerg sunkens line arrives as a new kit, never as
 * branches inside the planner (OCP; D8 keeps the bot Protoss-only today).
 */
public final class WallKit {

    /** Protoss wall-in: Pylon (power) + Gateway (plug) + Photon Cannons. */
    public static final WallKit PROTOSS = new WallKit(
        AUnitType.Protoss_Pylon, 2,
        AUnitType.Protoss_Gateway, 4,
        AUnitType.Protoss_Photon_Cannon, 2);

    private final AUnitType powerPiece;
    private final int powerWidth;
    private final AUnitType plugPiece;
    private final int plugWidth;
    private final AUnitType gunPiece;
    private final int gunWidth;

    public WallKit(
        AUnitType powerPiece, int powerWidth,
        AUnitType plugPiece, int plugWidth,
        AUnitType gunPiece, int gunWidth
    ) {
        this.powerPiece = powerPiece;
        this.powerWidth = powerWidth;
        this.plugPiece = plugPiece;
        this.plugWidth = plugWidth;
        this.gunPiece = gunPiece;
        this.gunWidth = gunWidth;
    }

    public AUnitType powerPiece() {
        return powerPiece;
    }

    public int powerWidth() {
        return powerWidth;
    }

    public AUnitType plugPiece() {
        return plugPiece;
    }

    public int plugWidth() {
        return plugWidth;
    }

    public AUnitType gunPiece() {
        return gunPiece;
    }

    public int gunWidth() {
        return gunWidth;
    }
}
