package quests;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

/**
 * @author: pchayka
 * @date: 10.06.2010
 * @corrected by n0nam3
 */

public class _10271_TheEnvelopingDarkness extends QuestScript
{
	// NPC's
	private static int Orbyu = 32560;
	private static int El = 32556;
	private static int MedibalsCorpse = 32528;
	// ITEMS
	private static int InspectorMedibalsDocument = 13852;

	// OTHERS
	private static int CC_MINIMUM = 36;

	public _10271_TheEnvelopingDarkness()
	{
		super(PARTY_NONE, ONETIME);

		addStartNpc(Orbyu);
		addTalkId(Orbyu);
		addTalkId(El);
		addTalkId(MedibalsCorpse);
		addQuestItem(InspectorMedibalsDocument);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		int cond = st.getCond();
		String htmltext = event;

		if(event.equalsIgnoreCase("orbyu_q10271_3.htm") && cond == 0)
		{
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("el_q10271_2.htm"))
		{
			st.setCond(2);
		}
		else if(event.equalsIgnoreCase("medibalscorpse_q10271_2.htm"))
		{
			st.setCond(3);
			st.giveItems(InspectorMedibalsDocument, 1, false, false);
		}
		else if(event.equalsIgnoreCase("el_q10271_4.htm"))
		{
			st.setCond(4);
			st.takeItems(InspectorMedibalsDocument, -1);
		}
		else if(event.equalsIgnoreCase("orbyu_q10271_5.htm"))
		{
			st.giveItems(ADENA_ID, 62516, true, true);
			st.addExpAndSp(377403, 37867);
			st.finishQuest();
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == Orbyu)
		{
			if(cond == 0)
			{
				Player player = st.getPlayer();
				if(player.getLevel() >= 75 && player.isQuestCompleted(10269) && player.getParty() != null && player.getParty().getCommandChannel() != null && player.getParty().getCommandChannel().getMemberCount() >= CC_MINIMUM)
					htmltext = "orbyu_q10271_1.htm";
				else
					htmltext = "orbyu_q10271_0.htm";
			}
			else if(cond == 4)
				htmltext = "orbyu_q10271_4.htm";
		}
		else if(npcId == El)
		{
			if(cond == 1)
				htmltext = "el_q10271_1.htm";
			else if(cond == 3 && st.getQuestItemsCount(InspectorMedibalsDocument) >= 1)
				htmltext = "el_q10271_3.htm";
			else if(cond == 3 && st.getQuestItemsCount(InspectorMedibalsDocument) < 1)
				htmltext = "el_q10271_0.htm";
		}
		else if(npcId == MedibalsCorpse)
			if(cond == 2)
				htmltext = "medibalscorpse_q10271_1.htm";
		return htmltext;
	}
}
