package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.Config;
import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.model.Party;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.DimensionalRift;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.utils.Log;

public class RequestWithDrawalParty extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		Party party = activeChar.getParty();
		if(party == null)
		{
			activeChar.sendActionFailed();
			return;
		}	
		
		if(activeChar.isInOlympiadMode())
		{
			activeChar.sendMessage(new CustomMessage("common.PartyOuts", activeChar));
			return;
		}

		if(Config.DISABLE_PARTY_LEAVE_INSTANCE && activeChar.getReflection() != ReflectionManager.DEFAULT)
		{
			activeChar.sendMessage("Во время инстанса, выход с группы запрещен");
			activeChar.sendActionFailed();
			return;
		}	
		
		Reflection r = activeChar.getParty().getReflection();
		if(r != null && r instanceof DimensionalRift && activeChar.getReflection().equals(r))
			activeChar.sendMessage(new CustomMessage("l2s.gameserver.network.l2.c2s.RequestWithDrawalParty.Rift", activeChar));
		else if(r != null && activeChar.isInCombat())
		{
			activeChar.sendMessage(new CustomMessage("common.PartyOuts", activeChar));
		}		
		else
		{
			activeChar.leaveParty();
			Log.LogEvent(activeChar.getName(), "Party", "LeftParty", "");
		}	
	}
}