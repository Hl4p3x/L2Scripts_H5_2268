package npc.model;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.s2c.NpcHtmlMessagePacket;
import l2s.gameserver.templates.npc.NpcTemplate;

/**
 * @author: Kolobrodik
 * @date: 13:15/30.03.12
 */
public class RiganInstance extends NpcInstance
{
	private static final String FILE_PATH = "custom/";

	public RiganInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void showChatWindow(Player player, int val, Object... arg)
	{
		String fileName = FILE_PATH;
		fileName += getNpcId();
		if(val > 0)
			fileName += "-" + val;
		fileName += ".htm";
		player.sendPacket(new NpcHtmlMessagePacket(player, this, fileName, val));
	}
}
