package l2s.gameserver.network.l2.c2s;

import java.util.List;

import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.residence.Fortress;
import l2s.gameserver.network.l2.s2c.ExShowFortressSiegeInfo;

public class RequestFortressSiegeInfo extends L2GameClientPacket
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
		List<Fortress> fortressList = ResidenceHolder.getInstance().getResidenceList(Fortress.class);
		for(Fortress fort : fortressList)
			if(fort != null && fort.getSiegeEvent().isInProgress())
				activeChar.sendPacket(new ExShowFortressSiegeInfo(fort));
	}
}