package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _652_AnAgedExAdventurer extends QuestScript
{
	//NPC
	private static final int Tantan = 32012;
	private static final int Sara = 30180;
	//Item
	private static final int SoulshotCgrade = 1464;
	private static final int ScrollEnchantArmorD = 956;

	public _652_AnAgedExAdventurer()
	{
		super(PARTY_NONE, REPEATABLE);

		addStartNpc(Tantan);
		addTalkId(Sara);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("retired_oldman_tantan_q0652_03.htm") && st.getQuestItemsCount(SoulshotCgrade) >= 100)
		{
			st.setCond(1);
			st.takeItems(SoulshotCgrade, 100);
			htmltext = "retired_oldman_tantan_q0652_04.htm";
		}
		else
		{
			htmltext = "retired_oldman_tantan_q0652_03.htm";
			st.finishQuest(SOUND_GIVEUP);
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		String htmltext = NO_QUEST_DIALOG;
		int cond = st.getCond();
		if(npcId == Tantan)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() < 46)
					htmltext = "retired_oldman_tantan_q0652_01a.htm";
				else
					htmltext = "retired_oldman_tantan_q0652_01.htm";
			}
		}
		else if(npcId == Sara && cond == 1)
		{
			htmltext = "sara_q0652_01.htm";
			st.giveItems(ADENA_ID, 5026, true, true);
			if(Rnd.chance(50))
				st.giveItems(ScrollEnchantArmorD, 1, false, false);
			st.finishQuest();
		}
		return htmltext;
	}
}