package quests;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _039_RedEyedInvaders extends QuestScript
{
	int BBN = 7178;
	int RBN = 7179;
	int IP = 7180;
	int GML = 7181;
	int[] REW = {
			6521,
			6529,
			6535
	};

	public _039_RedEyedInvaders()
	{
		super(PARTY_NONE, ONETIME);

		addStartNpc(30334);

		addTalkId(30332);

		addKillId(20919);
		addKillId(20920);
		addKillId(20921);
		addKillId(20925);

		addQuestItem(new int[]{
				BBN,
				IP,
				RBN,
				GML
		});
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equals("guard_babenco_q0039_0104.htm"))
		{
			st.setCond(1);
		}
		else if(event.equals("captain_bathia_q0039_0201.htm"))
		{
			st.setCond(2);
		}
		else if(event.equals("captain_bathia_q0039_0301.htm"))
		{
			if(st.getQuestItemsCount(BBN) >= 100 && st.getQuestItemsCount(RBN) >= 100)
			{
				st.setCond(4);
				st.takeItems(BBN, -1);
				st.takeItems(RBN, -1);
			}
			else
				htmltext = "captain_bathia_q0039_0203.htm";
		}
		else if(event.equals("captain_bathia_q0039_0401.htm"))
			if(st.getQuestItemsCount(IP) >= 30 && st.getQuestItemsCount(GML) >= 30)
			{
				st.takeItems(IP, -1);
				st.takeItems(GML, -1);
				st.giveItems(REW[0], 60, false, false);
				st.giveItems(REW[1], 1, false, false);
				st.giveItems(REW[2], 500, false, false);
				st.addExpAndSp(62236, 2783);
				st.finishQuest();
			}
			else
				htmltext = "captain_bathia_q0039_0304.htm";
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		String htmltext = NO_QUEST_DIALOG;
		int cond = st.getCond();
		if(npcId == 30334)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() < 20)
					htmltext = "guard_babenco_q0039_0102.htm";
				else if(st.getPlayer().getLevel() >= 20)
					htmltext = "guard_babenco_q0039_0101.htm";
			}
			else if(cond == 1)
				htmltext = "guard_babenco_q0039_0105.htm";
		}
		else if(npcId == 30332)
			if(cond == 1)
				htmltext = "captain_bathia_q0039_0101.htm";
			else if(cond == 2 && (st.getQuestItemsCount(BBN) < 100 || st.getQuestItemsCount(RBN) < 100))
				htmltext = "captain_bathia_q0039_0203.htm";
			else if(cond == 3 && st.getQuestItemsCount(BBN) >= 100 && st.getQuestItemsCount(RBN) >= 100)
				htmltext = "captain_bathia_q0039_0202.htm";
			else if(cond == 4 && (st.getQuestItemsCount(IP) < 30 || st.getQuestItemsCount(GML) < 30))
				htmltext = "captain_bathia_q0039_0304.htm";
			else if(cond == 5 && st.getQuestItemsCount(IP) >= 30 && st.getQuestItemsCount(GML) >= 30)
				htmltext = "captain_bathia_q0039_0303.htm";
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(cond == 2)
		{
			if((npcId == 20919 || npcId == 20920) && st.getQuestItemsCount(BBN) <= 99)
				st.giveItems(BBN, 1, true, true);
			else if(npcId == 20921 && st.getQuestItemsCount(RBN) <= 99)
				st.giveItems(RBN, 1, true, true);
			st.playSound(SOUND_ITEMGET);
			if(st.getQuestItemsCount(BBN) + st.getQuestItemsCount(RBN) >= 200)
			{
				st.setCond(3);
			}
		}

		if(cond == 4)
		{
			if((npcId == 20920 || npcId == 20921) && st.getQuestItemsCount(IP) <= 29)
				st.giveItems(IP, 1, true, true);
			else if(npcId == 20925 && st.getQuestItemsCount(GML) <= 29)
				st.giveItems(GML, 1, true, true);
			st.playSound(SOUND_ITEMGET);
			if(st.getQuestItemsCount(IP) + st.getQuestItemsCount(GML) >= 60)
			{
				st.setCond(5);
			}
		}
		return null;
	}
}