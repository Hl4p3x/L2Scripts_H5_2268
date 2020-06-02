package l2s.gameserver.handler.voicecommands.impl;

import l2s.gameserver.Config;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2s.gameserver.model.Effect;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Zone.ZoneType;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.skills.EffectType;
import l2s.gameserver.skills.skillclasses.Call;
import l2s.gameserver.utils.Location;

public class Relocate extends Functions implements IVoicedCommandHandler
{
	public static int SUMMON_PRICE = 5;

	private final String[] _commandList = new String[] { "km-all-to-me" };

	@Override
    public String[] getVoicedCommandList()
	{
		return _commandList;
	}

	@Override
    public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
	
		if(!Config.ALLOW_KM_ALL_TO_ME)
			return false;
			
		if(command.equalsIgnoreCase("km-all-to-me"))
		{
			if(!activeChar.isClanLeader())
			{
				activeChar.sendPacket(Msg.ONLY_THE_CLAN_LEADER_IS_ENABLED);
				return false;
			}

            if(activeChar.isInOlympiadMode())
			{
				activeChar.sendPacket(Msg.YOU_CANNOT_SUMMON_PLAYERS_WHO_ARE_CURRENTLY_PARTICIPATING_IN_THE_GRAND_OLYMPIAD);
				return false;
			}

			SystemMessage msg = Call.canSummonHere(activeChar);
			if(msg != null)
			{
				activeChar.sendPacket(msg);
				return false;
			}
			if(activeChar.isInZone(ZoneType.epic))
			{
				activeChar.sendPacket(Msg.YOU_MAY_NOT_SUMMON_FROM_YOUR_CURRENT_LOCATION);
				return false;
			}	
			if(activeChar.isAlikeDead())
			{
				activeChar.sendMessage(new CustomMessage("scripts.commands.voiced.Relocate.Dead", activeChar));
				return false;
			}

			for(Player pl : activeChar.getClan().getOnlineMembers(activeChar.getObjectId()))
			{
				Effect effect = pl.getEffectList().getEffectByType(EffectType.Meditation);
				if(effect != null)
					continue;
				if(Call.canBeSummoned(activeChar,pl) == null)
					// Спрашиваем, согласие на призыв
					pl.summonCharacterRequest(activeChar, Location.findPointToStay(activeChar.getX(), activeChar.getY(), activeChar.getZ(), 100, 150, activeChar.getReflection().getGeoIndex()), SUMMON_PRICE);
			}
			return true;
		}
		return false;
	}
}