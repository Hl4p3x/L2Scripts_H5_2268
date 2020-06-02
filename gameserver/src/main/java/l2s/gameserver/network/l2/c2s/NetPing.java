package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.Pinger;

/**
 * @author: Kolobrodik
 * @date: 23:40/28.02.2012
 * @description: Пакет ответа на запрос пинга ??? (@serverpackets.NetPing)
 * format: ddd
 */
public class NetPing extends L2GameClientPacket
{
    int kID;
    int ping;
    int mtu;

    @Override
    protected void readImpl() 
    {
        kID = readD();
        ping = readD();
        mtu = readD();
    }

	@Override
	protected void runImpl()
	{
    	final Player activeChar = getClient().getActiveChar();
    	
        if(activeChar == null)
        	return;
       
		Pinger.getInstance().answerPing(activeChar.getObjectId());
	}
}