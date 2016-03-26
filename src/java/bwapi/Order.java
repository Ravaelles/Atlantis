package bwapi;

import bwapi.*;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.List;

/**
An Order (Order type) represents a Unit's current action and can be retrieved with UnitInterface::getOrder. It can also be used to identify the current state of the unit during command execution (gathering minerals can consist of Orders::MoveToMinerals, Orders::WaitForMinerals, Orders::MiningMinerals, etc.). See also UnitInterface::getOrder, Orders
*/
/**
Expected type constructor. If the type is an invalid type, then it becomes Types::Unknown. A type is invalid if its value is less than 0 or greater than Types::Unknown. Parameters id The id that corresponds to this type. It is typically an integer value that corresponds to an internal Broodwar type. If the given id is invalid, then it becomes Types::Unknown.
*/
public class Order {

    public String toString() {
        return toString_native(pointer);
    }

    public static final Order Die = new Order(0);

    public static final Order Stop = new Order(0);

    public static final Order Guard = new Order(0);

    public static final Order PlayerGuard = new Order(0);

    public static final Order TurretGuard = new Order(0);

    public static final Order BunkerGuard = new Order(0);

    public static final Order Move = new Order(0);

    public static final Order AttackUnit = new Order(0);

    public static final Order AttackTile = new Order(0);

    public static final Order Hover = new Order(0);

    public static final Order AttackMove = new Order(0);

    public static final Order InfestedCommandCenter = new Order(0);

    public static final Order UnusedNothing = new Order(0);

    public static final Order UnusedPowerup = new Order(0);

    public static final Order TowerGuard = new Order(0);

    public static final Order VultureMine = new Order(0);

    public static final Order Nothing = new Order(0);

    public static final Order CastInfestation = new Order(0);

    public static final Order InfestingCommandCenter = new Order(0);

    public static final Order PlaceBuilding = new Order(0);

    public static final Order CreateProtossBuilding = new Order(0);

    public static final Order ConstructingBuilding = new Order(0);

    public static final Order Repair = new Order(0);

    public static final Order PlaceAddon = new Order(0);

    public static final Order BuildAddon = new Order(0);

    public static final Order Train = new Order(0);

    public static final Order RallyPointUnit = new Order(0);

    public static final Order RallyPointTile = new Order(0);

    public static final Order ZergBirth = new Order(0);

    public static final Order ZergUnitMorph = new Order(0);

    public static final Order ZergBuildingMorph = new Order(0);

    public static final Order IncompleteBuilding = new Order(0);

    public static final Order BuildNydusExit = new Order(0);

    public static final Order EnterNydusCanal = new Order(0);

    public static final Order Follow = new Order(0);

    public static final Order Carrier = new Order(0);

    public static final Order ReaverCarrierMove = new Order(0);

    public static final Order CarrierIgnore2 = new Order(0);

    public static final Order Reaver = new Order(0);

    public static final Order TrainFighter = new Order(0);

    public static final Order InterceptorAttack = new Order(0);

    public static final Order ScarabAttack = new Order(0);

    public static final Order RechargeShieldsUnit = new Order(0);

    public static final Order RechargeShieldsBattery = new Order(0);

    public static final Order ShieldBattery = new Order(0);

    public static final Order InterceptorReturn = new Order(0);

    public static final Order BuildingLand = new Order(0);

    public static final Order BuildingLiftOff = new Order(0);

    public static final Order DroneLiftOff = new Order(0);

    public static final Order LiftingOff = new Order(0);

    public static final Order ResearchTech = new Order(0);

    public static final Order Upgrade = new Order(0);

    public static final Order Larva = new Order(0);

    public static final Order SpawningLarva = new Order(0);

    public static final Order Harvest1 = new Order(0);

    public static final Order Harvest2 = new Order(0);

    public static final Order MoveToGas = new Order(0);

    public static final Order WaitForGas = new Order(0);

    public static final Order HarvestGas = new Order(0);

    public static final Order ReturnGas = new Order(0);

    public static final Order MoveToMinerals = new Order(0);

    public static final Order WaitForMinerals = new Order(0);

    public static final Order MiningMinerals = new Order(0);

    public static final Order Harvest3 = new Order(0);

    public static final Order Harvest4 = new Order(0);

    public static final Order ReturnMinerals = new Order(0);

    public static final Order Interrupted = new Order(0);

    public static final Order EnterTransport = new Order(0);

    public static final Order PickupIdle = new Order(0);

    public static final Order PickupTransport = new Order(0);

    public static final Order PickupBunker = new Order(0);

    public static final Order Pickup4 = new Order(0);

    public static final Order PowerupIdle = new Order(0);

    public static final Order Sieging = new Order(0);

    public static final Order Unsieging = new Order(0);

    public static final Order InitCreepGrowth = new Order(0);

    public static final Order SpreadCreep = new Order(0);

    public static final Order StoppingCreepGrowth = new Order(0);

    public static final Order GuardianAspect = new Order(0);

    public static final Order ArchonWarp = new Order(0);

    public static final Order CompletingArchonSummon = new Order(0);

    public static final Order HoldPosition = new Order(0);

    public static final Order Cloak = new Order(0);

    public static final Order Decloak = new Order(0);

    public static final Order Unload = new Order(0);

    public static final Order MoveUnload = new Order(0);

    public static final Order FireYamatoGun = new Order(0);

    public static final Order CastLockdown = new Order(0);

    public static final Order Burrowing = new Order(0);

    public static final Order Burrowed = new Order(0);

    public static final Order Unburrowing = new Order(0);

    public static final Order CastDarkSwarm = new Order(0);

    public static final Order CastParasite = new Order(0);

    public static final Order CastSpawnBroodlings = new Order(0);

    public static final Order CastEMPShockwave = new Order(0);

    public static final Order NukeWait = new Order(0);

    public static final Order NukeTrain = new Order(0);

    public static final Order NukeLaunch = new Order(0);

    public static final Order NukePaint = new Order(0);

    public static final Order NukeUnit = new Order(0);

    public static final Order CastNuclearStrike = new Order(0);

    public static final Order NukeTrack = new Order(0);

    public static final Order CloakNearbyUnits = new Order(0);

    public static final Order PlaceMine = new Order(0);

    public static final Order RightClickAction = new Order(0);

    public static final Order CastRecall = new Order(0);

    public static final Order Teleport = new Order(0);

    public static final Order CastScannerSweep = new Order(0);

    public static final Order Scanner = new Order(0);

    public static final Order CastDefensiveMatrix = new Order(0);

    public static final Order CastPsionicStorm = new Order(0);

    public static final Order CastIrradiate = new Order(0);

    public static final Order CastPlague = new Order(0);

    public static final Order CastConsume = new Order(0);

    public static final Order CastEnsnare = new Order(0);

    public static final Order CastStasisField = new Order(0);

    public static final Order CastHallucination = new Order(0);

    public static final Order Hallucination2 = new Order(0);

    public static final Order ResetCollision = new Order(0);

    public static final Order Patrol = new Order(0);

    public static final Order CTFCOPInit = new Order(0);

    public static final Order CTFCOPStarted = new Order(0);

    public static final Order CTFCOP2 = new Order(0);

    public static final Order ComputerAI = new Order(0);

    public static final Order AtkMoveEP = new Order(0);

    public static final Order HarassMove = new Order(0);

    public static final Order AIPatrol = new Order(0);

    public static final Order GuardPost = new Order(0);

    public static final Order RescuePassive = new Order(0);

    public static final Order Neutral = new Order(0);

    public static final Order ComputerReturn = new Order(0);

    public static final Order SelfDestructing = new Order(0);

    public static final Order Critter = new Order(0);

    public static final Order HiddenGun = new Order(0);

    public static final Order OpenDoor = new Order(0);

    public static final Order CloseDoor = new Order(0);

    public static final Order HideTrap = new Order(0);

    public static final Order RevealTrap = new Order(0);

    public static final Order EnableDoodad = new Order(0);

    public static final Order DisableDoodad = new Order(0);

    public static final Order WarpIn = new Order(0);

    public static final Order Medic = new Order(0);

    public static final Order MedicHeal = new Order(0);

    public static final Order HealMove = new Order(0);

    public static final Order MedicHealToIdle = new Order(0);

    public static final Order CastRestoration = new Order(0);

    public static final Order CastDisruptionWeb = new Order(0);

    public static final Order CastMindControl = new Order(0);

    public static final Order DarkArchonMeld = new Order(0);

    public static final Order CastFeedback = new Order(0);

    public static final Order CastOpticalFlare = new Order(0);

    public static final Order CastMaelstrom = new Order(0);

    public static final Order JunkYardDog = new Order(0);

    public static final Order Fatal = new Order(0);

    public static final Order None = new Order(0);

    public static final Order Unknown = new Order(0);


    private static Map<Long, Order> instances = new HashMap<Long, Order>();

    private Order(long pointer) {
        this.pointer = pointer;
    }

    private static Order get(long pointer) {
        if (pointer == 0 ) {
            return null;
        }
        Order instance = instances.get(pointer);
        if (instance == null ) {
            instance = new Order(pointer);
            instances.put(pointer, instance);
        }
        return instance;
    }

    private long pointer;

    private native String toString_native(long pointer);


}
