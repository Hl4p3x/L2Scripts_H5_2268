package l2s.gameserver.network.l2.c2s;

import org.apache.commons.lang3.tuple.Pair;

import l2s.commons.lang.ArrayUtils;
import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.listener.actor.player.OnAnswerListener;
import l2s.gameserver.listener.actor.player.impl.ReviveAnswerListener;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.RestartType;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.entity.residence.ClanHall;
import l2s.gameserver.model.entity.residence.Fortress;
import l2s.gameserver.model.entity.residence.ResidenceFunction;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ActionFailPacket;
import l2s.gameserver.network.l2.s2c.DiePacket;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.TeleportUtils;

public class RequestRestartPoint extends L2GameClientPacket
{
	private RestartType _restartType;

	@Override
	protected void readImpl()
	{
		_restartType = ArrayUtils.valid(RestartType.VALUES, readD());
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();

		if(_restartType == null || activeChar == null)
			return;

		if(activeChar.isFakeDeath())
		{
			activeChar.breakFakeDeath();
			return;
		}

		if(!activeChar.isDead() && !activeChar.isGM())
		{
			activeChar.sendActionFailed();
			return;
		}

		requestRestart(activeChar, _restartType);
	}

	public static void requestRestart(Player activeChar, RestartType restartType)
	{
		if(activeChar.isFestivalParticipant())
		{
			activeChar.doRevive();
			return;
		}

		switch(restartType)
		{
			case AGATHION:
				if(activeChar.isAgathionResAvailable())
					activeChar.doRevive(100);
				else
					activeChar.sendPacket(ActionFailPacket.STATIC, new DiePacket(activeChar));
				break;
			case FIXED:
				if(activeChar.getPlayerAccess().ResurectFixed)
					activeChar.doRevive(100);
				else if(ItemFunctions.deleteItem(activeChar, 13300, 1, true))
				{
					activeChar.sendPacket(SystemMsg.YOU_HAVE_USED_THE_FEATHER_OF_BLESSING_TO_RESURRECT);
					activeChar.doRevive(100);
				}
				else if(ItemFunctions.deleteItem(activeChar, 10649, 1, true))
				{
					activeChar.sendPacket(SystemMsg.YOU_HAVE_USED_THE_FEATHER_OF_BLESSING_TO_RESURRECT);
					activeChar.doRevive(100);
				}
				else
					activeChar.sendPacket(ActionFailPacket.STATIC, new DiePacket(activeChar));
				break;
			default:
				Location loc = null;
				Reflection ref = activeChar.getReflection();

				if(ref == ReflectionManager.DEFAULT)
					for(Event e : activeChar.getEvents())
						loc = e.getRestartLoc(activeChar, restartType);

				if(loc == null)
					loc = defaultLoc(restartType, activeChar);

				if(activeChar.isInFightClub())
				{
					activeChar.getFightClubEvent().requestRespawn(activeChar, restartType);
					return;
				}

				if(loc != null)
				{
					Pair<Integer, OnAnswerListener> ask = activeChar.getAskListener(false);
					if(ask != null && ask.getValue() instanceof ReviveAnswerListener && !((ReviveAnswerListener) ask.getValue()).isForPet())
						activeChar.getAskListener(true);

					activeChar.setPendingRevive(true);
					activeChar.teleToLocation(loc, ReflectionManager.DEFAULT);
				}
				else
					activeChar.sendPacket(ActionFailPacket.STATIC, new DiePacket(activeChar));
				break;
		}
	}

	//FIXME [VISTALL] вынести куда то?
	// телепорт к флагу, не обрабатывается, по дефалту
	public static Location defaultLoc(RestartType restartType, Player activeChar)
	{
		Location loc = null;
		Clan clan = activeChar.getClan();

		switch(restartType)
		{
			case TO_CLANHALL:
				if(clan != null && clan.getHasHideout() != 0)
				{
					ClanHall clanHall = activeChar.getClanHall();
					loc = TeleportUtils.getRestartLocation(activeChar, RestartType.TO_CLANHALL);
					if(clanHall.getFunction(ResidenceFunction.RESTORE_EXP) != null)
						activeChar.restoreExp(clanHall.getFunction(ResidenceFunction.RESTORE_EXP).getLevel());
				}
				break;
			case TO_CASTLE:
				if(clan != null && clan.getCastle() != 0)
				{
					Castle castle = activeChar.getCastle();
					loc = TeleportUtils.getRestartLocation(activeChar, RestartType.TO_CASTLE);
					if(castle.getFunction(ResidenceFunction.RESTORE_EXP) != null)
						activeChar.restoreExp(castle.getFunction(ResidenceFunction.RESTORE_EXP).getLevel());
				}
				break;
			case TO_FORTRESS:
				if(clan != null && clan.getHasFortress() != 0)
				{
					Fortress fort = activeChar.getFortress();
					loc = TeleportUtils.getRestartLocation(activeChar, RestartType.TO_FORTRESS);
					if(fort.getFunction(ResidenceFunction.RESTORE_EXP) != null)
						activeChar.restoreExp(fort.getFunction(ResidenceFunction.RESTORE_EXP).getLevel());
				}
				break;
			case TO_VILLAGE:
			default:
				loc = TeleportUtils.getRestartLocation(activeChar, RestartType.TO_VILLAGE);
				break;
		}
		return loc;
	}
}
