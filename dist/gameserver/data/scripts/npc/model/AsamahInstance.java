package npc.model;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.ItemFunctions;

/**
 * @author VISTALL
 * @date 10:35/24.06.2011
 */
public class AsamahInstance extends NpcInstance
{
	private static final int ElrokianTrap = 8763;
	private static final int TrapStone = 8764;

	public AsamahInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		if(command.equals("buyTrap"))
		{
			String htmltext = null;
			QuestState ElrokianHuntersProof = player.getQuestState(111);

			if(player.getLevel() >= 75 && ElrokianHuntersProof != null && ElrokianHuntersProof.isCompleted() && ItemFunctions.getItemCount(player, 57) > 1000000)
			{
				if(ItemFunctions.getItemCount(player, ElrokianTrap) > 0)
					htmltext = getNpcId() + "-alreadyhave.htm";
				else
				{
					ItemFunctions.deleteItem(player, 57, 1000000);
					ItemFunctions.addItem(player, ElrokianTrap, 1, "Buy trap by AsamahInstance");
					htmltext = getNpcId() + "-given.htm";
				}

			}
			else
				htmltext = getNpcId() + "-cant.htm";

			showChatWindow(player, "default/" + htmltext);
		}
		else if(command.equals("buyStones"))
		{
			String htmltext = null;
			QuestState ElrokianHuntersProof = player.getQuestState(111);

			if(player.getLevel() >= 75 && ElrokianHuntersProof != null && ElrokianHuntersProof.isCompleted() && ItemFunctions.getItemCount(player, 57) > 1000000)
			{
				ItemFunctions.deleteItem(player, 57, 1000000);
				ItemFunctions.addItem(player, TrapStone, 100, "Buy stones by AsamahInstance");
				htmltext = getNpcId() + "-given.htm";
			}
			else
				htmltext = getNpcId() + "-cant.htm";

			showChatWindow(player, "default/" + htmltext);
		}
		else
			super.onBypassFeedback(player, command);
	}
}
