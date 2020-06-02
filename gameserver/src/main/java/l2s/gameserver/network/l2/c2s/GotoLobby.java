package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.network.l2.s2c.CharacterSelectionInfoPacket;

public class GotoLobby extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{}

	@Override
	protected void runImpl()
	{
		CharacterSelectionInfoPacket cl = new CharacterSelectionInfoPacket(getClient().getLogin(), getClient().getSessionKey().playOkID1);
		sendPacket(cl);
	}
}