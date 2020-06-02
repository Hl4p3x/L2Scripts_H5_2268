package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.instancemanager.SoIManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

/**
 * @author pchayka
 */

public class _698_BlocktheLordsEscape extends QuestScript
{
	// NPC
	private static final int TEPIOS = 32603;
	private static final int VesperNobleEnhanceStone = 14052;

	public _698_BlocktheLordsEscape()
	{
		super(PARTY_ALL, REPEATABLE);
		addStartNpc(TEPIOS);
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int npcId = npc.getNpcId();
		int cond = st.getCond();

		if(npcId == TEPIOS)
			if(cond == 0)
			{
				Player player = st.getPlayer();
				if(player.getLevel() < 75 || player.getLevel() > 85)
					return "tepios_q698_0.htm";

				if(SoIManager.getCurrentStage() != 5)
					return "tepios_q698_0a.htm";

				return "tepios_q698_1.htm";
			}
			else if(cond == 1 && st.getInt("defenceDone") == 1)
			{
				htmltext = "tepios_q698_5.htm";
				st.giveItems(VesperNobleEnhanceStone, Rnd.get(5, 8), false, false);
				st.finishQuest();
			}
			else
				return "tepios_q698_4.htm";
		return htmltext;
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("tepios_q698_3.htm"))
		{
			st.setCond(1);
		}
		return htmltext;
	}
}