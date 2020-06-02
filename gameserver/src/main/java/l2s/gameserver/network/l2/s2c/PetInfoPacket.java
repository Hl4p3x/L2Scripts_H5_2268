package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.base.TeamType;
import l2s.gameserver.model.entity.events.impl.AbstractFightClub;
import l2s.gameserver.tables.PetDataTable;
import l2s.gameserver.utils.Location;

public class PetInfoPacket extends L2GameServerPacket
{
	private int _runSpd, _walkSpd, MAtkSpd, PAtkSpd, pvp_flag, karma, rideable;
	private int _type, obj_id, npc_id, runing, incombat, dead, _sp, level, _abnormalEffect, _abnormalEffect2;
	private int curFed, maxFed, curHp, maxHp, curMp, maxMp, curLoad, maxLoad;
	private int PAtk, PDef, MAtk, MDef, Accuracy, Evasion, Crit, sps, ss, type, _showSpawnAnimation;
	private Location _loc;
	private double col_redius, col_height;
	private long exp, exp_this_lvl, exp_next_lvl;
	private String _name, title;
	private TeamType _team;

	public PetInfoPacket(Servitor servitor)
	{
		_type = servitor.getServitorType();
		obj_id = servitor.getObjectId();
		npc_id = servitor.getTemplate().npcId;
		_loc = servitor.getLoc();
		MAtkSpd = servitor.getMAtkSpd();
		PAtkSpd = servitor.getPAtkSpd();
		_runSpd = servitor.getRunSpeed();
		_walkSpd = servitor.getWalkSpeed();
		col_redius = servitor.getCurrentCollisionRadius();
		col_height = servitor.getCurrentCollisionHeight();
		runing = servitor.isRunning() ? 1 : 0;
		incombat = servitor.isInCombat() ? 1 : 0;
		dead = servitor.isAlikeDead() ? 1 : 0;
		_name = servitor.getName().equalsIgnoreCase(servitor.getTemplate().name) ? "" : servitor.getName();
		title = servitor.getTitle();
		pvp_flag = servitor.getPvpFlag();
		karma = servitor.getKarma();
		curFed = servitor.getCurrentFed();
		maxFed = servitor.getMaxFed();
		curHp = (int) servitor.getCurrentHp();
		maxHp = servitor.getMaxHp();
		curMp = (int) servitor.getCurrentMp();
		maxMp = servitor.getMaxMp();
		_sp = servitor.getSp();
		level = servitor.getLevel();
		exp = servitor.getExp();
		exp_this_lvl = servitor.getExpForThisLevel();
		exp_next_lvl = servitor.getExpForNextLevel();
		curLoad = servitor.isPet() ? servitor.getInventory().getTotalWeight() : 0;
		maxLoad = servitor.getMaxLoad();
		PAtk = servitor.getPAtk(null);
		PDef = servitor.getPDef(null);
		MAtk = servitor.getMAtk(null, null);
		MDef = servitor.getMDef(null, null);
		Accuracy = servitor.getAccuracy();
		Evasion = servitor.getEvasionRate(null);
		Crit = servitor.getCriticalHit(null, null);
		_abnormalEffect = servitor.getAbnormalEffectMask();
		_abnormalEffect2 = servitor.getAbnormalEffectMask2();
		// В режиме трансформации значек mount/dismount не отображается
		if(servitor.getPlayer().getTransformation() != 0)
			rideable = 0; //not rideable
		else
			rideable = PetDataTable.isMountable(npc_id) ? 1 : 0;
		_team = servitor.getTeam();
		ss = servitor.getSoulshotConsumeCount();
		sps = servitor.getSpiritshotConsumeCount();
		_showSpawnAnimation = servitor.getSpawnAnimation();
		type = servitor.getFormId();

		if(servitor.getPlayer().isInFightClub())
		{
			AbstractFightClub fightClubEvent = servitor.getPlayer().getFightClubEvent();
			_name = fightClubEvent.getVisibleName(servitor.getPlayer(), _name, false);
			title = fightClubEvent.getVisibleTitle(servitor.getPlayer(), title, false);
		}
	}

	public PetInfoPacket update()
	{
		_showSpawnAnimation = 1;
		return this;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_type);
		writeD(obj_id);
		writeD(npc_id + 1000000);
		writeD(0); // 1=attackable
		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z);
		writeD(_loc.h);
		writeD(0);
		writeD(MAtkSpd);
		writeD(PAtkSpd);
		writeD(_runSpd);
		writeD(_walkSpd);
		writeD(_runSpd/*_swimRunSpd*/);
		writeD(_walkSpd/*_swimWalkSpd*/);
		writeD(_runSpd/*_flRunSpd*/);
		writeD(_walkSpd/*_flWalkSpd*/);
		writeD(_runSpd/*_flyRunSpd*/);
		writeD(_walkSpd/*_flyWalkSpd*/);
		writeF(1/*_cha.getProperMultiplier()*/);
		writeF(1/*_cha.getAttackSpeedMultiplier()*/);
		writeF(col_redius);
		writeF(col_height);
		writeD(0); // right hand weapon
		writeD(0);
		writeD(0); // left hand weapon
		writeC(1); // name above char 1=true ... ??
		writeC(runing); // running=1
		writeC(incombat); // attacking 1=true
		writeC(dead); // dead 1=true
		writeC(_showSpawnAnimation); // invisible ?? 0=false  1=true   2=summoned (only works if model has a summon animation)
		writeD(-1);
		writeS(_name);
		writeD(-1);
		writeS(title);
		writeD(1);
		writeD(pvp_flag); //0=white, 1=purple, 2=purpleblink, if its greater then karma = purple
		writeD(karma); // hmm karma ??
		writeD(curFed); // how fed it is
		writeD(maxFed); //max fed it can be
		writeD(curHp); //current hp
		writeD(maxHp); // max hp
		writeD(curMp); //current mp
		writeD(maxMp); //max mp
		writeD(_sp); //sp
		writeD(level);// lvl
		writeQ(exp);
		writeQ(exp_this_lvl); // 0%  absolute value
		writeQ(exp_next_lvl); // 100% absoulte value
		writeD(curLoad); //weight
		writeD(maxLoad); //max weight it can carry
		writeD(PAtk);//patk
		writeD(PDef);//pdef
		writeD(MAtk);//matk
		writeD(MDef);//mdef
		writeD(Accuracy);//accuracy
		writeD(Evasion);//evasion
		writeD(Crit);//critical
		writeD(_runSpd);//speed
		writeD(PAtkSpd);//atkspeed
		writeD(MAtkSpd);//casting speed
		writeD(_abnormalEffect); //c2  abnormal visual effect... bleed=1; poison=2; bleed?=4;
		writeH(rideable);
		writeC(0); // c2
		writeH(0); // ??
		writeC(_team.ordinal()); // team aura (1 = blue, 2 = red)
		writeD(ss);
		writeD(sps);
		writeD(type);
		writeD(_abnormalEffect2);
	}
}