package l2s.gameserver.network.l2.c2s;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.threading.RunnableImpl;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Request;
import l2s.gameserver.model.Request.L2RequestType;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.World;
import l2s.gameserver.model.entity.boat.ClanAirShip;
import l2s.gameserver.model.instances.PetBabyInstance;
import l2s.gameserver.model.instances.StaticObjectInstance;
import l2s.gameserver.model.instances.residences.SiegeFlagInstance;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ActionFailPacket;
import l2s.gameserver.network.l2.s2c.ExAirShipTeleportList;
import l2s.gameserver.network.l2.s2c.ExAskCoupleAction;
import l2s.gameserver.network.l2.s2c.PrivateStoreBuyManageList;
import l2s.gameserver.network.l2.s2c.PrivateStoreManageList;
import l2s.gameserver.network.l2.s2c.RecipeShopManageListPacket;
import l2s.gameserver.network.l2.s2c.SocialActionPacket;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.tables.PetDataTable;
import l2s.gameserver.tables.PetSkillsTable;
import l2s.gameserver.utils.BotPunish;
import l2s.gameserver.utils.TradeHelper;

/**
 * packet type id 0x56
 * format:		cddc
 */
public class RequestActionUse extends L2GameClientPacket
{
	private static final Logger _log = LoggerFactory.getLogger(RequestActionUse.class);

	private int _actionId;
	private boolean _ctrlPressed;
	private boolean _shiftPressed;

	/* type:
	 * 0 - action
	 * 1 - pet action
	 * 2 - pet skill
	 * 3 - social
	 * 4 - couple social
	 * 
	 * transform:
	 * 0 для любых разрешено
	 * 1 разрешено для некоторых
	 * 2 запрещено для всех
	 */
	public static enum Action
	{
		// Действия персонажей
		ACTION0(0, 0, 0, 1), // Сесть/встать
		ACTION1(1, 0, 0, 0), // Изменить тип передвижения, шаг/бег
		ACTION7(7, 0, 0, 1), // Next Target
		ACTION10(10, 0, 0, 1), // Запрос на создание приватного магазина продажи
		ACTION28(28, 0, 0, 1), // Запрос на создание приватного магазина покупки
		ACTION37(37, 0, 0, 1), // Создание магазина Common Craft
		ACTION38(38, 0, 0, 1), // Mount
		ACTION51(51, 0, 0, 1), // Создание магазина Dwarven Craft
		ACTION61(61, 0, 0, 1), // Запрос на создание приватного магазина продажи (Package)
		ACTION96(96, 0, 0, 1), // Quit Party Command Channel?
		ACTION97(97, 0, 0, 1), // Request Party Command Channel Info?
		ACTION65(65, 0, 0, 0), // кнопка "Сообщить о боте"

		ACTION67(67, 0, 0, 1), // Steer. Allows you to control the Airship.
		ACTION68(68, 0, 0, 1), // Cancel Control. Relinquishes control of the Airship.
		ACTION69(69, 0, 0, 1), // Destination Map. Choose from pre-designated locations.
		ACTION70(70, 0, 0, 1), // Exit Airship. Disembarks from the Airship.

		// Действия петов
		ACTION15(15, 1, 0, 0), // Pet Follow
		ACTION16(16, 1, 0, 0), // Атака петом
		ACTION17(17, 1, 0, 0), // Отмена действия у пета
		ACTION19(19, 1, 0, 0), // Отзыв пета
		ACTION21(21, 1, 0, 0), // Pet Follow
		ACTION22(22, 1, 0, 0), // Атака петом
		ACTION23(23, 1, 0, 0), // Отмена действия у пета
		ACTION52(52, 1, 0, 0), // Отзыв саммона
		ACTION53(53, 1, 0, 0), // Передвинуть пета к цели
		ACTION54(54, 1, 0, 0), // Передвинуть пета к цели

		// Действия петов со скиллами
		ACTION32(32, 2, 4230, 0), // Wild Hog Cannon - Mode Change
		ACTION36(36, 2, 4259, 0), // Soulless - Toxic Smoke
		ACTION39(39, 2, 4138, 0), // Soulless - Parasite Burst
		ACTION41(41, 2, 4230, 0), // Wild Hog Cannon - Attack
		ACTION42(42, 2, 4378, 0), // Kai the Cat - Self Damage Shield
		ACTION43(43, 2, 4137, 0), // Unicorn Merrow - Hydro Screw
		ACTION44(44, 2, 4139, 0), // Big Boom - Boom Attack
		ACTION45(45, 2, 4025, 0), // Unicorn Boxer - Master Recharge
		ACTION46(46, 2, 4261, 0), // Mew the Cat - Mega Storm Strike
		ACTION47(47, 2, 4260, 0), // Silhouette - Steal Blood
		ACTION48(48, 2, 4068, 0), // Mechanic Golem - Mech. Cannon
		ACTION1000(1000, 2, 4079, 0), // Siege Golem - Siege Hammer
		//ACTION1001(1001, 2, , 0), // Sin Eater - Ultimate Bombastic Buster
		ACTION1003(1003, 2, 4710, 0), // Wind Hatchling/Strider - Wild Stun
		ACTION1004(1004, 2, 4711, 0), // Wind Hatchling/Strider - Wild Defense
		ACTION1005(1005, 2, 4712, 0), // Star Hatchling/Strider - Bright Burst
		ACTION1006(1006, 2, 4713, 0), // Star Hatchling/Strider - Bright Heal
		ACTION1007(1007, 2, 4699, 0), // Cat Queen - Blessing of Queen
		ACTION1008(1008, 2, 4700, 0), // Cat Queen - Gift of Queen
		ACTION1009(1009, 2, 4701, 0), // Cat Queen - Cure of Queen
		ACTION1010(1010, 2, 4702, 0), // Unicorn Seraphim - Blessing of Seraphim
		ACTION1011(1011, 2, 4703, 0), // Unicorn Seraphim - Gift of Seraphim
		ACTION1012(1012, 2, 4704, 0), // Unicorn Seraphim - Cure of Seraphim
		ACTION1013(1013, 2, 4705, 0), // Nightshade - Curse of Shade
		ACTION1014(1014, 2, 4706, 0), // Nightshade - Mass Curse of Shade
		ACTION1015(1015, 2, 4707, 0), // Nightshade - Shade Sacrifice
		ACTION1016(1016, 2, 4709, 0), // Cursed Man - Cursed Blow
		ACTION1017(1017, 2, 4708, 0), // Cursed Man - Cursed Strike/Stun
		ACTION1031(1031, 2, 5135, 0), // Feline King - Slash
		ACTION1032(1032, 2, 5136, 0), // Feline King - Spin Slash
		ACTION1033(1033, 2, 5137, 0), // Feline King - Hold of King
		ACTION1034(1034, 2, 5138, 0), // Magnus the Unicorn - Whiplash
		ACTION1035(1035, 2, 5139, 0), // Magnus the Unicorn - Tridal Wave
		ACTION1036(1036, 2, 5142, 0), // Spectral Lord - Corpse Kaboom
		ACTION1037(1037, 2, 5141, 0), // Spectral Lord - Dicing Death
		ACTION1038(1038, 2, 5140, 0), // Spectral Lord - Force Curse
		ACTION1039(1039, 2, 5110, 0), // Swoop Cannon - Cannon Fodder
		ACTION1040(1040, 2, 5111, 0), // Swoop Cannon - Big Bang
		ACTION1041(1041, 2, 5442, 0), // Great Wolf - 5442 - Bite Attack
		ACTION1042(1042, 2, 5444, 0), // Great Wolf - 5444 - Moul
		ACTION1043(1043, 2, 5443, 0), // Great Wolf - 5443 - Cry of the Wolf
		ACTION1044(1044, 2, 5445, 0), // Great Wolf - 5445 - Awakening 70
		ACTION1045(1045, 2, 5584, 0), // Wolf Howl
		ACTION1046(1046, 2, 5585, 0), // Strider - Roar // TODO скилл не отображается даже на 85 уровне, вероятно нужно корректировать поле type в PetInfo для страйдеров
		ACTION1047(1047, 2, 5580, 0), // Divine Beast - Bite
		ACTION1048(1048, 2, 5581, 0), // Divine Beast - Stun Attack
		ACTION1049(1049, 2, 5582, 0), // Divine Beast - Fire Breath
		ACTION1050(1050, 2, 5583, 0), // Divine Beast - Roar
		ACTION1051(1051, 2, 5638, 0), // Feline Queen - Bless The Body
		ACTION1052(1052, 2, 5639, 0), // Feline Queen - Bless The Soul
		ACTION1053(1053, 2, 5640, 0), // Feline Queen - Haste
		ACTION1054(1054, 2, 5643, 0), // Unicorn Seraphim - Acumen
		ACTION1055(1055, 2, 5647, 0), // Unicorn Seraphim - Clarity
		ACTION1056(1056, 2, 5648, 0), // Unicorn Seraphim - Empower
		ACTION1057(1057, 2, 5646, 0), // Unicorn Seraphim - Wild Magic
		ACTION1058(1058, 2, 5652, 0), // Nightshade - Death Whisper
		ACTION1059(1059, 2, 5653, 0), // Nightshade - Focus
		ACTION1060(1060, 2, 5654, 0), // Nightshade - Guidance
		ACTION1061(1061, 2, 5745, 0), // (Wild Beast Fighter, White Weasel) Death Blow - Awakens a hidden ability to inflict a powerful attack on the enemy. Requires application of the Awakening skill.
		ACTION1062(1062, 2, 5746, 0), // (Wild Beast Fighter) Double Attack - Rapidly attacks the enemy twice.
		ACTION1063(1063, 2, 5747, 0), // (Wild Beast Fighter) Spin Attack - Inflicts shock and damage to the enemy at the same time with a powerful spin attack.
		ACTION1064(1064, 2, 5748, 0), // (Wild Beast Fighter) Meteor Shower - Attacks nearby enemies with a doll heap attack.
		ACTION1065(1065, 2, 5753, 0), // (Fox Shaman, Wild Beast Fighter, White Weasel, Fairy Princess) Awakening - Awakens a hidden ability.
		ACTION1066(1066, 2, 5749, 0), // (Fox Shaman, Spirit Shaman) Thunder Bolt - Attacks the enemy with the power of thunder.
		ACTION1067(1067, 2, 5750, 0), // (Fox Shaman, Spirit Shaman) Flash - Inflicts a swift magic attack upon contacted enemies nearby.
		ACTION1068(1068, 2, 5751, 0), // (Fox Shaman, Spirit Shaman) Lightning Wave - Attacks nearby enemies with the power of lightning.
		ACTION1069(1069, 2, 5752, 0), // (Fox Shaman, Fairy Princess) Flare - Awakens a hidden ability to inflict a powerful attack on the enemy. Requires application of the Awakening skill.
		ACTION1070(1070, 2, 5771, 0), // (White Weasel, Fairy Princess, Improved Baby Buffalo, Improved Baby Kookaburra, Improved Baby Cougar) Buff Control - Controls to prevent a buff upon the master. Lasts for 5 minutes.
		ACTION1071(1071, 2, 5761, 0), // (Tigress) Power Striker - Powerfully attacks the target.
		ACTION1072(1072, 2, 6046, 0), // (Toy Knight) Piercing attack
		ACTION1073(1073, 2, 6047, 0), // (Toy Knight) Whirlwind
		ACTION1074(1074, 2, 6048, 0), // (Toy Knight) Lance Smash
		ACTION1075(1075, 2, 6049, 0), // (Toy Knight) Battle Cry
		ACTION1076(1076, 2, 6050, 0), // (Turtle Ascetic) Power Smash
		ACTION1077(1077, 2, 6051, 0), // (Turtle Ascetic) Energy Burst
		ACTION1078(1078, 2, 6052, 0), // (Turtle Ascetic) Shockwave
		ACTION1079(1079, 2, 6053, 0), // (Turtle Ascetic) Howl
		ACTION1080(1080, 2, 6041, 0), // Phoenix Rush
		ACTION1081(1081, 2, 6042, 0), // Phoenix Cleanse
		ACTION1082(1082, 2, 6043, 0), // Phoenix Flame Feather
		ACTION1083(1083, 2, 6044, 0), // Phoenix Flame Beak
		ACTION1084(1084, 2, 6054, 0), // (Spirit Shaman, Toy Knight, Turtle Ascetic) Switch State - Toggles you between Attack and Support modes.
		ACTION1086(1086, 2, 6094, 0), // Panther Cancel
		ACTION1087(1087, 2, 6095, 0), // Panther Dark Claw
		ACTION1088(1088, 2, 6096, 0), // Panther Fatal Claw
		ACTION1089(1089, 2, 6199, 0), // (Deinonychus) Tail Strike
		ACTION1090(1090, 2, 6205, 0), // (Guardian's Strider) Strider Bite
		ACTION1091(1091, 2, 6206, 0), // (Guardian's Strider) Strider Fear
		ACTION1092(1092, 2, 6207, 0), // (Guardian's Strider) Strider Dash
		ACTION1093(1093, 2, 6618, 0),
		ACTION1094(1094, 2, 6681, 0),
		ACTION1095(1095, 2, 6619, 0),
		ACTION1096(1096, 2, 6682, 0),
		ACTION1097(1097, 2, 6683, 0),
		ACTION1098(1098, 2, 6684, 0),
		ACTION5000(5000, 2, 23155, 0), // Baby Rudolph - Reindeer Scratch 
		ACTION5001(5001, 2, 23167, 0),
		ACTION5002(5002, 2, 23168, 0),
		ACTION5003(5003, 2, 5749, 0),
		ACTION5004(5004, 2, 5750, 0),
		ACTION5005(5005, 2, 5751, 0),
		ACTION5006(5006, 2, 5771, 0),
		ACTION5007(5007, 2, 6046, 0),
		ACTION5008(5008, 2, 6047, 0),
		ACTION5009(5009, 2, 6048, 0),
		ACTION5010(5010, 2, 6049, 0),
		ACTION5011(5011, 2, 6050, 0),
		ACTION5012(5012, 2, 6051, 0),
		ACTION5013(5013, 2, 6052, 0),
		ACTION5014(5014, 2, 6053, 0),
		ACTION5015(5015, 2, 6054, 0), 
		ACTION5016(5016, 2, 23318, 0), // (Super Kat/Feline/Mow Z the Cat) Skill | Instantly restores the master's HP by 10%. Increases the master's resistnace debuff attacks by 80%, resistance to buff-canceling attacks by 40% speed by 10, p. def by 20%, and m. def by 20% and decreases MP comsmption for all skills by 50% for 1 minute?

		// Социальные действия
		ACTION12(12, 3, SocialActionPacket.GREETING, 2),
		ACTION13(13, 3, SocialActionPacket.VICTORY, 2),
		ACTION14(14, 3, SocialActionPacket.ADVANCE, 2),
		ACTION24(24, 3, SocialActionPacket.YES, 2),
		ACTION25(25, 3, SocialActionPacket.NO, 2),
		ACTION26(26, 3, SocialActionPacket.BOW, 2),
		ACTION29(29, 3, SocialActionPacket.UNAWARE, 2),
		ACTION30(30, 3, SocialActionPacket.WAITING, 2),
		ACTION31(31, 3, SocialActionPacket.LAUGH, 2),
		ACTION33(33, 3, SocialActionPacket.APPLAUD, 2),
		ACTION34(34, 3, SocialActionPacket.DANCE, 2),
		ACTION35(35, 3, SocialActionPacket.SORROW, 2),
		ACTION62(62, 3, SocialActionPacket.CHARM, 2),
		ACTION66(66, 3, SocialActionPacket.SHYNESS, 2),
		//TAUTI
		ACTION87(87, 3, SocialActionPacket.PROPOSE, 2),
		ACTION88(88, 3, SocialActionPacket.PROVOKE, 2),
		ACTION89(89, 3, SocialActionPacket.BOASTING, 2),

		// Парные социальные действия
		ACTION71(71, 4, SocialActionPacket.COUPLE_BOW, 2),
		ACTION72(72, 4, SocialActionPacket.COUPLE_HIGH_FIVE, 2),
		ACTION73(73, 4, SocialActionPacket.COUPLE_DANCE, 2);

		public int id;
		public int type;
		public int value;
		public int transform;

		private Action(int id, int type, int value, int transform)
		{
			this.id = id;
			this.type = type;
			this.value = value;
			this.transform = transform;
		}

		public static Action find(int id)
		{
			for(Action action : Action.values())
				if(action.id == id)
					return action;
			return null;
		}
	}

	@Override
	protected void readImpl()
	{
		_actionId = readD();
		_ctrlPressed = readD() == 1;
		_shiftPressed = readC() == 1;
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		Action action = Action.find(_actionId);
		if(action == null)
		{
			_log.warn("unhandled action type " + _actionId + " by player " + activeChar.getName());
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isHFClient())
		{
			if(action.id == 87 || action.id == 88 || action.id == 89)
			{
				activeChar.sendActionFailed();
				return;
			}
		}

		if(Config.ALT_ENABLE_BOTREPORT)
		{
			// Check if has any bot punishment
			if(activeChar.isBeingPunished())
			{
				// Remove punishment if finished
				if(activeChar.getPlayerPunish().canPerformAction() && activeChar.getBotPunishType() == BotPunish.Punish.ACTIONBAN)
					activeChar.endPunishment();
				// Else apply it
				else if(activeChar.getBotPunishType() == BotPunish.Punish.ACTIONBAN)
				{
					SystemMsg msg;
					switch(activeChar.getPlayerPunish().getDuration())
					{
						case 7200:
							msg = SystemMsg.REPORTED_120_MINS_WITHOUT_ACTIONS;
							break;
						case 10800:
							msg = SystemMsg.REPORTED_180_MINS_WITHOUT_ACTIONS;
							break;
						default:
							msg = SystemMsg.NOTHING_HAPPENED;
							break;
					}
					activeChar.sendPacket(msg);
					return;
				}
			}
		}

		boolean usePet = action.type == 1 || action.type == 2;

		// dont do anything if player is dead or confused
		if(!usePet && (activeChar.isOutOfControl() || activeChar.isActionsDisabled()) && !(activeChar.isFakeDeath() && _actionId == 0))
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.getTransformation() != 0 && action.transform > 0) // TODO разрешить для некоторых трансформ
		{
			activeChar.sendActionFailed();
			return;
		}

		// Социальные действия
		if(action.type == 3)
		{
			if(activeChar.isOutOfControl() || activeChar.getTransformation() != 0 || activeChar.isActionsDisabled() || activeChar.isSitting() || activeChar.getPrivateStoreType() != Player.STORE_PRIVATE_NONE || activeChar.isProcessingRequest())
			{
				activeChar.sendActionFailed();
				return;
			}
			if(activeChar.isFishing())
			{
				activeChar.sendPacket(SystemMsg.YOU_CANNOT_DO_THAT_WHILE_FISHING_2);
				return;
			}
			activeChar.broadcastPacket(new SocialActionPacket(activeChar.getObjectId(), action.value));
			activeChar.addValue(action.value);
			if(Config.ALT_SOCIAL_ACTION_REUSE)
			{
				ThreadPoolManager.getInstance().schedule(new SocialTask(activeChar), 2600);
				activeChar.startParalyzed();
			}
			return;
		}

		final GameObject target = activeChar.getTarget();
		// Парные социальные действия
		if(action.type == 4)
		{
			if(activeChar.isOutOfControl() || activeChar.isActionsDisabled() || activeChar.isSitting())
			{
				activeChar.sendActionFailed();
				return;
			}
			if(target == null || !target.isPlayer())
			{
				activeChar.sendActionFailed();
				return;
			}
			final Player pcTarget = target.getPlayer();
			if(pcTarget.isProcessingRequest() && pcTarget.getRequest().isTypeOf(L2RequestType.COUPLE_ACTION))
			{
				activeChar.sendPacket(new SystemMessagePacket(SystemMsg.C1_IS_ALREADY_PARTICIPATING_IN_A_COUPLE_ACTION_AND_CANNOT_BE_REQUESTED_FOR_ANOTHER_COUPLE_ACTION).addName(pcTarget));
				return;
			}
			if(pcTarget.isProcessingRequest())
			{
				activeChar.sendPacket(new SystemMessagePacket(SystemMsg.C1_IS_ON_ANOTHER_TASK).addName(pcTarget));
				return;
			}
			if(!activeChar.isInRange(pcTarget, 300) || activeChar.isInRange(pcTarget, 25) || activeChar.getTargetId() == activeChar.getObjectId() || !GeoEngine.canSeeTarget(activeChar, pcTarget))
			{
				activeChar.sendPacket(SystemMsg.THE_REQUEST_CANNOT_BE_COMPLETED_BECAUSE_THE_TARGET_DOES_NOT_MEET_LOCATION_REQUIREMENTS);
				return;
			}
			if(!activeChar.checkCoupleAction(pcTarget))
				return;

			new Request(L2RequestType.COUPLE_ACTION, activeChar, pcTarget).setTimeout(10000L);
			activeChar.sendPacket(new SystemMessagePacket(SystemMsg.YOU_HAVE_REQUESTED_A_COUPLE_ACTION_WITH_C1).addName(pcTarget));
			pcTarget.sendPacket(new ExAskCoupleAction(activeChar.getObjectId(), action.value));

			if(Config.ALT_SOCIAL_ACTION_REUSE)
			{
				ThreadPoolManager.getInstance().schedule(new SocialTask(activeChar), 2600);
				activeChar.startParalyzed();
			}
			return;
		}

		final Servitor servitor = activeChar.getServitor();
		if(usePet)
		{
			if(servitor == null || servitor.isOutOfControl())
			{
				activeChar.sendActionFailed();
				return;
			}
			if(servitor.isDepressed())
			{
				activeChar.sendPacket(SystemMsg.YOUR_PETSERVITOR_IS_UNRESPONSIVE_AND_WILL_NOT_OBEY_ANY_ORDERS);
				return;
			}
		}

		// Скиллы петов
		if(action.type == 2)
		{
			// TODO перенести эти условия в скиллы
			if(action.id == 1000 && target != null && !target.isDoor()) // Siege Golem - Siege Hammer
			{
				activeChar.sendActionFailed();
				return;
			}
			if(target != null)
			{
				if((action.id == 1039 || action.id == 1040) && (target.isDoor() || target instanceof SiegeFlagInstance)) // Swoop Cannon (не может атаковать двери и флаги)
				{
					activeChar.sendActionFailed();
					return;
				}
			}	
			UseSkill(action.value);
			return;
		}

		switch(action.id)
		{
			// Действия с игроками:

			case 0: // Сесть/встать
				// На страйдере нельзя садиться
				if(activeChar.isMounted())
				{
					activeChar.sendActionFailed();
					break;
				}

				if(activeChar.isFakeDeath())
				{
					activeChar.breakFakeDeath();
					activeChar.updateEffectIcons();
					break;
				}

				if(activeChar.getSittingTask())
				{
					activeChar.sendActionFailed();
					return;
				}

				if(!activeChar.isSitting())
				{
					if(target != null && target instanceof StaticObjectInstance && ((StaticObjectInstance) target).getType() == 1 && activeChar.checkInteractionDistance(target))
						activeChar.sitDown((StaticObjectInstance)target);
					else
						activeChar.sitDown(null);
				}
				else
					activeChar.standUp();

				break;
			case 1: // Изменить тип передвижения, шаг/бег
				if(activeChar.isRunning())
					activeChar.setWalking();
				else
					activeChar.setRunning();
				break;
			case 7: // Next Target
				Creature nearest_target = null;
				for(Creature cha : World.getAroundCharacters(activeChar, 400, 200))
				{
					if(cha != null && !cha.isAlikeDead())
					{
						if((nearest_target == null || activeChar.getDistance3D(cha) < activeChar.getDistance3D(nearest_target)) && cha.isAutoAttackable(activeChar))
						{
							nearest_target = cha;
						}
					}
				}
				if(nearest_target != null && activeChar.getTarget() != nearest_target)
				{
					activeChar.setTarget(nearest_target);
					return;
				}
				break;
			case 10: // Запрос на создание приватного магазина продажи
			case 61: // Запрос на создание приватного магазина продажи (Package)
			{
				if(activeChar.getSittingTask())
				{
					activeChar.sendActionFailed();
					return;
				}
				if(activeChar.getTransformation() != 0)
				{
					activeChar.sendActionFailed();
					return;
				}
				if(activeChar.isInStoreMode())
				{
					activeChar.setPrivateStoreType(Player.STORE_PRIVATE_NONE);
					activeChar.standUp();
					activeChar.broadcastCharInfo();
				}
				else if(!TradeHelper.checksIfCanOpenStore(activeChar, _actionId == 61 ? Player.STORE_PRIVATE_SELL_PACKAGE : Player.STORE_PRIVATE_SELL))
				{
					activeChar.sendActionFailed();
					return;
				}
				activeChar.sendPacket(new PrivateStoreManageList(1, activeChar, _actionId == 61));
				activeChar.sendPacket(new PrivateStoreManageList(2, activeChar, _actionId == 61));
				break;
			}
			case 28: // Запрос на создание приватного магазина покупки
			{
				if(activeChar.getSittingTask())
				{
					activeChar.sendActionFailed();
					return;
				}
				if(activeChar.getTransformation() != 0)
				{
					activeChar.sendActionFailed();
					return;
				}
				if(activeChar.isInStoreMode())
				{
					activeChar.setPrivateStoreType(Player.STORE_PRIVATE_NONE);
					activeChar.standUp();
					activeChar.broadcastCharInfo();
				}
				else if(!TradeHelper.checksIfCanOpenStore(activeChar, Player.STORE_PRIVATE_BUY))
				{
					activeChar.sendActionFailed();
					return;
				}
				activeChar.sendPacket(new PrivateStoreBuyManageList(1, activeChar));
				activeChar.sendPacket(new PrivateStoreBuyManageList(2, activeChar));
				break;
			}
			case 37: // Создание магазина Dwarven Craft
			{
				if(activeChar.getSittingTask())
				{
					activeChar.sendActionFailed();
					return;
				}
				if(activeChar.isInStoreMode())
				{
					activeChar.setPrivateStoreType(Player.STORE_PRIVATE_NONE);
					activeChar.standUp();
					activeChar.broadcastCharInfo();
				}
				else if(!TradeHelper.checksIfCanOpenStore(activeChar, Player.STORE_PRIVATE_MANUFACTURE))
				{
					activeChar.sendActionFailed();
					return;
				}
				activeChar.sendPacket(new RecipeShopManageListPacket(activeChar, true));
				break;
			}
			case 51: // Создание магазина Common Craft
			{
				if(activeChar.getSittingTask())
				{
					activeChar.sendActionFailed();
					return;
				}
				if(activeChar.isInStoreMode())
				{
					activeChar.setPrivateStoreType(Player.STORE_PRIVATE_NONE);
					activeChar.standUp();
					activeChar.broadcastCharInfo();
				}
				else if(!TradeHelper.checksIfCanOpenStore(activeChar, Player.STORE_PRIVATE_MANUFACTURE))
				{
					activeChar.sendActionFailed();
					return;
				}
				activeChar.sendPacket(new RecipeShopManageListPacket(activeChar, false));
				break;
			}
			case 96: // Quit Party Command Channel?
				_log.info("96 Accessed");
				break;
			case 97: // Request Party Command Channel Info?
				_log.info("97 Accessed");
				break;

			// Действия с петами:

			case 15:
			case 21: // Follow для пета
				if(servitor != null && !servitor.isAfraid())
					servitor.setFollowMode(!servitor.isFollowMode());
				break;
			case 16:
			case 22: // Атака петом

				if(target == null || !target.isCreature() || servitor == target || servitor.isDead())
				{
					activeChar.sendActionFailed();
					return;
				}

				if(activeChar.isInOlympiadMode() && !activeChar.isOlympiadCompStart())
				{
					activeChar.sendActionFailed();
					return;
				}

				// Sin Eater
				if(servitor.getTemplate().getNpcId() == PetDataTable.SIN_EATER_ID)
					return;

				if(!_ctrlPressed && target.isCreature() && !((Creature) target).isAutoAttackable(servitor))
					return;

				if(_ctrlPressed && !target.isAttackable(servitor))
				{
					activeChar.sendPacket(SystemMsg.INVALID_TARGET);
					return;
				}
				if(!target.isMonster() && (servitor.isInZonePeace() || target.isCreature() && ((Creature) target).isInZonePeace()))
				{
					activeChar.sendPacket(SystemMsg.YOU_MAY_NOT_ATTACK_THIS_TARGET_IN_A_PEACEFUL_ZONE);
					return;
				}

				if(activeChar.getLevel() + 20 <= servitor.getLevel())
				{
					activeChar.sendPacket(SystemMsg.YOUR_PET_IS_TOO_HIGH_LEVEL_TO_CONTROL);
					return;
				}
				if(!_ctrlPressed && target == activeChar)
					return;
					
				if(!Config.ALLOW_PET_ATTACK_MASTER && target == activeChar)
					return;
				if(Config.DISALLOW_PET_ACTIONS_IF_MASTER_DEAD && activeChar.isDead())
				{
					activeChar.sendActionFailed();
					return;		
				}
				
				servitor.getAI().Attack(target, _ctrlPressed, _shiftPressed);
				break;
			case 17:
			case 23: // Отмена действия у пета
				servitor.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				break;
			case 19: // Отзыв пета
				if(servitor.isDead())
				{
					activeChar.sendPacket(SystemMsg.DEAD_PETS_CANNOT_BE_RETURNED_TO_THEIR_SUMMONING_ITEM, ActionFailPacket.STATIC);
					return;
				}

				if(servitor.isInCombat())
				{
					activeChar.sendPacket(SystemMsg.A_PET_CANNOT_BE_UNSUMMONED_DURING_BATTLE, ActionFailPacket.STATIC);
					break;
				}

				if(!PetDataTable.isSpecialPet(servitor.getNpcId()) && servitor.isPet() && servitor.getCurrentFed() < 0.55 * servitor.getMaxFed())
				{
					activeChar.sendPacket(SystemMsg.YOU_MAY_NOT_RESTORE_A_HUNGRY_PET, ActionFailPacket.STATIC);
					break;
				}
				if(Config.DISALLOW_PET_ACTIONS_IF_MASTER_DEAD && activeChar.isDead())
				{
					activeChar.sendActionFailed();
					return;		
				}
				servitor.unSummon();
				break;
			case 38: // Mount
				if(activeChar.getTransformation() != 0)
					activeChar.sendPacket(SystemMsg.YOU_CANNOT_BOARD_BECAUSE_YOU_DO_NOT_MEET_THE_REQUIREMENTS);
				else if(servitor == null || !servitor.isMountable())
				{
					if(activeChar.isMounted())
					{
						if(activeChar.isFlying() && !activeChar.checkLandingState()) // Виверна
						{
							activeChar.sendPacket(Msg.YOU_ARE_NOT_ALLOWED_TO_DISMOUNT_AT_THIS_LOCATION, ActionFailPacket.STATIC);
							return;
						}
						activeChar.setMount(0, 0, 0);
					}
				}
				else if(activeChar.isMounted() || activeChar.isInBoat())
					activeChar.sendPacket(Msg.YOU_CANNOT_MOUNT_BECAUSE_YOU_DO_NOT_MEET_THE_REQUIREMENTS);
				else if(activeChar.isDead())
					activeChar.sendPacket(Msg.YOU_CANNOT_MOUNT_BECAUSE_YOU_DO_NOT_MEET_THE_REQUIREMENTS);
				else if(servitor.isDead())
					activeChar.sendPacket(Msg.A_DEAD_PET_CANNOT_BE_RIDDEN);
				else if(activeChar.isInDuel())
					activeChar.sendPacket(Msg.YOU_CANNOT_MOUNT_BECAUSE_YOU_DO_NOT_MEET_THE_REQUIREMENTS);
				else if(activeChar.isInCombat() || servitor.isInCombat())
					activeChar.sendPacket(Msg.YOU_CANNOT_MOUNT_BECAUSE_YOU_DO_NOT_MEET_THE_REQUIREMENTS);
				else if(activeChar.isFishing())
					activeChar.sendPacket(Msg.YOU_CANNOT_MOUNT_BECAUSE_YOU_DO_NOT_MEET_THE_REQUIREMENTS);
				else if(activeChar.isSitting())
					activeChar.sendPacket(Msg.YOU_CANNOT_MOUNT_BECAUSE_YOU_DO_NOT_MEET_THE_REQUIREMENTS);
				else if(activeChar.isCursedWeaponEquipped())
					activeChar.sendPacket(Msg.YOU_CANNOT_MOUNT_BECAUSE_YOU_DO_NOT_MEET_THE_REQUIREMENTS);
				else if(activeChar.getActiveWeaponFlagAttachment() != null)
					activeChar.sendPacket(Msg.YOU_CANNOT_MOUNT_BECAUSE_YOU_DO_NOT_MEET_THE_REQUIREMENTS);
				else if(activeChar.isCastingNow())
					activeChar.sendPacket(Msg.YOU_CANNOT_MOUNT_BECAUSE_YOU_DO_NOT_MEET_THE_REQUIREMENTS);
				else if(activeChar.isParalyzed())
					activeChar.sendPacket(Msg.YOU_CANNOT_MOUNT_BECAUSE_YOU_DO_NOT_MEET_THE_REQUIREMENTS);
				else
				{
					activeChar.getEffectList().stopEffect(Skill.SKILL_EVENT_TIMER);
					activeChar.setMount(servitor.getTemplate().npcId, servitor.getObjectId(), servitor.getLevel());
					servitor.unSummon();
				}
				break;
			case 52: // Отзыв саммона
				if(servitor.isInCombat())
				{
					activeChar.sendPacket(SystemMsg.A_PET_CANNOT_BE_UNSUMMONED_DURING_BATTLE);
					activeChar.sendActionFailed();
				}
				else if(servitor.isDead())
				{
					activeChar.sendPacket(SystemMsg.DEAD_PETS_CANNOT_BE_RETURNED_TO_THEIR_SUMMONING_ITEM);
					activeChar.sendActionFailed();				
				}
				else
				{
					servitor.saveEffects();
					servitor.unSummon();
				}
				break;
			case 53:
			case 54: // Передвинуть пета к цели
				if(Config.DISALLOW_PET_ACTIONS_IF_MASTER_DEAD && activeChar.isDead())
				{
					activeChar.sendActionFailed();
					return;		
				}			
				else if(target != null && servitor != target && !servitor.isMovementDisabled() && !servitor.isAfraid())
				{
					servitor.setFollowMode(false);
					servitor.moveToLocation(target.getLoc(), 100, true);
				}
				break;
			case 1070:
				if(servitor instanceof PetBabyInstance)
					((PetBabyInstance) servitor).triggerBuff();
				break;
			case 65: // Кнопка "Сообщить о боте"
				if(Config.ALT_ENABLE_BOTREPORT)
				{
					if(activeChar.getTarget() == null)
						return;
					if(activeChar.getTarget().isPlayer())
						l2s.gameserver.handler.voicecommands.impl.BotReport.CaptchaHandler.tryReportPlayer(activeChar, activeChar.getTarget().getPlayer());
					else
						activeChar.sendMessage("Ваша цель не является игроком!"); // FIXME: CustomMessages
				}
				else
					activeChar.sendMessage("Действие отключено.");
				// Месага верная (c) PaInKiLlEr
				break;
			case 67: // Steer. Allows you to control the Airship.
				if(activeChar.isInBoat() && activeChar.getBoat().isClanAirShip() && !activeChar.getBoat().isMoving)
				{
					ClanAirShip boat = (ClanAirShip) activeChar.getBoat();
					if(boat.getDriver() == null)
						boat.setDriver(activeChar);
					else
						activeChar.sendPacket(SystemMsg.ANOTHER_PLAYER_IS_PROBABLY_CONTROLLING_THE_TARGET);
				}
				break;
			case 68: // Cancel Control. Relinquishes control of the Airship.
				if(activeChar.isClanAirShipDriver())
				{
					ClanAirShip boat = (ClanAirShip) activeChar.getBoat();
					boat.setDriver(null);
					activeChar.broadcastCharInfo();
				}
				break;
			case 69: // Destination Map. Choose from pre-designated locations.
				if(activeChar.isClanAirShipDriver() && activeChar.getBoat().isDocked())
					activeChar.sendPacket(new ExAirShipTeleportList((ClanAirShip) activeChar.getBoat()));
				break;
			case 70: // Exit Airship. Disembarks from the Airship.
				if(activeChar.isInBoat() && activeChar.getBoat().isAirShip() && activeChar.getBoat().isDocked())
					activeChar.getBoat().oustPlayer(activeChar, activeChar.getBoat().getReturnLoc(), true);
				break;
			case 1001:
				break;

			default:
				_log.warn("unhandled action type " + _actionId + " by player " + activeChar.getName());
		}
		activeChar.sendActionFailed();
	}

	private void UseSkill(int skillId)
	{
		Player activeChar = getClient().getActiveChar();
		Servitor servitor = activeChar.getServitor();
		if(servitor == null)
		{
			activeChar.sendActionFailed();
			return;
		}
		
		if(Config.DISALLOW_PET_ACTIONS_IF_MASTER_DEAD && activeChar.isDead())
		{
			activeChar.sendActionFailed();
			return;		
		}
		
		int skillLevel = PetSkillsTable.getInstance().getAvailableLevel(servitor, skillId);
		if(skillLevel == 0)
		{
			activeChar.sendActionFailed();
			return;
		}

		Skill skill = SkillHolder.getInstance().getSkill(skillId, skillLevel);
		if(skill == null)
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.getLevel() + 20 <= servitor.getLevel())
		{
			activeChar.sendPacket(SystemMsg.YOUR_PET_IS_TOO_HIGH_LEVEL_TO_CONTROL);
			return;
		}
		Creature aimingTarget = skill.getAimingTarget(servitor, activeChar.getTarget());
		if(skill.checkCondition(servitor, aimingTarget, _ctrlPressed, _shiftPressed, true))
		{	
			servitor.getAI().Cast(skill, aimingTarget, _ctrlPressed, _shiftPressed);
		}	
		else
		{
			activeChar.sendActionFailed();
		}	
	}

	static class SocialTask extends RunnableImpl
	{
		Player _player;

		SocialTask(Player player)
		{
			_player = player;
		}

		@Override
		public void runImpl() throws Exception
		{
			_player.stopParalyzed();
		}
	}
}