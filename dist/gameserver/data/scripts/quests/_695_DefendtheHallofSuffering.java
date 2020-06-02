package quests;

import l2s.gameserver.instancemanager.SoIManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

/**
 * @author pchayka
 */

public class _695_DefendtheHallofSuffering extends QuestScript
{
	private static final int TEPIOS = 32603;

	public _695_DefendtheHallofSuffering()
	{
		super(PARTY_ALL, REPEATABLE);
		addStartNpc(TEPIOS);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("tepios_q695_3.htm"))
		{
			st.setCond(1);
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int npcId = npc.getNpcId();
		Player player = st.getPlayer();
		int cond = st.getCond();
		if(npcId == TEPIOS)
		{
			if(cond == 0)
			{
				if(player.getLevel() >= 75 && player.getLevel() <= 82)
				{
					if(SoIManager.getCurrentStage() == 4)
						htmltext = "tepios_q695_1.htm";
					else
						htmltext = "tepios_q695_0a.htm";
				}
				else
					htmltext = "tepios_q695_0.htm";
			}
			else if(cond == 1)
				htmltext = "tepios_q695_4.htm";
		}

		return htmltext;
	}

}