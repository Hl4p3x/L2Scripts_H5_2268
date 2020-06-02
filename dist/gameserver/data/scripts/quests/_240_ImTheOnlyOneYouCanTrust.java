package quests;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _240_ImTheOnlyOneYouCanTrust extends QuestScript
{

	private static final int KINTAIJIN = 32640;

	private static final int SpikedStakato = 22617;
	private static final int CannibalisticStakatoFollower = 22624;
	private static final int CannibalisticStakatoLeader1 = 22625;
	private static final int CannibalisticStakatoLeader2 = 22626;

	private static final int STAKATOFANGS = 14879;

	public _240_ImTheOnlyOneYouCanTrust()
	{
		super(PARTY_NONE, ONETIME);
		addStartNpc(KINTAIJIN);
		addKillId(SpikedStakato, CannibalisticStakatoFollower, CannibalisticStakatoLeader1, CannibalisticStakatoLeader2);
		addQuestItem(STAKATOFANGS);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("32640-3.htm"))
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
		int cond = st.getCond();
		if(npcId == KINTAIJIN)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 81)
					htmltext = "32640-1.htm";
				else
					htmltext = "32640-0.htm";
			}
			else if(cond == 1)
				htmltext = "32640-8.htm";
			else if(cond == 2)
			{
				st.addExpAndSp(589542, 36800);
				st.giveItems(57, 147200, true, true);
				st.finishQuest();
				htmltext = "32640-9.htm";
			}
		}
		return htmltext;
	}

	@Override
	public String onCompleted(NpcInstance npc, QuestState st)
	{
		String htmltext = COMPLETED_DIALOG;
		int npcId = npc.getNpcId();
		if(npcId == KINTAIJIN)
			htmltext = "32640-10.htm";
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.getCond() == 1)
		{
			st.giveItems(STAKATOFANGS, 1, true, true);
			if(st.getQuestItemsCount(STAKATOFANGS) >= 25)
			{
				st.setCond(2);
			}
			else
				st.playSound(SOUND_ITEMGET);
		}
		return null;
	}
}