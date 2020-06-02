package l2s.gameserver.network.l2.s2c;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.Config;
import l2s.gameserver.instancemanager.CursedWeaponsManager;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.TeamType;
import l2s.gameserver.model.entity.events.impl.AbstractFightClub;
import l2s.gameserver.model.entity.events.impl.DominionSiegeEvent;
import l2s.gameserver.model.instances.DecoyInstance;
import l2s.gameserver.model.items.Inventory;
import l2s.gameserver.model.items.PcInventory;
import l2s.gameserver.model.matching.MatchingRoom;
import l2s.gameserver.model.pledge.Alliance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.skills.AbnormalEffect;
import l2s.gameserver.skills.effects.EffectCubic;
import l2s.gameserver.utils.Location;

public class CIPacket extends L2GameServerPacket
{
	private static final Logger _log = LoggerFactory.getLogger(CIPacket.class);

	private boolean _canWrite = false;
	private int[][] _inv;
	private int _mAtkSpd, _pAtkSpd;
	private int _runSpd, _walkSpd, _swimRunSpd, _swimWalkSpd, _flRunSpd, _flWalkSpd, _flyRunSpd, _flyWalkSpd;
	private Location _loc, _fishLoc;
	private String _name, _title;
	private int _objId, _race, _sex, base_class, pvp_flag, karma, rec_have;
	private double speed_move, speed_atack, col_radius, col_height;
	private int hair_style, hair_color, face, _abnormalEffect, _abnormalEffect2;
	private int clan_id, clan_crest_id, large_clan_crest_id, large_clan_crest_idHF, ally_id, ally_crest_id, class_id;
	private int _sit, _run, _combat, _dead, private_store, _enchant;
	private int _noble, _hero, _fishing, mount_type;
	private int plg_class, pledge_type, clan_rep_score, cw_level, mount_id;
	private int _nameColor, _title_color, _transform, _agathion, _clanBoatObjectId;
	private int _territoryId;
	private boolean _isDisguised;
	private EffectCubic[] cubics;
	private boolean _isPartyRoomLeader, _isFlying;
	private int _curHp, _maxHp, _curMp, _maxMp, _curCp;
	private TeamType _team;
	private final Player _receiver;
	private Set<AbnormalEffect> _abnormalEffects;

	public CIPacket(Player cha, Player receiver)
	{
		this((Creature) cha, receiver);
	}

	public CIPacket(DecoyInstance cha, Player receiver)
	{
		this((Creature) cha, receiver);
	}

	public CIPacket(Creature cha, Player receiver)
	{
		_receiver = receiver;

		if(cha == null)
		{
			System.out.println("CIPacket: cha is null!");
			Thread.dumpStack();
			return;
		}

		if(_receiver == null)
			return;

		if(cha.isInvisible(receiver))
			return;

		if(cha.isDeleted())
			return;

		_objId = cha.getObjectId();
		if(_objId == 0)
			return;

		if(_receiver.getObjectId() == _objId)
		{
			_log.error("You cant send CIPacket about his character to active user!!!");
			return;
		}

		Player player = cha.getPlayer();
		if(player == null)
			return;

		if(player.isInBoat())
		{
			_loc = player.getInBoatPosition();
			if(player.isClanAirShipDriver())
			{
				_clanBoatObjectId = player.getBoat().getObjectId();
			}
		}

		if (_loc == null)
			_loc = cha.getLoc();

		_name = player.getVisibleName(_receiver);
		_nameColor = player.getVisibleNameColor(_receiver);

		if(player.isConnected() || player.isInOfflineMode() || player.isInBuffStore() || player.isFakePlayer())
		{
			_title = player.getVisibleTitle(_receiver);
			_title_color = player.getVisibleTitleColor(_receiver);
		}
		else
		{
			_title = "NO CARRIER";
			_title_color = 255;
		}

		// Проклятое оружие и трансформации для ТВ скрывают имя и все остальные опознавательные знаки
		if(player.isPledgeVisible(_receiver))
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

		if(player.isMounted())
		{
			_enchant = 0;
			mount_id = player.getMountNpcId() + 1000000;
			mount_type = player.getMountType();
		}
		else
		{
			_enchant = player.getEnchantEffect();
			mount_id = 0;
			mount_type = 0;
		}

		_inv = new int[PcInventory.PAPERDOLL_MAX][4];
		for(int PAPERDOLL_ID : PAPERDOLL_ORDER)
		{
			_inv[PAPERDOLL_ID][0] = player.getInventory().getPaperdollItemId(PAPERDOLL_ID);
			_inv[PAPERDOLL_ID][1] = player.getInventory().getPaperdollVariation1Id(PAPERDOLL_ID);
			_inv[PAPERDOLL_ID][2] = player.getInventory().getPaperdollVariation2Id(PAPERDOLL_ID);
			_inv[PAPERDOLL_ID][3] = player.getInventory().getPaperdollVisualId(PAPERDOLL_ID);
		}

		_mAtkSpd = player.getMAtkSpd();
		_pAtkSpd = player.getPAtkSpd();
		speed_move = player.getMovementSpeedMultiplier();
		_runSpd = (int) (player.getRunSpeed() / speed_move);
		_walkSpd = (int) (player.getWalkSpeed() / speed_move);

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

		_swimRunSpd = player.getSwimRunSpeed();
		_swimWalkSpd = player.getSwimWalkSpeed();
		_race = player.getRace().ordinal();
		_sex = player.getSex();
		base_class = player.getBaseClassId();
		pvp_flag = player.getPvpFlag();
		karma = player.getKarma();

		speed_atack = player.getAttackSpeedMultiplier();
		col_radius = player.getCurrentCollisionRadius();
		col_height = player.getCurrentCollisionHeight();
		hair_style = player.getInventory().getPaperdollVisualId(Inventory.PAPERDOLL_HAIR) > 0 ? _sex : player.getHairStyle();
		hair_color = player.getHairColor();
		face = player.getFace();
		if(clan_id > 0 && player.getClan() != null)
			clan_rep_score = player.getClan().getReputationScore();
		else
			clan_rep_score = 0;
		_sit = player.isSitting() ? 0 : 1; // standing = 1 sitting = 0
		_run = player.isRunning() ? 1 : 0; // running = 1 walking = 0
		_combat = player.isInCombat() ? 1 : 0;
		_dead = player.isAlikeDead() ? 1 : 0;
		private_store = (player.isInBuffStore() ? 0 : player.isInObserverMode() ? Player.STORE_OBSERVING_GAMES : player.getPrivateStoreType());
		cubics = player.getCubics().toArray(new EffectCubic[player.getCubics().size()]);
		_abnormalEffects = player.getAbnormalEffects();
		_abnormalEffect = player.getAbnormalEffectMask();
		_abnormalEffect2 = player.getAbnormalEffectMask2();
		rec_have = player.isGM() || player.getKarma() > 0 ? 0 : player.getRecomHave();
		class_id = player.getClassId().getId();
		_team = player.getTeam();

		_noble = player.isNoble() ? 1 : 0; // 0x01: symbol on char menu ctrl+I
		_hero = player.isHero() || player.isGM() && Config.GM_HERO_AURA ? 1 : 0; // 0x01: Hero Aura
		_fishing = player.isFishing() ? 1 : 0;
		_fishLoc = player.getFishLoc();
		plg_class = player.getPledgeClass();
		pledge_type = player.getPledgeType();
		_transform = player.getTransformation();
		_agathion = player.getAgathionId();
		_isPartyRoomLeader = player.getMatchingRoom() != null && player.getMatchingRoom().getType() == MatchingRoom.PARTY_MATCHING && player.getMatchingRoom().getLeader() == player;
		_isFlying = player.isInFlyingTransform();
		_curHp = (int) player.getCurrentHp();
		_maxHp = player.getMaxHp();
		_curMp = (int) player.getCurrentMp();
		_maxMp = player.getMaxMp();
		_curCp = (int) player.getCurrentCp();

		DominionSiegeEvent siegeEvent = player.getEvent(DominionSiegeEvent.class);
		if(siegeEvent != null)
		{
			_territoryId = siegeEvent.getId();
			_isDisguised = siegeEvent.getObjects(DominionSiegeEvent.DISGUISE_PLAYERS).contains(_objId);
		}
		else
		{
			_territoryId = 0;
			_isDisguised = false;
		}

		if(player.isInFightClub())
		{
			AbstractFightClub fightClubEvent = player.getFightClubEvent();
			_name = fightClubEvent.getVisibleName(player, _name, false);
			_title = fightClubEvent.getVisibleTitle(player, _title, false);
			_title_color = fightClubEvent.getVisibleTitleColor(player, _title_color, false);
			_nameColor = fightClubEvent.getVisibleNameColor(player, _nameColor, false);
		}

		_canWrite = true;
	}

	@Override
	protected boolean canWrite()
	{
		return _canWrite;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x00); // [TODO]: Grand Crusade
		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z);
		writeD(_clanBoatObjectId);
		writeD(_objId);
		writeS(_name);
		writeH(_race);
		writeC(_sex);
		writeD(base_class);

		for(int PAPERDOLL_ID : PAPERDOLL_ORDER)
			writeD(_inv[PAPERDOLL_ID][0]);

		writeD(_inv[Inventory.PAPERDOLL_RHAND][1]);
		writeD(_inv[Inventory.PAPERDOLL_RHAND][2]);

		writeD(_inv[Inventory.PAPERDOLL_LHAND][1]);
		writeD(_inv[Inventory.PAPERDOLL_LHAND][2]);

		writeD(_inv[Inventory.PAPERDOLL_LRHAND][1]);
		writeD(_inv[Inventory.PAPERDOLL_LRHAND][2]);

		writeC(0);	// TODOGOD Armor Enchant Abnormal

		writeD(_inv[Inventory.PAPERDOLL_RHAND][3]);
		writeD(_inv[Inventory.PAPERDOLL_LHAND][3]);
		writeD(_inv[Inventory.PAPERDOLL_LRHAND][3]);
		writeD(_inv[Inventory.PAPERDOLL_GLOVES][3]);
		writeD(_inv[Inventory.PAPERDOLL_CHEST][3]);
		writeD(_inv[Inventory.PAPERDOLL_LEGS][3]);
		writeD(_inv[Inventory.PAPERDOLL_FEET][3]);
		writeD(_inv[Inventory.PAPERDOLL_HAIR][3]);
		writeD(_inv[Inventory.PAPERDOLL_DHAIR][3]);

		writeC(pvp_flag);
		writeD(karma);

		writeD(_mAtkSpd);
		writeD(_pAtkSpd);

		writeH(_runSpd);
		writeH(_walkSpd);
		writeH(_swimRunSpd);
		writeH(_swimWalkSpd);
		writeH(_flRunSpd);
		writeH(_flWalkSpd);
		writeH(_flyRunSpd);
		writeH(_flyWalkSpd);

		writeF(speed_move); // _cha.getProperMultiplier()
		writeF(speed_atack); // _cha.getAttackSpeedMultiplier()
		writeF(col_radius);
		writeF(col_height);
		writeD(hair_style);
		writeD(hair_color);
		writeD(face);
		writeS(_title);
		writeD(clan_id);
		writeD(clan_crest_id);
		writeD(ally_id);
		writeD(ally_crest_id);

		writeC(_sit);
		writeC(_run);
		writeC(_combat);
		writeC(_dead);
		writeC(0x00);
		writeC(mount_type); // 1-on Strider, 2-on Wyvern, 3-on Great Wolf, 0-no mount
		writeC(private_store);
		writeH(cubics.length);
		for(EffectCubic cubic : cubics)
			writeH(cubic == null ? 0 : cubic.getId());
		writeC(_isPartyRoomLeader ? 0x01 : 0x00); // find party members
		writeC(_isFlying ? 0x02 : 0x00);
		writeH(rec_have);
		writeD(mount_id);
		writeD(class_id);
		writeD(0x00);
		writeC(_enchant);

		writeC(_team.ordinal()); // team circle around feet 1 = Blue, 2 = red

		writeD(large_clan_crest_id);
		writeC(_noble);
		writeC(_hero);

		writeC(_fishing);
		writeD(_fishLoc.x);
		writeD(_fishLoc.y);
		writeD(_fishLoc.z);

		writeD(_nameColor);
		writeD(_loc.h);
		writeC(plg_class);
		writeH(pledge_type);
		writeD(_title_color);
		writeC(cw_level);
		writeD(clan_rep_score);
		writeD(_transform);
		writeD(_agathion);

		writeC(0x01);	// UNK

		writeD(_curCp);
		writeD(_curHp);
		writeD(_maxHp);
		writeD(_curMp);
		writeD(_maxMp);

		writeC(0x00);	// UNK

		writeD(_abnormalEffects.size());
		for(AbnormalEffect abnormal : _abnormalEffects)
			writeH(abnormal.getId());

		writeC(0);	// Chaos Festival Winner
		writeC(1);	// Show Head Accessories
		writeC(0);	// Used Abilities Points
	}

	@Override
	protected final void writeImplHF()
	{
		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z);
		writeD(_clanBoatObjectId);
		writeD(_objId);
		writeS(_name);
		writeD(_race);
		writeD(_sex);
		writeD(base_class);

		for(int PAPERDOLL_ID : PAPERDOLL_ORDER_HF)
			writeD(_inv[PAPERDOLL_ID][3]);

		for(int PAPERDOLL_ID : PAPERDOLL_ORDER_HF)
		{
			writeH(_inv[PAPERDOLL_ID][1]);
			writeH(_inv[PAPERDOLL_ID][2]);
		}

		writeD(0x01); //TODO talisman count(VISTALL)
		writeD(0x00); //TODO cloak status(VISTALL)

		writeD(pvp_flag);
		writeD(karma);

		writeD(_mAtkSpd);
		writeD(_pAtkSpd);

		writeD(0x00);

		writeD(_runSpd);
		writeD(_walkSpd);
		writeD(_swimRunSpd);
		writeD(_swimWalkSpd);
		writeD(_flRunSpd);
		writeD(_flWalkSpd);
		writeD(_flyRunSpd);
		writeD(_flyWalkSpd);

		writeF(speed_move); // _cha.getProperMultiplier()
		writeF(speed_atack); // _cha.getAttackSpeedMultiplier()
		writeF(col_radius);
		writeF(col_height);
		writeD(hair_style);
		writeD(hair_color);
		writeD(face);
		writeS(_title);
		writeD(clan_id);
		writeD(clan_crest_id);
		writeD(ally_id);
		writeD(ally_crest_id);

		writeC(_sit);
		writeC(_run);
		writeC(_combat);
		writeC(_dead);
		writeC(0x00); // is invisible
		writeC(mount_type); // 1-on Strider, 2-on Wyvern, 3-on Great Wolf, 0-no mount
		writeC(private_store);
		writeH(cubics.length);
		for(EffectCubic cubic : cubics)
			writeH(cubic == null ? 0 : cubic.getId());
		writeC(_isPartyRoomLeader ? 0x01 : 0x00); // find party members
		writeD(_abnormalEffect);
		writeC(_isFlying ? 0x02 : 0x00);
		writeH(rec_have);
		writeD(mount_id);
		writeD(class_id);
		writeD(0x00);
		writeC(_enchant);

		writeC(_team.ordinal()); // team circle around feet 1 = Blue, 2 = red

		writeD(large_clan_crest_idHF);
		writeC(_noble);
		writeC(_hero);

		writeC(_fishing);
		writeD(_fishLoc.x);
		writeD(_fishLoc.y);
		writeD(_fishLoc.z);

		writeD(_nameColor);
		writeD(_loc.h);
		writeD(plg_class);
		writeD(pledge_type);
		writeD(_title_color);
		writeD(cw_level);
		writeD(clan_rep_score);
		writeD(_transform);
		writeD(_agathion);

		writeD(0x01); // T2

		writeD(_abnormalEffect2);
		writeD(_territoryId);
		writeD(_isDisguised ? 1 : 0);
		writeD(_isDisguised ? _territoryId : 0);
	}

	public static final int[] PAPERDOLL_ORDER = {
			Inventory.PAPERDOLL_UNDER,
			Inventory.PAPERDOLL_HEAD,
			Inventory.PAPERDOLL_RHAND,
			Inventory.PAPERDOLL_LHAND,
			Inventory.PAPERDOLL_GLOVES,
			Inventory.PAPERDOLL_CHEST,
			Inventory.PAPERDOLL_LEGS,
			Inventory.PAPERDOLL_FEET,
			Inventory.PAPERDOLL_BACK,
			Inventory.PAPERDOLL_LRHAND,
			Inventory.PAPERDOLL_HAIR,
			Inventory.PAPERDOLL_DHAIR };

	public static final int[] PAPERDOLL_ORDER_HF =
	{
			Inventory.PAPERDOLL_UNDER,
			Inventory.PAPERDOLL_HEAD,
			Inventory.PAPERDOLL_RHAND,
			Inventory.PAPERDOLL_LHAND,
			Inventory.PAPERDOLL_GLOVES,
			Inventory.PAPERDOLL_CHEST,
			Inventory.PAPERDOLL_LEGS,
			Inventory.PAPERDOLL_FEET,
			Inventory.PAPERDOLL_BACK,
			Inventory.PAPERDOLL_LRHAND,
			Inventory.PAPERDOLL_HAIR,
			Inventory.PAPERDOLL_DHAIR,
			Inventory.PAPERDOLL_RBRACELET,
			Inventory.PAPERDOLL_LBRACELET,
			Inventory.PAPERDOLL_DECO1,
			Inventory.PAPERDOLL_DECO2,
			Inventory.PAPERDOLL_DECO3,
			Inventory.PAPERDOLL_DECO4,
			Inventory.PAPERDOLL_DECO5,
			Inventory.PAPERDOLL_DECO6,
			Inventory.PAPERDOLL_BELT
	};
}