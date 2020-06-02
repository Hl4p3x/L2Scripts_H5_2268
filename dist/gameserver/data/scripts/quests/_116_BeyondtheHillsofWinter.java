package quests;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _116_BeyondtheHillsofWinter extends QuestScript
{
	//NPC
	public final int FILAUR = 30535;
	public final int OBI = 32052;
	//Quest Item
	public final int Supplying_Goods_for_Railroad_Worker = 8098;
	//Item
	public final int Bandage = 1833;
	public final int Energy_Stone = 5589;
	public final int Thief_Key = 1661;
	public final int SSD = 1463;

	public _116_BeyondtheHillsofWinter()
	{
		super(PARTY_NONE, ONETIME);

		addStartNpc(FILAUR);
		addTalkId(OBI);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("elder_filaur_q0116_0104.htm"))
		{
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("elder_filaur_q0116_0201.htm"))
		{
			if(st.getQuestItemsCount(Bandage) >= 20 && st.getQuestItemsCount(Energy_Stone) >= 5 && st.getQuestItemsCount(Thief_Key) >= 10)
			{
				st.takeItems(Bandage, 20);
				st.takeItems(Energy_Stone, 5);
				st.takeItems(Thief_Key, 10);
				st.giveItems(Supplying_Goods_for_Railroad_Worker, 1, false, false);
				st.setCond(2);
			}
			else
				htmltext = "elder_filaur_q0116_0104.htm";
		}
		else if(event.equalsIgnoreCase("materials"))
		{
			htmltext = "railman_obi_q0116_0302.htm";
			st.takeItems(Supplying_Goods_for_Railroad_Worker, 1);
			st.giveItems(SSD, 1740, false, false);
			st.addExpAndSp(82792, 4981);
			st.finishQuest();
		}
		else if(event.equalsIgnoreCase("adena"))
		{
			htmltext = "railman_obi_q0116_0302.htm";
			st.takeItems(Supplying_Goods_for_Railroad_Worker, 1);
			st.giveItems(ADENA_ID, 16500, true, true);
			st.addExpAndSp(82792, 4981);
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
		if(npcId == FILAUR)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() < 30)
					htmltext = "elder_filaur_q0116_0103.htm";
				else
					htmltext = "elder_filaur_q0116_0101.htm";
			}
			else if(cond == 1)
				htmltext = "elder_filaur_q0116_0105.htm";
			else if(cond == 2)
				htmltext = "elder_filaur_q0116_0201.htm";
		}
		else if(npcId == OBI)
			if(cond == 2 && st.getQuestItemsCount(Supplying_Goods_for_Railroad_Worker) > 0)
				htmltext = "railman_obi_q0116_0201.htm";
		return htmltext;
	}
}