package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _313_CollectSpores extends QuestScript
{
	//NPC
	public final int Herbiel = 30150;
	//Mobs
	public final int SporeFungus = 20509;
	//Quest Items
	public final int SporeSac = 1118;

	public _313_CollectSpores()
	{
		super(PARTY_NONE, REPEATABLE);

		addStartNpc(Herbiel);
		addTalkId(Herbiel);
		addKillId(SporeFungus);
		addQuestItem(SporeSac);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		if(event.equalsIgnoreCase("green_q0313_05.htm"))
		{
			st.setCond(1);
		}
		return event;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int cond = st.getCond();
		if(cond == 0)
		{
			if(st.getPlayer().getLevel() >= 8)
				htmltext = "green_q0313_03.htm";
			else
				htmltext = "green_q0313_02.htm";
		}
		else if(cond == 1)
			htmltext = "green_q0313_06.htm";
		else if(cond == 2)
			if(st.getQuestItemsCount(SporeSac) < 10)
			{
				st.setCond(1);
				htmltext = "green_q0313_06.htm";
			}
			else
			{
				st.takeItems(SporeSac, -1);
				st.giveItems(ADENA_ID, 3500, true, true);
				htmltext = "green_q0313_07.htm";
				st.finishQuest();
			}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(cond == 1 && npcId == SporeFungus && Rnd.chance(70))
		{
			st.giveItems(SporeSac, 1, true, true);
			if(st.getQuestItemsCount(SporeSac) < 10)
				st.playSound(SOUND_ITEMGET);
			else
			{
				st.setCond(2);
			}
		}
		return null;
	}
}