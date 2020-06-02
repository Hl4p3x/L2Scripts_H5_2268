package l2s.gameserver.skills.skillclasses;

import java.util.List;

import l2s.gameserver.data.xml.holder.NpcHolder;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.entity.events.impl.CastleSiegeEvent;
import l2s.gameserver.model.entity.events.impl.DominionSiegeEvent;
import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import l2s.gameserver.model.entity.events.objects.SiegeClanObject;
import l2s.gameserver.model.entity.events.objects.ZoneObject;
import l2s.gameserver.model.instances.residences.SiegeFlagInstance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.stats.funcs.FuncMul;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.utils.Location;

public class SummonSiegeFlag extends Skill
{
	public static enum FlagType
	{
		DESTROY,
		NORMAL,
		ADVANCED,
		OUTPOST
	}

	private final FlagType _flagType;
	private final double _advancedMult;

	public SummonSiegeFlag(StatsSet set)
	{
		super(set);
		_flagType = set.getEnum("flagType", FlagType.class);
		_advancedMult = set.getDouble("advancedMultiplier", 1.);
	}
	
	private static int getIntCastleId(Zone zone)
	{
		if(zone.getName().startsWith("[gludio"))
			return 1;
		if(zone.getName().startsWith("[dion"))
			return 2;
		if(zone.getName().startsWith("[giran"))
			return 3;
		if(zone.getName().startsWith("[oren"))
			return 4;
		if(zone.getName().startsWith("[aden"))
			return 5;
		if(zone.getName().startsWith("[innadrile"))
			return 6;
		if(zone.getName().startsWith("[godad"))
			return 7;
		if(zone.getName().startsWith("[rune"))
			return 8;
		if(zone.getName().startsWith("[schuttgart"))
			return 9;			
		return 0;	
	}

	@Override
	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		if(!activeChar.isPlayer())
			return false;
		if(!super.checkCondition(activeChar, target, forceUse, dontMove, first))
			return false;

		Player player = (Player) activeChar;
		if(player.getClan() == null || !player.isClanLeader())
			return false;

		DominionSiegeEvent domEvent = activeChar.getEvent(DominionSiegeEvent.class);
		if(domEvent != null && (_flagType == FlagType.NORMAL || _flagType == FlagType.ADVANCED))
		{
			player.sendPacket(SystemMsg.YOU_CANNOT_SET_UP_A_BASE_HERE, new SystemMessagePacket(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this));
			return false;
		}
		
		CastleSiegeEvent castleEvent = activeChar.getEvent(CastleSiegeEvent.class);
		if(castleEvent != null && _flagType == FlagType.OUTPOST)
		{
			player.sendPacket(SystemMsg.YOU_CANNOT_SET_UP_A_BASE_HERE, new SystemMessagePacket(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this));
			return false;
		}	
		
		if(domEvent != null || castleEvent != null)
		{
			SiegeEvent<?, ?> siegeEvent = activeChar.getEvent(SiegeEvent.class);
			List<ZoneObject> zones = siegeEvent.getObjects(SiegeEvent.FLAG_ZONES);
			for(ZoneObject zone : zones)
			{
				if(player.isInZone(zone.getZone()))
				{
					int castleId = getIntCastleId(zone.getZone());
					if(castleId == player.getClan().getCastle())
					{
						player.sendPacket(SystemMsg.YOU_CANNOT_SET_UP_A_BASE_HERE, new SystemMessagePacket(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this));
						return false;
					}	
				}
			}		
		}
		
		switch(_flagType)
		{
			case DESTROY:
				//
				break;
			case OUTPOST:
			case NORMAL:
			case ADVANCED:
				if(player.isInZone(Zone.ZoneType.RESIDENCE))
				{
					player.sendPacket(SystemMsg.YOU_CANNOT_SET_UP_A_BASE_HERE, new SystemMessagePacket(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this));
					return false;
				}

				SiegeEvent<?, ?> siegeEvent = activeChar.getEvent(SiegeEvent.class);
				if(siegeEvent == null)
				{
					player.sendPacket(SystemMsg.YOU_CANNOT_SET_UP_A_BASE_HERE, new SystemMessagePacket(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this));
					return false;
				}

				boolean inZone = false;
				List<ZoneObject> zones = siegeEvent.getObjects(SiegeEvent.FLAG_ZONES);
				for(ZoneObject zone : zones)
				{
					if(player.isInZone(zone.getZone()))
						inZone = true;
				}

				if(!inZone)
				{
					player.sendPacket(SystemMsg.YOU_CANNOT_SET_UP_A_BASE_HERE, new SystemMessagePacket(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this));
					return false;
				}

				SiegeClanObject siegeClan = siegeEvent.getSiegeClan(siegeEvent.getClass() == DominionSiegeEvent.class ? SiegeEvent.DEFENDERS : SiegeEvent.ATTACKERS, player.getClan());
				if(siegeClan == null)
				{
					player.sendPacket(SystemMsg.YOU_CANNOT_SUMMON_THE_ENCAMPMENT_BECAUSE_YOU_ARE_NOT_A_MEMBER_OF_THE_SIEGE_CLAN_INVOLVED_IN_THE_CASTLE__FORTRESS__HIDEOUT_SIEGE, new SystemMessagePacket(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this));
					return false;
				}

				if(siegeClan.getFlag() != null)
				{
					player.sendPacket(SystemMsg.AN_OUTPOST_OR_HEADQUARTERS_CANNOT_BE_BUILT_BECAUSE_ONE_ALREADY_EXISTS, new SystemMessagePacket(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this));
					return false;
				}
				break;
		}
		return true;
	}

	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		Player player = (Player) activeChar;

		Clan clan = player.getClan();
		if(clan == null || !player.isClanLeader())
			return;

		SiegeEvent<?, ?> siegeEvent = activeChar.getEvent(SiegeEvent.class);
		if(siegeEvent == null)
			return;

		SiegeClanObject siegeClan = siegeEvent.getSiegeClan(siegeEvent.getClass() == DominionSiegeEvent.class ? SiegeEvent.DEFENDERS : SiegeEvent.ATTACKERS, clan);
		if(siegeClan == null)
			return;

		switch(_flagType)
		{
			case DESTROY:
				siegeClan.deleteFlag();
				break;
			default:
				if(siegeClan.getFlag() != null)
					return;

				// 35062/36590
				SiegeFlagInstance flag = (SiegeFlagInstance)NpcHolder.getInstance().getTemplate(_flagType == FlagType.OUTPOST ? 36590 : 35062).getNewInstance();
				flag.setClan(siegeClan);
				flag.addEvent(siegeEvent);

				if(_flagType == FlagType.ADVANCED)
					flag.addStatFunc(new FuncMul(Stats.MAX_HP, 0x50, flag, _advancedMult));

				flag.setCurrentHpMp(flag.getMaxHp(), flag.getMaxMp(), true);
				flag.setHeading(player.getHeading());

				// Ставим флаг перед чаром
				int x = (int) (player.getX() + 100 * Math.cos(player.headingToRadians(player.getHeading() - 32768)));
				int y = (int) (player.getY() + 100 * Math.sin(player.headingToRadians(player.getHeading() - 32768)));
				Location loc = GeoEngine.moveCheck(player.getX(), player.getY(), player.getZ(), x, y, player.getGeoIndex());
				if(loc == null)
					loc = Location.findAroundPosition(player.getLoc(), 100, player.getGeoIndex());
				flag.spawnMe(loc);

				siegeClan.setFlag(flag);
				break;
		}
	}
}