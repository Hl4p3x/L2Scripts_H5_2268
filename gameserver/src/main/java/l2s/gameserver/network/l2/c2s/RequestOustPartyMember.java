package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Party;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.DimensionalRift;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.network.l2.components.CustomMessage;

public class RequestOustPartyMember extends L2GameClientPacket
{
	//Format: cS
	private String _name;

	@Override
	protected void readImpl()
	{
		_name = readS(16);
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		
		Party party = activeChar.getParty();
		if(party == null || !activeChar.getParty().isLeader(activeChar))
		{
			activeChar.sendActionFailed();
			return;
		}
				
			if(activeChar.isInOlympiadMode())
			{
				activeChar.sendMessage(new CustomMessage("common.PartyOuts", activeChar));
				return;
			}
						
			Player member = party.getPlayerByName(_name);
			
			if(member == activeChar)
			{
				activeChar.sendActionFailed();
				return;
			}
			
			if(member == null)
			{
				activeChar.sendActionFailed();
				return;
			}
			
			Reflection r = party.getReflection();

			if(r != null && r instanceof DimensionalRift && member.getReflection().equals(r))
				activeChar.sendMessage(new CustomMessage("l2s.gameserver.network.l2.c2s.RequestOustPartyMember.CantOustInRift", activeChar));
			else if(r != null && !(r instanceof DimensionalRift))
				activeChar.sendMessage(new CustomMessage("l2s.gameserver.network.l2.c2s.RequestOustPartyMember.CantOustInDungeon", activeChar));
			else
				party.removePartyMember(member, true);
	}
}