package jnibwapi.types;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a StarCraft order type.
 * 
 * For a description of fields see: http://code.google.com/p/bwapi/wiki/Order
 */
public class OrderType {
	
	private static Map<Integer, OrderType> idToOrderType = new HashMap<>();
	
	public static class OrderTypes {
		public static final OrderType Die = new OrderType(0);
		public static final OrderType Stop = new OrderType(1);
		public static final OrderType Guard = new OrderType(2);
		public static final OrderType PlayerGuard = new OrderType(3);
		public static final OrderType TurretGuard = new OrderType(4);
		public static final OrderType BunkerGuard = new OrderType(5);
		public static final OrderType Move = new OrderType(6);
		// public static final OrderType ReaverStop = new OrderType(7); // Unused
		/** Generic attack order */
		public static final OrderType Attack = new OrderType(8);
		/** Attack building in fog */
		public static final OrderType AttackShrouded = new OrderType(9);
		public static final OrderType AttackUnit = new OrderType(10);
		// public static final OrderType AttackFixedRange = new OrderType(11); // Unused
		public static final OrderType AttackTile = new OrderType(12);
		public static final OrderType Hover = new OrderType(13);
		public static final OrderType AttackMove = new OrderType(14);
		public static final OrderType InfestedCommandCenter = new OrderType(15);
		public static final OrderType UnusedNothing = new OrderType(16);
		public static final OrderType UnusedPowerup = new OrderType(17);
		public static final OrderType TowerGuard = new OrderType(18);
		public static final OrderType FailedCasting = new OrderType(19);
		public static final OrderType VultureMine = new OrderType(20);
		// public static final OrderType StayInRange = new OrderType(21); // Unused
		// public static final OrderType TurretAttack = new OrderType(22); // Unused
		public static final OrderType Nothing = new OrderType(23);
		public static final OrderType Nothing3 = new OrderType(24);
		// public static final OrderType DroneStartBuild = new OrderType(25); // Unused
		// public static final OrderType DroneBuild = new OrderType(26); // Unused
		public static final OrderType CastInfestation = new OrderType(27);
		public static final OrderType InfestingCommandCenter = new OrderType(29);
		public static final OrderType PlaceBuilding = new OrderType(30);
		public static final OrderType BuildProtoss2 = new OrderType(32);
		public static final OrderType ConstructingBuilding = new OrderType(33);
		public static final OrderType Repair = new OrderType(34);
		// public static final OrderType RepairMove = new OrderType(35); // Unused
		public static final OrderType PlaceAddon = new OrderType(36);
		public static final OrderType BuildAddon = new OrderType(37);
		public static final OrderType Train = new OrderType(38);
		public static final OrderType RallyPointUnit = new OrderType(39);
		public static final OrderType RallyPointTile = new OrderType(40);
		public static final OrderType ZergBirth = new OrderType(41);
		public static final OrderType ZergUnitMorph = new OrderType(42);
		public static final OrderType ZergBuildingMorph = new OrderType(43);
		public static final OrderType IncompleteBuilding = new OrderType(44);
		public static final OrderType BuildNydusExit = new OrderType(46);
		public static final OrderType EnterNydusCanal = new OrderType(47);
		// public static final OrderType ZergUnitMorph2 = new OrderType(48); // Unused
		public static final OrderType Follow = new OrderType(49);
		public static final OrderType Carrier = new OrderType(50);
		public static final OrderType ReaverCarrierMove = new OrderType(51);
		// public static final OrderType CarrierStop = new OrderType(52); // Unused
		public static final OrderType CarrierAttack1 = new OrderType(53);
		public static final OrderType CarrierAttack2 = new OrderType(54);
		// public static final OrderType CarrierFight = new OrderType(56); // Unused
		public static final OrderType CarrierIgnore2 = new OrderType(55);
		public static final OrderType Reaver = new OrderType(58);
		public static final OrderType ReaverAttack1 = new OrderType(59);
		public static final OrderType ReaverAttack2 = new OrderType(60);
		// public static final OrderType ReaverFight = new OrderType(61); // Unused
		// public static final OrderType ReaverHoldPosition = new OrderType(62); // Unused
		public static final OrderType TrainFighter = new OrderType(63); // Interceptor / Scarab ?
		public static final OrderType InterceptorAttack = new OrderType(64);
		public static final OrderType ScarabAttack = new OrderType(65);
		public static final OrderType RechargeShieldsUnit = new OrderType(66);
		public static final OrderType RechargeShieldsBattery = new OrderType(67);
		public static final OrderType ShieldBattery = new OrderType(68);
		public static final OrderType InterceptorReturn = new OrderType(69);
		// public static final OrderType DroneLand = new OrderType(70); // Unused
		public static final OrderType BuildingLand = new OrderType(71);
		public static final OrderType BuildingLiftOff = new OrderType(72);
		public static final OrderType DroneLiftOff = new OrderType(73);
		public static final OrderType LiftingOff = new OrderType(74);
		public static final OrderType ResearchTech = new OrderType(75);
		public static final OrderType Upgrade = new OrderType(76);
		public static final OrderType Larva = new OrderType(77);
		public static final OrderType SpawningLarva = new OrderType(78);
		public static final OrderType Harvest1 = new OrderType(79);
		public static final OrderType Harvest2 = new OrderType(80);
		/** Unit is moving to refinery */
		public static final OrderType MoveToGas = new OrderType(81);
		/** Unit is waiting to enter the refinery (another unit is currently in it) */
		public static final OrderType WaitForGas = new OrderType(82);
		/** Unit is in refinery */
		public static final OrderType HarvestGas = new OrderType(83);
		/** Unit is returning gas to center */
		public static final OrderType ReturnGas = new OrderType(84);
		/** Unit is moving to mineral patch */
		public static final OrderType MoveToMinerals = new OrderType(85);
		/** Unit is waiting to use the mineral patch (another unit is currently mining from it) */
		public static final OrderType WaitForMinerals = new OrderType(86);
		/** Unit is mining minerals from mineral patch */
		public static final OrderType MiningMinerals = new OrderType(87);
		public static final OrderType Harvest3 = new OrderType(88);
		public static final OrderType Harvest4 = new OrderType(89);
		/** Unit is returning minerals to center */
		public static final OrderType ReturnMinerals = new OrderType(90);
		public static final OrderType Interrupted = new OrderType(91);
		public static final OrderType EnterTransport = new OrderType(92);
		public static final OrderType PickupIdle = new OrderType(93);
		public static final OrderType PickupTransport = new OrderType(94);
		public static final OrderType PickupBunker = new OrderType(95);
		public static final OrderType Pickup4 = new OrderType(96);
		public static final OrderType PowerupIdle = new OrderType(97);
		public static final OrderType Sieging = new OrderType(98);
		public static final OrderType Unsieging = new OrderType(99);
		// public static final OrderType WatchTarget = new OrderType(100); // Unused
		public static final OrderType InitCreepGrowth = new OrderType(101);
		public static final OrderType SpreadCreep = new OrderType(102);
		public static final OrderType StoppingCreepGrowth = new OrderType(103);
		public static final OrderType GuardianAspect = new OrderType(104);
		public static final OrderType ArchonWarp = new OrderType(105);
		public static final OrderType CompletingArchonsummon = new OrderType(106);
		public static final OrderType HoldPosition = new OrderType(107);
		public static final OrderType Cloak = new OrderType(109);
		public static final OrderType Decloak = new OrderType(110);
		public static final OrderType Unload = new OrderType(111);
		public static final OrderType MoveUnload = new OrderType(112);
		public static final OrderType FireYamatoGun = new OrderType(113);
		public static final OrderType MoveToFireYamatoGun = new OrderType(114);
		public static final OrderType CastLockdown = new OrderType(115);
		public static final OrderType Burrowing = new OrderType(116);
		public static final OrderType Burrowed = new OrderType(117);
		public static final OrderType Unburrowing = new OrderType(118);
		public static final OrderType CastDarkSwarm = new OrderType(119);
		public static final OrderType CastParasite = new OrderType(120);
		public static final OrderType CastSpawnBroodlings = new OrderType(121);
		public static final OrderType CastEMPShockwave = new OrderType(122);
		public static final OrderType NukeWait = new OrderType(123);
		public static final OrderType NukeTrain = new OrderType(124);
		public static final OrderType NukeLaunch = new OrderType(125);
		public static final OrderType NukePaint = new OrderType(126);
		public static final OrderType NukeUnit = new OrderType(127);
		public static final OrderType CastNuclearStrike = new OrderType(128);
		public static final OrderType NukeTrack = new OrderType(129);
		// public static final OrderType InitArbiter = new OrderType(130); // Unused
		public static final OrderType CloakNearbyUnits = new OrderType(131);
		public static final OrderType PlaceMine = new OrderType(132);
		public static final OrderType RightClickAction = new OrderType(133);
		// public static final OrderType SapUnit = new OrderType(134); // Unused
		// public static final OrderType SapLocation = new OrderType(135); // Unused
		// public static final OrderType SuicideHoldPosition = new OrderType(136); // Unused
		public static final OrderType CastRecall = new OrderType(137);
		public static final OrderType TeleporttoLocation = new OrderType(138);
		public static final OrderType CastScannerSweep = new OrderType(139);
		public static final OrderType Scanner = new OrderType(140);
		public static final OrderType CastDefensiveMatrix = new OrderType(141);
		public static final OrderType CastPsionicStorm = new OrderType(142);
		public static final OrderType CastIrradiate = new OrderType(143);
		public static final OrderType CastPlague = new OrderType(144);
		public static final OrderType CastConsume = new OrderType(145);
		public static final OrderType CastEnsnare = new OrderType(146);
		public static final OrderType CastStasisField = new OrderType(147);
		public static final OrderType CastHallucination = new OrderType(148);
		public static final OrderType Hallucination2 = new OrderType(149);
		public static final OrderType ResetCollision = new OrderType(150);
		public static final OrderType Patrol = new OrderType(152);
		public static final OrderType CTFCOPInit = new OrderType(153);
		public static final OrderType CTFCOP1 = new OrderType(154);
		public static final OrderType CTFCOP2 = new OrderType(155);
		public static final OrderType ComputerAI = new OrderType(156);
		public static final OrderType AtkMoveEP = new OrderType(157);
		public static final OrderType HarassMove = new OrderType(158);
		public static final OrderType AIPatrol = new OrderType(159);
		public static final OrderType GuardPost = new OrderType(160);
		public static final OrderType RescuePassive = new OrderType(161);
		public static final OrderType Neutral = new OrderType(162);
		public static final OrderType ComputerReturn = new OrderType(163);
		// public static final OrderType InitPsiProvider = new OrderType(164); // Unused
		public static final OrderType SelfDestrucing = new OrderType(165);
		public static final OrderType Critter = new OrderType(166);
		public static final OrderType HiddenGun = new OrderType(167);
		public static final OrderType OpenDoor = new OrderType(168);
		public static final OrderType CloseDoor = new OrderType(169);
		public static final OrderType HideTrap = new OrderType(170);
		public static final OrderType RevealTrap = new OrderType(171);
		public static final OrderType Enabledoodad = new OrderType(172);
		public static final OrderType Disabledoodad = new OrderType(173);
		public static final OrderType Warpin = new OrderType(174);
		public static final OrderType Medic = new OrderType(175);
		public static final OrderType MedicHeal1 = new OrderType(176);
		public static final OrderType HealMove = new OrderType(177);
		public static final OrderType MedicHeal2 = new OrderType(179);
		public static final OrderType CastRestoration = new OrderType(180);
		public static final OrderType CastDisruptionWeb = new OrderType(181);
		public static final OrderType CastMindControl = new OrderType(182);
		public static final OrderType DarkArchonMeld = new OrderType(183);
		public static final OrderType CastFeedback = new OrderType(184);
		public static final OrderType CastOpticalFlare = new OrderType(185);
		public static final OrderType CastMaelstrom = new OrderType(186);
		public static final OrderType JunkYardDog = new OrderType(187);
		public static final OrderType Fatal = new OrderType(188);
		public static final OrderType None = new OrderType(189);
		public static final OrderType Unknown = new OrderType(190);
		
		public static OrderType getOrderType(int id) {
			return idToOrderType.get(id);
		}
		
		public static Collection<OrderType> getAllOrderTypes() {
			return Collections.unmodifiableCollection(idToOrderType.values());
		}
	}
	
	public static final int numAttributes = 1;
	
	private String name;
	private int ID;
	
	private OrderType(int ID) {
		this.ID = ID;
		idToOrderType.put(ID, this);
	}
	
	public void initialize(int[] data, int index, String name) {
		if (ID != data[index++])
			throw new IllegalArgumentException();
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public int getID() {
		return ID;
	}
	
	@Override
	public String toString() {
		return getName() + " (" + getID() + ")";
	}
	
}
