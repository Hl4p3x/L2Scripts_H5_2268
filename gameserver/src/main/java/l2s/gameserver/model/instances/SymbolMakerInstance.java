package l2s.gameserver.model.instances;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.HennaEquipListPacket;
import l2s.gameserver.network.l2.s2c.HennaUnequipListPacket;
import l2s.gameserver.templates.npc.NpcTemplate;

/**
 * This class ...
 *
 * @version $Revision$ $Date$
 */
public class SymbolMakerInstance extends NpcInstance
{
	public SymbolMakerInstance(int objectID, NpcTemplate template)
	{
		super(objectID, template);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		if(command.equals("Draw"))
			player.sendPacket(new HennaEquipListPacket(player));
		else if(command.equals("RemoveList"))
			player.sendPacket(new HennaUnequipListPacket(player));
		else
			super.onBypassFeedback(player, command);
	}

	@Override
	public String getHtmlPath(int npcId, int val, Player player)
	{
		String pom;
		if(val == 0)
			pom = "SymbolMaker";
		else
			pom = "SymbolMaker-" + val;

		return "symbolmaker/" + pom + ".htm";
	}
}