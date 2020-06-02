package quests;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _10283_RequestOfIceMerchant extends QuestScript
{
	// NPC's
	private static final int _rafforty = 32020;
	private static final int _kier = 32022;
	private static final int _jinia = 32760;

	public _10283_RequestOfIceMerchant()
	{
		super(PARTY_NONE, ONETIME);

		addStartNpc(_rafforty);
		addTalkId(_kier);
		addTalkId(_jinia);
		addFirstTalkId(_jinia);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		if(npc == null)
			return null;

		int npcId = npc.getNpcId();
		if(npcId == _rafforty)
		{
			if(event.equalsIgnoreCase("32020-03.htm"))
			{
				st.setCond(1);
			}
			else if(event.equalsIgnoreCase("32020-07.htm"))
			{
				st.setCond(2);
			}
		}
		else if(npcId == _kier && event.equalsIgnoreCase("spawn"))
		{
			addSpawn(_jinia, 104322, -107669, -3680, 44954, 0, 60000);
			return null;
		}
		else if(npcId == _jinia && event.equalsIgnoreCase("32760-04.htm"))
		{
			st.giveItems(57, 190000, true, true);
			st.addExpAndSp(627000, 50300);
			st.finishQuest();
			npc.deleteMe();
		}
		return event;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == _rafforty)
		{
			if(cond == 0)
			{
				if(st.getPlayer().isQuestCompleted(115) && st.getPlayer().getLevel() >= 82)
					htmltext = "32020-01.htm";
				else
					htmltext = "32020-00.htm";
			}
			if(cond == 1)
				htmltext = "32020-04.htm";
			else if(cond == 2)
				htmltext = "32020-08.htm";
		}
		else if(npcId == _kier)
		{
			if(cond == 2)
				htmltext = "32022-01.htm";
		}
		else if(npcId == _jinia)
		{
			if(cond == 2)
				htmltext = "32760-02.htm";
		}
		return htmltext;
	}

	@Override
	public String onFirstTalk(NpcInstance npc, Player player)
	{
		QuestState st = player.getQuestState(this);
		if(st == null)
			return null;
		if(npc.getNpcId() == _jinia && st.getCond() == 2)
			return "32760-01.htm";
		return null;
	}
}
