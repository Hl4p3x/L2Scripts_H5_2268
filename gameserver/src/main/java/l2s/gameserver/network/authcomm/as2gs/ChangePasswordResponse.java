package l2s.gameserver.network.authcomm.as2gs;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.authcomm.AuthServerCommunication;
import l2s.gameserver.network.authcomm.ReceivablePacket;
import l2s.gameserver.network.l2.GameClient;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.scripts.Functions;

/**
 * @Author: Death
 * @Date: 8/2/2007
 * @Time: 14:39:46
 */
public class ChangePasswordResponse extends ReceivablePacket
{
	public String _account;
	public boolean _changed;
	
	@Override
	protected void readImpl()
	{
		_account = readS();
		_changed = readD() == 1;
	}
	
	@Override
	protected void runImpl()
	{
		GameClient client = AuthServerCommunication.getInstance().getAuthedClient(_account);

		if(client == null)
			return;

		Player activeChar = client.getActiveChar();

		if(activeChar == null)
			return;

		if(_changed)
		{
			//ItemFunctions.deleteItem(activeChar, Config.PASSWORD_PAY_ID, Config.PASSWORD_PAY_COUNT);
			Functions.show(new CustomMessage("scripts.commands.user.password.ResultTrue", activeChar), activeChar);
		}	
		else
			Functions.show(new CustomMessage("scripts.commands.user.password.ResultFalse", activeChar), activeChar);
	}	
}