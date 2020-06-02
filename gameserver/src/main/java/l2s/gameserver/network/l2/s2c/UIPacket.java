package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.NpcHolder;
import l2s.gameserver.instancemanager.CursedWeaponsManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.base.Element;
import l2s.gameserver.model.base.Experience;
import l2s.gameserver.model.base.TeamType;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.entity.events.impl.AbstractFightClub;
import l2s.gameserver.model.entity.events.impl.DominionSiegeEvent;
import l2s.gameserver.model.items.Inventory;
import l2s.gameserver.model.matching.MatchingRoom;
import l2s.gameserver.model.pledge.Alliance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.s2c.updatetype.UserInfoType;
import l2s.gameserver.skills.effects.EffectCubic;
import l2s.gameserver.utils.Location;

/**
 * @reworked by Bonux
 */
public class UIPacket extends AbstractMaskPacket<UserInfoType>
{
	public static final int USER_RELATION_PARTY_MEMBER = 0x08;
	public static final int USER_RELATION_PARTY_LEADER = 0x10;
	public static final int USER_RELATION_CLAN_MEMBER = 0x20;
	public static final int USER_RELATION_CLAN_LEADER = 0x40;
	public static final int USER_RELATION_IN_SIEGE = 0x80;
	public static final int USER_RELATION_ATTACKER = 0x100;
	public static final int USER_RELATION_IN_DOMINION_WAR = 0x1000;

	private boolean _canWrite = false, partyRoom;
	private int _runSpd, _walkSpd, _swimRunSpd, _swimWalkSpd, _flRunSpd, _flWalkSpd, _flyRunSpd, _flyWalkSpd, _relation;
	private double move_speed, attack_speed, col_radius, col_height;
	private int[][] _inv;
	private Location _loc, _fishLoc;
	private int obj_id, vehicle_obj_id, _race, sex, base_class, level, curCp, maxCp, _weaponEnchant, _weaponFlag;
	private long _exp;
	private int curHp, maxHp, curMp, maxMp, curLoad, maxLoad, rec_left, rec_have;
	private int _str, _con, _dex, _int, _wit, _men, _sp, ClanPrivs, InventoryLimit;
	private int _patk, _patkspd, _pdef, _pEvasion, _pAccuracy, _pCrit, _matk, _matkspd;
	private int _mdef, pvp_flag, karma, hair_style, hair_color, face, gm_commands, fame, vitality;
	private int clan_id, clan_crest_id, ally_id, ally_crest_id, large_clan_crest_id, large_clan_crest_idHF;
	private int private_store, can_crystalize, pk_kills, pvp_kills, class_id, agathion;
	private int _abnormalEffect, _abnormalEffect2, noble, hero, mount_id, cw_level;
	private int name_color, running, pledge_class, pledge_type, title_color, transformation;
	private int defenceFire, defenceWater, defenceWind, defenceEarth, defenceHoly, defenceUnholy;
	private int mount_type;
	private String _name, _title;
	private EffectCubic[] cubics;
	private Element attackElement;
	private int attackElementValue;
	private boolean isFlying, _allowMap;
	private int talismans;
	private boolean openCloak;
	private double _expPercent;
	private TeamType _team;
	private int _territoryId;
	private boolean _isDisguised;
	private boolean _isClanLeader;
	private int _moveType;

	private final byte[] _masks = new byte[]
	{
		(byte) 0x00,
		(byte) 0x00,
		(byte) 0x00
	};
	
	private int _initSize = 5;

	public UIPacket(Player player)
	{
		this(player, true);
	}

	public UIPacket(Player player, boolean addAll)
	{
		_name = player.getVisibleName(player);
		name_color = player.getVisibleNameColor(player);
		_title = player.getVisibleTitle(player);
		title_color = player.getVisibleTitleColor(player);

		if(player.isPledgeVisible(player))
		{
			Clan clan = player.getClan();
			Alliance alliance = clan == null ? null : clan.getAlliance();
			//
			clan_id = clan == null ? 0 : clan.getClanId();
			clan_crest_id = clan == null ? 0 : clan.getCrestId(player);
			large_clan_crest_id = clan == null ? 0 : clan.getCrestLargeId();
			large_clan_crest_idHF = clan == null ? 0 : clan.getCrestLargeIdHF();
			//
			ally_id = alliance == null ? 0 : alliance.getAllyId();
			ally_crest_id = alliance == null ? 0 : alliance.getAllyCrestId(player);
		}

		cw_level = player.isCursedWeaponEquipped() ? CursedWeaponsManager.getInstance().getLevel(player.getCursedWeaponEquippedId()) : 0;

		if(player.isGMInvisible())
			_title += "[I]";
		if(player.isPolymorphed())
			if(NpcHolder.getInstance().getTemplate(player.getPolyId()) != null)
				_title += " - " + NpcHolder.getInstance().getTemplate(player.getPolyId()).name;
			else
				_title += " - Polymorphed";

		if(player.isMounted())
		{
			_weaponEnchant = 0;
			mount_id = player.getMountNpcId() + 1000000;
			mount_type = player.getMountType();
		}
		else
		{
			_weaponEnchant = player.getEnchantEffect();
			mount_id = 0;
			mount_type = 0;
		}

		_weaponFlag = player.getActiveWeaponInstance() == null ? 0x14 : 0x28;

		move_speed = player.getMovementSpeedMultiplier();
		_runSpd = (int) (player.getRunSpeed() / move_speed);
		_walkSpd = (int) (player.getWalkSpeed() / move_speed);

		_flRunSpd = 0; // TODO
		_flWalkSpd = 0; // TODO

		if(player.isFlying())
		{
			_flyRunSpd = _runSpd;
			_flyWalkSpd = _walkSpd;
		}
		else
		{
			_flyRunSpd = 0;
			_flyWalkSpd = 0;
		}

		_swimRunSpd = (int) (player.getSwimRunSpeed() / move_speed);
		_swimWalkSpd = (int) (player.getSwimWalkSpeed() / move_speed);

		_inv = new int[Inventory.PAPERDOLL_MAX][4];
		for(int PAPERDOLL_ID : Inventory.PAPERDOLL_ORDER)
		{
			_inv[PAPERDOLL_ID][0] = player.getInventory().getPaperdollObjectId(PAPERDOLL_ID);
			_inv[PAPERDOLL_ID][1] = player.getInventory().getPaperdollVisualId(PAPERDOLL_ID);
			_inv[PAPERDOLL_ID][2] = player.getInventory().getPaperdollVariation1Id(PAPERDOLL_ID);
			_inv[PAPERDOLL_ID][3] = player.getInventory().getPaperdollVariation2Id(PAPERDOLL_ID);
		}

		_isClanLeader = player.isClanLeader();

		_relation = _isClanLeader ? 0x40 : 0;
		for(Event e : player.getEvents())
			_relation = e.getUserRelation(player, _relation);

		_loc = player.getLoc();
		obj_id = player.getObjectId();
		vehicle_obj_id = player.isInBoat() ? player.getBoat().getObjectId() : 0x00;
		_race = player.getRace().ordinal();
		sex = player.getSex();
		base_class = player.getBaseClassId();
		level = player.getLevel();
		_exp = player.getExp();
		_expPercent = Experience.getExpPercent(player.getLevel(), player.getExp());
		_str = player.getSTR();
		_dex = player.getDEX();
		_con = player.getCON();
		_int = player.getINT();
		_wit = player.getWIT();
		_men = player.getMEN();
		curHp = (int) player.getCurrentHp();
		maxHp = player.getMaxHp();
		curMp = (int) player.getCurrentMp();
		maxMp = player.getMaxMp();
		curLoad = player.getCurrentLoad();
		maxLoad = player.getMaxLoad();
		_sp = player.getIntSp();
		_patk = player.getPAtk(null);
		_patkspd = player.getPAtkSpd();
		_pdef = player.getPDef(null);
		_pEvasion = player.getEvasionRate(null);
		_pAccuracy = player.getAccuracy();
		_pCrit = player.getCriticalHit(null, null);
		_matk = player.getMAtk(null, null);
		_matkspd = player.getMAtkSpd();
		_mdef = player.getMDef(null, null);
		pvp_flag = player.getPvpFlag(); // 0=white, 1=purple, 2=purpleblink
		karma = player.getKarma();
		attack_speed = player.getAttackSpeedMultiplier();
		col_radius = player.getCurrentCollisionRadius();
		col_height = player.getCurrentCollisionHeight();
		hair_style = player.getInventory().getPaperdollVisualId(Inventory.PAPERDOLL_HAIR) > 0 ? sex : player.getHairStyle();
		hair_color = player.getHairColor();
		face = player.getFace();
		gm_commands = player.isGM() || player.getPlayerAccess().CanUseGMCommand ? 1 : 0;
		// builder level активирует в клиенте админские команды
		clan_id = player.getClanId();
		ally_id = player.getAllyId();
		private_store = (player.isInBuffStore() ? 0 : player.getPrivateStoreType());
		can_crystalize = player.getSkillLevel(Skill.SKILL_CRYSTALLIZE) > 0 ? 1 : 0;
		pk_kills = player.getPkKills();
		pvp_kills = player.getPvpKills();
		cubics = player.getCubics().toArray(new EffectCubic[player.getCubics().size()]);
		_abnormalEffect = player.getAbnormalEffectMask();
		_abnormalEffect2 = player.getAbnormalEffectMask2();
		ClanPrivs = player.getClanPrivileges();
		rec_left = player.getRecomLeft(); //c2 recommendations remaining
		rec_have = player.getRecomHave(); //c2 recommendations received
		InventoryLimit = player.getInventoryLimit();
		class_id = player.getClassId().getId();
		maxCp = player.getMaxCp();
		curCp = (int) player.getCurrentCp();
		_team = player.getTeam();
		noble = player.isNoble() || player.isGM() && Config.GM_HERO_AURA ? 1 : 0; //0x01: symbol on char menu ctrl+I
		hero = player.isHero() || player.isGM() && Config.GM_HERO_AURA ? 1 : 0; //0x01: Hero Aura and symbol
		//fishing = _cha.isFishing() ? 1 : 0; // Fishing Mode
		_fishLoc = player.getFishLoc();
		DominionSiegeEvent siegeEvent = player.getEvent(DominionSiegeEvent.class);
		if(siegeEvent != null)
		{
			_territoryId = siegeEvent.getId();
			_isDisguised = siegeEvent.getObjects(DominionSiegeEvent.DISGUISE_PLAYERS).contains(obj_id);
		}
		else
		{
			_territoryId = 0;
			_isDisguised = false;
		}
		running = player.isRunning() ? 0x01 : 0x00; //changes the Speed display on Status Window
		pledge_class = player.getPledgeClass();
		pledge_type = player.getPledgeType();
		transformation = player.getTransformation();
		attackElement = player.getAttackElement();
		attackElementValue = player.getAttack(attackElement);
		defenceFire = player.getDefence(Element.FIRE);
		defenceWater = player.getDefence(Element.WATER);
		defenceWind = player.getDefence(Element.WIND);
		defenceEarth = player.getDefence(Element.EARTH);
		defenceHoly = player.getDefence(Element.HOLY);
		defenceUnholy = player.getDefence(Element.UNHOLY);
		agathion = player.getAgathionId();
		fame = player.getFame();
		vitality = (int) player.getVitality();
			
		partyRoom = player.getMatchingRoom() != null && player.getMatchingRoom().getType() == MatchingRoom.PARTY_MATCHING && player.getMatchingRoom().getLeader() == player;
		isFlying = player.isInFlyingTransform();
		talismans = player.getTalismanCount();
		openCloak = player.getOpenCloak();
		_allowMap = player.isActionBlocked(Zone.BLOCKED_ACTION_MINIMAP);

		_moveType = player.isInFlyingTransform() ? 0x02 : (player.isInWater() ? 0x01 : 0x00);

		if(player.isInFightClub())
		{
			AbstractFightClub fightClubEvent = player.getFightClubEvent();
			_name = fightClubEvent.getVisibleName(player, _name, true);
			_title = fightClubEvent.getVisibleTitle(player, _title, true);
			title_color = fightClubEvent.getVisibleTitleColor(player, title_color, true);
			name_color = fightClubEvent.getVisibleNameColor(player, name_color, true);
		}

		_canWrite = true;

		if(addAll)
			addComponentType(UserInfoType.values());
	}
	
	@Override
	protected byte[] getMasks()
	{
		return _masks;
	}
	
	@Override
	protected void onNewMaskAdded(UserInfoType component)
	{
		calcBlockSize(component);
	}
	
	private void calcBlockSize(UserInfoType type)
	{
		switch(type)
		{
			case BASIC_INFO:
			{
				_initSize += type.getBlockLength() + (_name.length() * 2);
				break;
			}
			case CLAN:
			{
				_initSize += type.getBlockLength() + (_title.length() * 2);
				break;
			}
			default:
			{
				_initSize += type.getBlockLength();
				break;
			}
		}
	}

	@Override
	protected boolean canWrite()
	{
		return _canWrite;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(obj_id);

		writeD(_initSize);
		writeH(23);
		writeB(_masks);

		if(containsMask(UserInfoType.RELATION))
			writeD(_relation);

		if(containsMask(UserInfoType.BASIC_INFO))
		{
			writeH(UserInfoType.BASIC_INFO.getBlockLength() + (_name.length() * 2));
			writeString(_name);
			writeC(gm_commands);
			writeC(_race);
			writeC(sex);
			writeD(base_class);
			writeD(class_id);
			writeC(level);
		}

		if(containsMask(UserInfoType.BASE_STATS))
		{
			writeH(UserInfoType.BASE_STATS.getBlockLength());
			writeH(_str);
			writeH(_dex);
			writeH(_con);
			writeH(_int);
			writeH(_wit);
			writeH(_men);
			writeH(0); // LUC
			writeH(0); // CHA
		}

		if(containsMask(UserInfoType.MAX_HPCPMP))
		{
			writeH(UserInfoType.MAX_HPCPMP.getBlockLength());
			writeD(maxHp);
			writeD(maxMp);
			writeD(maxCp);
		}

		if(containsMask(UserInfoType.CURRENT_HPMPCP_EXP_SP))
		{
			writeH(UserInfoType.CURRENT_HPMPCP_EXP_SP.getBlockLength());
			writeD(curHp);
			writeD(curMp);
			writeD(curCp);
			writeQ(_sp);
			writeQ(_exp);
			writeF(_expPercent);
		}

		if(containsMask(UserInfoType.ENCHANTLEVEL))
		{
			writeH(UserInfoType.ENCHANTLEVEL.getBlockLength());
			writeC(_weaponEnchant);
			writeC(0); //TODOGOD armor enchant
		}

		if(containsMask(UserInfoType.APPAREANCE))
		{
			writeH(UserInfoType.APPAREANCE.getBlockLength());
			writeD(hair_style);
			writeD(hair_color);
			writeD(face);
			writeC(1);  //TODOGOD переключения прически/головного убора, true - включено
		}

		if(containsMask(UserInfoType.STATUS))
		{
			writeH(UserInfoType.STATUS.getBlockLength());
			writeC(mount_type);
			writeC(private_store);
			writeC(can_crystalize);
			writeC(0);	// Available Abilities Points
		}

		if(containsMask(UserInfoType.STATS))
		{
			writeH(UserInfoType.STATS.getBlockLength());
			writeH(_weaponFlag);
			writeD(_patk);
			writeD(_patkspd);
			writeD(_pdef);
			writeD(_pEvasion);
			writeD(_pAccuracy);
			writeD(_pCrit);
			writeD(_matk);
			writeD(_matkspd);
			writeD(_patkspd);
			writeD(0); // M. Evasion
			writeD(_mdef);
			writeD(0);	// M. Accuracy
			writeD(0);	// M. Crit
		}

		if(containsMask(UserInfoType.ELEMENTALS))
		{
			writeH(UserInfoType.ELEMENTALS.getBlockLength());
			writeH(defenceFire);
			writeH(defenceWater);
			writeH(defenceWind);
			writeH(defenceEarth);
			writeH(defenceHoly);
			writeH(defenceUnholy);
		}

		if(containsMask(UserInfoType.POSITION))
		{
			writeH(UserInfoType.POSITION.getBlockLength());
			writeD(_loc.x);
			writeD(_loc.y);
			writeD(_loc.z);
			writeD(vehicle_obj_id);
		}

		if(containsMask(UserInfoType.SPEED))
		{
			writeH(UserInfoType.SPEED.getBlockLength());
			writeH(_runSpd);
			writeH(_walkSpd);
			writeH(_swimRunSpd);
			writeH(_swimWalkSpd);
			writeH(_flRunSpd);
			writeH(_flWalkSpd);
			writeH(_flyRunSpd);
			writeH(_flyWalkSpd);
		}

		if(containsMask(UserInfoType.MULTIPLIER))
		{
			writeH(UserInfoType.MULTIPLIER.getBlockLength());
			writeF(move_speed);
			writeF(attack_speed);
		}

		if(containsMask(UserInfoType.COL_RADIUS_HEIGHT))
		{
			writeH(UserInfoType.COL_RADIUS_HEIGHT.getBlockLength());
			writeF(col_radius);
			writeF(col_height);
		}

		if(containsMask(UserInfoType.ATK_ELEMENTAL))
		{
			writeH(UserInfoType.ATK_ELEMENTAL.getBlockLength());
			writeC(attackElement.getId());
			writeH(attackElementValue);
		}

		if(containsMask(UserInfoType.CLAN))
		{
			writeH(UserInfoType.CLAN.getBlockLength() + (_title.length() * 2));
			writeString(_title);
			writeH(pledge_type);
			writeD(clan_id);
			writeD(large_clan_crest_id);
			writeD(clan_crest_id);
			writeD(ClanPrivs);
			writeC(_isClanLeader);
			writeD(ally_id);
			writeD(ally_crest_id);
			writeC(partyRoom ? 0x01 : 0x00);
		}

		if(containsMask(UserInfoType.SOCIAL))
		{
			writeH(UserInfoType.SOCIAL.getBlockLength());
			writeC(pvp_flag);
			writeD(karma);
			writeC(noble);
			writeC(hero);
			writeC(pledge_class);
			writeD(pk_kills);
			writeD(pvp_kills);
			writeH(rec_left);
			writeH(rec_have);
		}

		if(containsMask(UserInfoType.VITA_FAME))
		{
			writeH(UserInfoType.VITA_FAME.getBlockLength());
			writeD(vitality * 7);
			writeC(0x00); // Vita Bonus
			writeD(fame);
			writeD(0x00); // Рейдовые Очки
		}

		if(containsMask(UserInfoType.SLOTS))
		{
			writeH(UserInfoType.SLOTS.getBlockLength());
			writeC(talismans);
			writeC(0);	// Jewel Limits
			writeC(_team.ordinal());
			writeC(0x00); // (1 = Red, 2 = White, 3 = White Pink) dotted ring on the floor
			writeC(0x00);
			writeC(0x00);
			writeC(0x00);
			writeC(0x00);	// TODOGOD Active Main Agathion Slot
			writeC(0x00);	// TODOGOD Sub Agathions Limit
		}

		if(containsMask(UserInfoType.MOVEMENTS))
		{
			writeH(UserInfoType.MOVEMENTS.getBlockLength());
			writeC(_moveType);
			writeC(running);
		}

		if(containsMask(UserInfoType.COLOR))
		{
			writeH(UserInfoType.COLOR.getBlockLength());
			writeD(name_color);
			writeD(title_color);
		}

		if(containsMask(UserInfoType.INVENTORY_LIMIT))
		{
			writeH(UserInfoType.INVENTORY_LIMIT.getBlockLength());
			writeH(0x00);
			writeH(0x00);
			writeH(InventoryLimit);
			writeC(0); //при 1 не показывает титул
		}

		if(containsMask(UserInfoType.UNK_3))
		{
			writeH(UserInfoType.UNK_3.getBlockLength());
			writeD(0x00);
			writeH(0x00);
			writeC(0x00); // Chaos Festival Winner (100 - winner, 0 - none)
		}
	}

	@Override
	protected final void writeImplHF()
	{
		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z);
		writeD(vehicle_obj_id);
		writeD(obj_id);
		writeS(_name);
		writeD(_race);
		writeD(sex);
		writeD(base_class);
		writeD(level);
		writeQ(_exp);
		writeF(_expPercent);
		writeD(_str);
		writeD(_dex);
		writeD(_con);
		writeD(_int);
		writeD(_wit);
		writeD(_men);
		writeD(maxHp);
		writeD(curHp);
		writeD(maxMp);
		writeD(curMp);
		writeD(_sp);
		writeD(curLoad);
		writeD(maxLoad);
		writeD(_weaponFlag);

		for(int PAPERDOLL_ID : Inventory.PAPERDOLL_ORDER_HF)
			writeD(_inv[PAPERDOLL_ID][0]);

		for(int PAPERDOLL_ID : Inventory.PAPERDOLL_ORDER_HF)
			writeD(_inv[PAPERDOLL_ID][1]);

		for(int PAPERDOLL_ID : Inventory.PAPERDOLL_ORDER_HF)
		{
			writeH(_inv[PAPERDOLL_ID][2]);
			writeH(_inv[PAPERDOLL_ID][3]);
		}

		writeD(talismans);
		writeD(openCloak ? 0x01 : 0x00);

		writeD(_patk);
		writeD(_patkspd);
		writeD(_pdef);
		writeD(_pEvasion);
		writeD(_pAccuracy);
		writeD(_pCrit);
		writeD(_matk);
		writeD(_matkspd);
		writeD(_patkspd);
		writeD(_mdef);
		writeD(pvp_flag);
		writeD(karma);
		writeD(_runSpd);
		writeD(_walkSpd);
		writeD(_swimRunSpd); // swimspeed
		writeD(_swimWalkSpd); // swimspeed
		writeD(_flRunSpd);
		writeD(_flWalkSpd);
		writeD(_flyRunSpd);
		writeD(_flyWalkSpd);
		writeF(move_speed);
		writeF(attack_speed);
		writeF(col_radius);
		writeF(col_height);
		writeD(hair_style);
		writeD(hair_color);
		writeD(face);
		writeD(gm_commands);
		writeS(_title);
		writeD(clan_id);
		writeD(clan_crest_id);
		writeD(ally_id);
		writeD(ally_crest_id);
		// 0x40 leader rights
		// siege flags: attacker - 0x180 sword over name, defender - 0x80 shield, 0xC0 crown (|leader), 0x1C0 flag (|leader)
		writeD(_relation);
		writeC(mount_type); // mount type
		writeC(private_store);
		writeC(can_crystalize);
		writeD(pk_kills);
		writeD(pvp_kills);
		writeH(cubics.length);
		for(EffectCubic cubic : cubics)
			writeH(cubic == null ? 0 : cubic.getId());
		writeC(partyRoom ? 0x01 : 0x00); //1-find party members
		writeD(_abnormalEffect);
		writeC(isFlying ? 0x02 : 0x00);
		writeD(ClanPrivs);
		writeH(rec_left);
		writeH(rec_have);
		writeD(mount_id);
		writeH(InventoryLimit);
		writeD(class_id);
		writeD(0x00); // special effects? circles around player...
		writeD(maxCp);
		writeD(curCp);
		writeC(_weaponEnchant);
		writeC(_team.ordinal());
		writeD(large_clan_crest_idHF);
		writeC(noble);
		writeC(hero);
		writeC(0x00);
		writeD(_fishLoc.x);
		writeD(_fishLoc.y);
		writeD(_fishLoc.z);
		writeD(name_color);
		writeC(running);
		writeD(pledge_class);
		writeD(pledge_type);
		writeD(title_color);
		writeD(cw_level);
		writeD(transformation); // Transformation id

		// AttackElement (0 - Fire, 1 - Water, 2 - Wind, 3 - Earth, 4 - Holy, 5 - Dark, -2 - None)
		writeH(attackElement.getId());
		writeH(attackElementValue); // AttackElementValue
		writeH(defenceFire); // DefAttrFire
		writeH(defenceWater); // DefAttrWater
		writeH(defenceWind); // DefAttrWind
		writeH(defenceEarth); // DefAttrEarth
		writeH(defenceHoly); // DefAttrHoly
		writeH(defenceUnholy); // DefAttrUnholy

		writeD(agathion);

		// T2 Starts
		writeD(fame); // Fame
		writeD(_allowMap ? 1 : 0); // Minimap on Hellbound

		writeD(vitality); // Vitality Points
		writeD(_abnormalEffect2);
		writeD(_territoryId);
		writeD(_isDisguised ? 1 : 0);
		writeD(_isDisguised ? _territoryId : 0);
	}
}