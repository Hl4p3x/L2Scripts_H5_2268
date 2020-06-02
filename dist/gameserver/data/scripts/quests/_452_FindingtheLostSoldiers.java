package quests;

import org.apache.commons.lang3.ArrayUtils;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;


public class _452_FindingtheLostSoldiers extends QuestScript
{
	private static final int JAKAN = 32773;
	private static final int TAG_ID = 15513;
	private static final int[] SOLDIER_CORPSES = {
			32769,
			32770,
			32771,
			32772
	};

	public _452_FindingtheLostSoldiers()
	{
		super(PARTY_NONE, DAILY);

		addStartNpc(JAKAN);
		addTalkId(JAKAN);
		addTalkId(SOLDIER_CORPSES);
		addQuestItem(TAG_ID);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		if(npc == null)
			return event;

		if(npc.getNpcId() == JAKAN)
		{
			if(event.equalsIgnoreCase("32773-3.htm"))
			{
				st.setCond(1);
			}
		}
		else if(ArrayUtils.contains(SOLDIER_CORPSES, npc.getNpcId()) && st.getCond() == 1)
		{
			st.giveItems(TAG_ID, 1, false, false);
			st.setCond(2);
			npc.deleteMe();
		}
		return event;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		if(npc == null)
			return htmltext;

		int cond = st.getCond();
		if(npc.getNpcId() == JAKAN)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 84)
					htmltext = "32773-1.htm";
				else
					htmltext = "32773-0.htm";
			}
			else if(cond == 1)
				htmltext = "32773-4.htm";
			else if(cond == 2)
			{
				htmltext = "32773-5.htm";
				st.unset("cond");
				st.takeItems(TAG_ID, 1);
				st.giveItems(57, 95200, true, true);
				st.addExpAndSp(435024, 50366);
				st.finishQuest();
			}
		}
		else if(ArrayUtils.contains(SOLDIER_CORPSES, npc.getNpcId()))
			if(cond == 1)
				htmltext = "corpse-1.htm";

		return htmltext;
	}

	@Override
	public String onCompleted(NpcInstance npc, QuestState st)
	{
		String htmltext = COMPLETED_DIALOG;
		int npcId = npc.getNpcId();
		if(npcId == JAKAN)
			htmltext = "32773-6.htm";
		return htmltext;
	}
}