package quests;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _126_IntheNameofEvilPart2 extends QuestScript
{
	private int Mushika = 32114;
	private int Asamah = 32115;
	private int UluKaimu = 32119;
	private int BaluKaimu = 32120;
	private int ChutaKaimu = 32121;
	private int WarriorGrave = 32122;
	private int ShilenStoneStatue = 32109;

	private int BONEPOWDER = 8783;
	private int EPITAPH = 8781;
	private int EWA = 729;

	public _126_IntheNameofEvilPart2()
	{
		super(PARTY_NONE, ONETIME);

		addStartNpc(Asamah);
		addTalkId(Mushika);
		addTalkId(UluKaimu);
		addTalkId(BaluKaimu);
		addTalkId(ChutaKaimu);
		addTalkId(WarriorGrave);
		addTalkId(ShilenStoneStatue);
		addQuestItem(BONEPOWDER, EPITAPH);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("asamah_q126_4.htm"))
		{
			st.setCond(1);
			st.takeAllItems(EPITAPH);
		}
		else if(event.equalsIgnoreCase("asamah_q126_7.htm"))
		{
			st.setCond(2);
		}
		else if(event.equalsIgnoreCase("ulukaimu_q126_2.htm"))
		{
			st.setCond(3);
		}
		else if(event.equalsIgnoreCase("ulukaimu_q126_8.htm"))
		{
			st.setCond(4);
		}
		else if(event.equalsIgnoreCase("ulukaimu_q126_10.htm"))
		{
			st.setCond(5);
		}
		else if(event.equalsIgnoreCase("balukaimu_q126_2.htm"))
		{
			st.setCond(6);
		}
		else if(event.equalsIgnoreCase("balukaimu_q126_7.htm"))
		{
			st.setCond(7);
		}
		else if(event.equalsIgnoreCase("balukaimu_q126_9.htm"))
		{
			st.setCond(8);
		}
		else if(event.equalsIgnoreCase("chutakaimu_q126_2.htm"))
		{
			st.setCond(9);
		}
		else if(event.equalsIgnoreCase("chutakaimu_q126_9.htm"))
		{
			st.setCond(10);
		}
		else if(event.equalsIgnoreCase("chutakaimu_q126_14.htm"))
		{
			st.setCond(11);
		}
		else if(event.equalsIgnoreCase("warriorgrave_q126_2.htm"))
		{
			st.setCond(12);
		}
		else if(event.equalsIgnoreCase("warriorgrave_q126_10.htm"))
		{
			st.setCond(13);
		}
		else if(event.equalsIgnoreCase("warriorgrave_q126_19.htm"))
		{
			st.setCond(14);
		}
		else if(event.equalsIgnoreCase("warriorgrave_q126_20.htm"))
		{
			st.setCond(15);
		}
		else if(event.equalsIgnoreCase("warriorgrave_q126_23.htm"))
		{
			st.setCond(16);
		}
		else if(event.equalsIgnoreCase("warriorgrave_q126_25.htm"))
		{
			st.setCond(17);
			st.giveItems(BONEPOWDER, 1, false, false);
		}
		else if(event.equalsIgnoreCase("warriorgrave_q126_27.htm"))
		{
			st.setCond(18);
		}
		else if(event.equalsIgnoreCase("shilenstatue_q126_2.htm"))
		{
			st.setCond(19);
		}
		else if(event.equalsIgnoreCase("shilenstatue_q126_13.htm"))
		{
			st.setCond(20);
			st.takeAllItems(BONEPOWDER);
		}
		else if(event.equalsIgnoreCase("asamah_q126_10.htm"))
		{
			st.setCond(21);
		}
		else if(event.equalsIgnoreCase("asamah_q126_17.htm"))
		{
			st.setCond(22);
		}
		else if(event.equalsIgnoreCase("mushika_q126_3.htm"))
		{
			st.setCond(23);
		}
		else if(event.equalsIgnoreCase("mushika_q126_4.htm"))
		{
			st.giveItems(EWA, 1, false, false);
			st.giveItems(ADENA_ID, 460483, true, true);
			st.addExpAndSp(1015973, 102802);
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
		if(npcId == Asamah)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 77 && st.getPlayer().isQuestCompleted(125))
					htmltext = "asamah_q126_1.htm";
				else
					htmltext = "asamah_q126_0.htm";
			}
			else if(cond == 1)
				htmltext = "asamah_q126_4.htm";
			else if(cond == 20)
				htmltext = "asamah_q126_8.htm";
			else if(cond == 21)
				htmltext = "asamah_q126_10.htm";
			else if(cond == 22)
				htmltext = "asamah_q126_17.htm";
			else
				htmltext = "asamah_q126_0a.htm";
		}
		else if(npcId == UluKaimu)
		{
			if(cond == 2)
				htmltext = "ulukaimu_q126_1.htm";
			else if(cond == 3)
				htmltext = "ulukaimu_q126_2.htm";
			else if(cond == 4)
				htmltext = "ulukaimu_q126_8.htm";
			else if(cond == 5)
				htmltext = "ulukaimu_q126_10.htm";
			else
				htmltext = "ulukaimu_q126_0.htm";
		}
		else if(npcId == BaluKaimu)
		{
			if(cond == 5)
				htmltext = "balukaimu_q126_1.htm";
			else if(cond == 6)
				htmltext = "balukaimu_q126_2.htm";
			else if(cond == 7)
				htmltext = "balukaimu_q126_7.htm";
			else if(cond == 8)
				htmltext = "balukaimu_q126_9.htm";
			else
				htmltext = "balukaimu_q126_0.htm";
		}
		else if(npcId == ChutaKaimu)
		{
			if(cond == 8)
				htmltext = "chutakaimu_q126_1.htm";
			else if(cond == 9)
				htmltext = "chutakaimu_q126_2.htm";
			else if(cond == 10)
				htmltext = "chutakaimu_q126_9.htm";
			else if(cond == 11)
				htmltext = "chutakaimu_q126_14.htm";
			else
				htmltext = "chutakaimu_q126_0.htm";
		}
		else if(npcId == WarriorGrave)
		{
			if(cond == 11)
				htmltext = "warriorgrave_q126_1.htm";
			else if(cond == 12)
				htmltext = "warriorgrave_q126_2.htm";
			else if(cond == 13)
				htmltext = "warriorgrave_q126_10.htm";
			else if(cond == 14)
				htmltext = "warriorgrave_q126_19.htm";
			else if(cond == 15)
				htmltext = "warriorgrave_q126_20.htm";
			else if(cond == 16)
				htmltext = "warriorgrave_q126_23.htm";
			else if(cond == 17)
				htmltext = "warriorgrave_q126_25.htm";
			else if(cond == 18)
				htmltext = "warriorgrave_q126_27.htm";
			else
				htmltext = "warriorgrave_q126_0.htm";
		}
		else if(npcId == ShilenStoneStatue)
		{
			if(cond == 18)
				htmltext = "shilenstatue_q126_1.htm";
			else if(cond == 19)
				htmltext = "shilenstatue_q126_2.htm";
			else if(cond == 20)
				htmltext = "shilenstatue_q126_13.htm";
			else
				htmltext = "shilenstatue_q126_0.htm";
		}
		else if(npcId == Mushika)
		{
			if(cond == 22)
				htmltext = "mushika_q126_1.htm";
			else if(cond == 23)
				htmltext = "mushika_q126_3.htm";
			else
				htmltext = "mushika_q126_0.htm";
		}

		return htmltext;
	}
}