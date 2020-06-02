package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.Element;
import l2s.gameserver.model.base.Experience;
import l2s.gameserver.model.items.Inventory;
import l2s.gameserver.model.pledge.Alliance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.utils.Location;

public class GMViewCharacterInfoPacket extends L2GameServerPacket
{
	private Location _loc;
	private int[][] _inv;
	private int obj_id, _race, _sex, class_id, pvp_flag, karma, level, mount_type;
	private int _str, _con, _dex, _int, _wit, _men, _sp;
	private int curHp, maxHp, curMp, maxMp, curCp, maxCp, curLoad, maxLoad, rec_left, rec_have;
	private int _patk, _patkspd, _pdef, evasion, accuracy, crit, _matk, _matkspd;
	private int _mdef, hair_style, hair_color, face, gm_commands;
	private int clan_id, clan_crest_id, ally_id, title_color;
	private int noble, hero, private_store, name_color, pk_kills, pvp_kills;
	private int _runSpd, _walkSpd, _swimRunSpd, _swimWalkSpd, DwarvenCraftLevel, running, pledge_class;
	private String _name, title;
	private long _exp;
	private double move_speed, attack_speed, col_radius, col_height;
	private Element attackElement;
	private int attackElementValue;
	private int defenceFire, defenceWater, defenceWind, defenceEarth, defenceHoly, defenceUnholy;
	private int fame, vitality;
	private int talismans;
	private boolean openCloak;
	private double _expPercent;

	public GMViewCharacterInfoPacket(final Player cha)
	{
		_loc = cha.getLoc();
		obj_id = cha.getObjectId();
		_name = cha.getName();
		_race = cha.getRace().ordinal();
		_sex = cha.getSex();
		class_id = cha.getClassId().getId();
		level = cha.getLevel();
		_exp = cha.getExp();
		_str = cha.getSTR();
		_dex = cha.getDEX();
		_con = cha.getCON();
		_int = cha.getINT();
		_wit = cha.getWIT();
		_men = cha.getMEN();
		curHp = (int) cha.getCurrentHp();
		maxHp = cha.getMaxHp();
		curMp = (int) cha.getCurrentMp();
		maxMp = cha.getMaxMp();
		_sp = cha.getIntSp();
		curLoad = cha.getCurrentLoad();
		maxLoad = cha.getMaxLoad();
		_patk = cha.getPAtk(null);
		_patkspd = cha.getPAtkSpd();
		_pdef = cha.getPDef(null);
		evasion = cha.getEvasionRate(null);
		accuracy = cha.getAccuracy();
		crit = cha.getCriticalHit(null, null);
		_matk = cha.getMAtk(null, null);
		_matkspd = cha.getMAtkSpd();
		_mdef = cha.getMDef(null, null);
		pvp_flag = cha.getPvpFlag();
		karma = cha.getKarma();
		_runSpd = cha.getRunSpeed();
		_walkSpd = cha.getWalkSpeed();
		_swimRunSpd = cha.getSwimRunSpeed();
		_swimWalkSpd = cha.getSwimWalkSpeed();
		move_speed = cha.getMovementSpeedMultiplier();
		attack_speed = cha.getAttackSpeedMultiplier();
		mount_type = cha.getMountType();
		col_radius = cha.getCurrentCollisionRadius();
		col_height = cha.getCurrentCollisionHeight();
		hair_style = cha.getHairStyle();
		hair_color = cha.getHairColor();
		face = cha.getFace();
		gm_commands = cha.isGM() ? 1 : 0;
		title = cha.getTitle();
		_expPercent = Experience.getExpPercent(cha.getLevel(), cha.getExp());
		//
		Clan clan = cha.getClan();
		Alliance alliance = clan == null ? null : clan.getAlliance();
		//
		clan_id = clan == null ? 0 : clan.getClanId();
		clan_crest_id = clan == null ? 0 : clan.getCrestId();
		//
		ally_id = alliance == null ? 0 : alliance.getAllyId();
		//ally_crest_id = alliance == null ? 0 : alliance.getAllyCrestId();

		private_store = cha.isInObserverMode() ? Player.STORE_OBSERVING_GAMES : cha.getPrivateStoreType();
		DwarvenCraftLevel = Math.max(cha.getSkillLevel(1320), 0);
		pk_kills = cha.getPkKills();
		pvp_kills = cha.getPvpKills();
		rec_left = cha.getRecomLeft(); //c2 recommendations remaining
		rec_have = cha.getRecomHave(); //c2 recommendations received
		curCp = (int) cha.getCurrentCp();
		maxCp = cha.getMaxCp();
		running = cha.isRunning() ? 0x01 : 0x00;
		pledge_class = cha.getPledgeClass();
		noble = cha.isNoble() ? 1 : 0; //0x01: symbol on char menu ctrl+I
		hero = cha.isHero() ? 1 : 0; //0x01: Hero Aura and symbol
		name_color = cha.getNameColor();
		title_color = cha.getTitleColor();
		attackElement = cha.getAttackElement();
		attackElementValue = cha.getAttack(attackElement);
		defenceFire = cha.getDefence(Element.FIRE);
		defenceWater = cha.getDefence(Element.WATER);
		defenceWind = cha.getDefence(Element.WIND);
		defenceEarth = cha.getDefence(Element.EARTH);
		defenceHoly = cha.getDefence(Element.HOLY);
		defenceUnholy = cha.getDefence(Element.UNHOLY);
		fame = cha.getFame();
		vitality = (int) cha.getVitality();
		talismans = cha.getTalismanCount();
		openCloak = cha.getOpenCloak();
		_inv = new int[Inventory.PAPERDOLL_MAX][4];
		for(int PAPERDOLL_ID : Inventory.PAPERDOLL_ORDER)
		{
			_inv[PAPERDOLL_ID][0] = cha.getInventory().getPaperdollObjectId(PAPERDOLL_ID);
			_inv[PAPERDOLL_ID][1] = cha.getInventory().getPaperdollItemId(PAPERDOLL_ID);
			_inv[PAPERDOLL_ID][2] = cha.getInventory().getPaperdollVariation1Id(PAPERDOLL_ID);
			_inv[PAPERDOLL_ID][3] = cha.getInventory().getPaperdollVariation2Id(PAPERDOLL_ID);
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z);
		writeD(_loc.h);
		writeD(obj_id);
		writeS(_name);
		writeD(_race);
		writeD(_sex);
		writeD(class_id);
		writeD(level);
		writeQ(_exp);
		writeF(_expPercent);
		writeD(_str);
		writeD(_dex);
		writeD(_con);
		writeD(_int);
		writeD(_wit);
		writeD(_men);
		writeD(0x00);	// loc
		writeD(0x00);	// cha
		writeD(maxHp);
		writeD(curHp);
		writeD(maxMp);
		writeD(curMp);
		writeQ(_sp);
		writeD(curLoad);
		writeD(maxLoad);
		writeD(pk_kills);

		for(int PAPERDOLL_ID : Inventory.PAPERDOLL_ORDER)
			writeD(_inv[PAPERDOLL_ID][0]);

		for(int PAPERDOLL_ID : Inventory.PAPERDOLL_ORDER)
			writeD(_inv[PAPERDOLL_ID][1]);

		for(int PAPERDOLL_ID : Inventory.PAPERDOLL_ORDER)
		{
			writeD(_inv[PAPERDOLL_ID][2]);
			writeD(_inv[PAPERDOLL_ID][3]);
		}

		writeC(talismans);
		writeC(0x00);
		writeD(0x00);
		writeH(0x00);
		writeD(_patk);
		writeD(_patkspd);
		writeD(_pdef);
		writeD(evasion);
		writeD(accuracy);
		writeD(crit);
		writeD(_matk);

		writeD(_matkspd);
		writeD(_patkspd);

		writeD(_mdef);
		writeD(0x00);	// M. Evasion
		writeD(0x00);	// M. Accuracy
		writeD(0x00);	// M. Crit

		writeD(pvp_flag);
		writeD(karma);

		writeD(_runSpd);
		writeD(_walkSpd);
		writeD(_swimRunSpd); // swimspeed
		writeD(_swimWalkSpd); // swimspeed
		writeD(_runSpd);
		writeD(_walkSpd);
		writeD(_runSpd);
		writeD(_walkSpd);
		writeF(move_speed);
		writeF(attack_speed);
		writeF(col_radius);
		writeF(col_height);
		writeD(hair_style);
		writeD(hair_color);
		writeD(face);
		writeD(gm_commands);

		writeS(title);
		writeD(clan_id);
		writeD(clan_crest_id);
		writeD(ally_id);
		writeC(mount_type);
		writeC(private_store);
		writeC(DwarvenCraftLevel); //_cha.getDwarvenCraftLevel() > 0 ? 1 : 0
		writeD(pk_kills);
		writeD(pvp_kills);

		writeH(rec_left);
		writeH(rec_have); //Blue value for name (0 = white, 255 = pure blue)
		writeD(class_id);
		writeD(0x00); // special effects? circles around player...
		writeD(maxCp);
		writeD(curCp);

		writeC(running); //changes the Speed display on Status Window

		writeC(321);

		writeD(pledge_class); //changes the text above CP on Status Window

		writeC(noble);
		writeC(hero);

		writeD(name_color);
		writeD(title_color);

		writeH(attackElement.getId());
		writeH(attackElementValue);
		writeH(defenceFire);
		writeH(defenceWater);
		writeH(defenceWind);
		writeH(defenceEarth);
		writeH(defenceHoly);
		writeH(defenceUnholy);

		writeD(fame);
		writeD(vitality * 7);

		writeD(0x00);
		writeD(0x00);
	}

	@Override
	protected final void writeImplHF()
	{
		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z);
		writeD(_loc.h);
		writeD(obj_id);
		writeS(_name);
		writeD(_race);
		writeD(_sex);
		writeD(class_id);
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
		writeD(pk_kills);

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
		writeD(evasion);
		writeD(accuracy);
		writeD(crit);
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
		writeD(_runSpd);
		writeD(_walkSpd);
		writeD(_runSpd);
		writeD(_walkSpd);
		writeF(move_speed);
		writeF(attack_speed);
		writeF(col_radius);
		writeF(col_height);
		writeD(hair_style);
		writeD(hair_color);
		writeD(face);
		writeD(gm_commands);
		writeS(title);
		writeD(clan_id);
		writeD(clan_crest_id);
		writeD(ally_id);
		writeC(mount_type);
		writeC(private_store);
		writeC(DwarvenCraftLevel); //_cha.getDwarvenCraftLevel() > 0 ? 1 : 0
		writeD(pk_kills);
		writeD(pvp_kills);
		writeH(rec_left);
		writeH(rec_have); //Blue value for name (0 = white, 255 = pure blue)
		writeD(class_id);
		writeD(0x00); // special effects? circles around player...
		writeD(maxCp);
		writeD(curCp);
		writeC(running); //changes the Speed display on Status Window
		writeC(321);
		writeD(pledge_class); //changes the text above CP on Status Window
		writeC(noble);
		writeC(hero);
		writeD(name_color);
		writeD(title_color);

		writeH(attackElement.getId());
		writeH(attackElementValue);
		writeH(defenceFire);
		writeH(defenceWater);
		writeH(defenceWind);
		writeH(defenceEarth);
		writeH(defenceHoly);
		writeH(defenceUnholy);

		writeD(fame);
		writeD(vitality);
	}
}