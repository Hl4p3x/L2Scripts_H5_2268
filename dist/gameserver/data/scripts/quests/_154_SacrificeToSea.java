package quests;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _154_SacrificeToSea extends QuestScript
{
	private static final int FOX_FUR_ID = 1032;
	private static final int FOX_FUR_YARN_ID = 1033;
	private static final int MAIDEN_DOLL_ID = 1034;
	private static final int MYSTICS_EARRING_ID = 113;

	public _154_SacrificeToSea()
	{
		super(PARTY_NONE, ONETIME);

		addStartNpc(30312);

		addTalkId(30051);
		addTalkId(30055);

		addKillId(20481);
		addKillId(20544);
		addKillId(20545);

		addQuestItem(new int[]{
				FOX_FUR_ID,
				FOX_FUR_YARN_ID,
				MAIDEN_DOLL_ID
		});
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equals("1"))
		{
			st.set("id", "0");
			htmltext = "30312-04.htm";
			st.setCond(1);
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int npcId = npc.getNpcId();
		if(npcId == 30312 && st.getCond() == 0)
		{
			if(st.getPlayer().getLevel() >= 2)
				htmltext = "30312-03.htm";
			else
				htmltext = "30312-02.htm";
		}
		else if(npcId == 30312 && st.getCond() == 1 && st.getQuestItemsCount(FOX_FUR_YARN_ID) == 0 && st.getQuestItemsCount(MAIDEN_DOLL_ID) == 0 && st.getQuestItemsCount(FOX_FUR_ID) < 10)
			htmltext = "30312-05.htm";
		else if(npcId == 30312 && st.getCond() == 1 && st.getQuestItemsCount(FOX_FUR_ID) >= 10)
			htmltext = "30312-08.htm";
		else if(npcId == 30051 && st.getCond() == 1 && st.getQuestItemsCount(FOX_FUR_ID) < 10 && st.getQuestItemsCount(FOX_FUR_ID) > 0)
			htmltext = "30051-01.htm";
		else if(npcId == 30051 && st.getCond() == 1 && st.getQuestItemsCount(FOX_FUR_ID) >= 10 && st.getQuestItemsCount(FOX_FUR_YARN_ID) == 0 && st.getQuestItemsCount(MAIDEN_DOLL_ID) == 0 && st.getQuestItemsCount(MAIDEN_DOLL_ID) < 10)
		{
			htmltext = "30051-02.htm";
			st.giveItems(FOX_FUR_YARN_ID, 1, false, false);
			st.takeItems(FOX_FUR_ID, st.getQuestItemsCount(FOX_FUR_ID));
		}
		else if(npcId == 30051 && st.getCond() == 1 && st.getQuestItemsCount(FOX_FUR_YARN_ID) >= 1)
			htmltext = "30051-03.htm";
		else if(npcId == 30051 && st.getCond() == 1 && st.getQuestItemsCount(MAIDEN_DOLL_ID) >= 1)
			htmltext = "30051-04.htm";
		else if(npcId == 30312 && st.getCond() == 1 && st.getQuestItemsCount(FOX_FUR_YARN_ID) >= 1)
			htmltext = "30312-06.htm";
		else if(npcId == 30055 && st.getCond() == 1 && st.getQuestItemsCount(FOX_FUR_YARN_ID) >= 1)
		{
			htmltext = "30055-01.htm";
			st.giveItems(MAIDEN_DOLL_ID, 1, false, false);
			st.takeItems(FOX_FUR_YARN_ID, st.getQuestItemsCount(FOX_FUR_YARN_ID));
		}
		else if(npcId == 30055 && st.getCond() == 1 && st.getQuestItemsCount(MAIDEN_DOLL_ID) >= 1)
			htmltext = "30055-02.htm";
		else if(npcId == 30055 && st.getCond() == 1 && st.getQuestItemsCount(FOX_FUR_YARN_ID) == 0 && st.getQuestItemsCount(MAIDEN_DOLL_ID) == 0)
			htmltext = "30055-03.htm";
		else if(npcId == 30312 && st.getCond() == 1 && st.getQuestItemsCount(MAIDEN_DOLL_ID) >= 1)
		{
			if(st.getInt("id") != 154)
			{
				st.set("id", "154");
				htmltext = "30312-07.htm";
				st.takeItems(MAIDEN_DOLL_ID, st.getQuestItemsCount(MAIDEN_DOLL_ID));
				st.giveItems(MYSTICS_EARRING_ID, 1, false, false);
				st.addExpAndSp(1000, 0);
				st.finishQuest();
			}
		}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.getCond() == 1 && st.getQuestItemsCount(FOX_FUR_YARN_ID) == 0)
			st.rollAndGive(FOX_FUR_ID, 1, 1, 10, 14);
		return null;
	}
}