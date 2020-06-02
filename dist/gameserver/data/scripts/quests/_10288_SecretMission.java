package quests;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _10288_SecretMission extends QuestScript
{
	// NPC's
	private static final int _dominic = 31350;
	private static final int _aquilani = 32780;
	private static final int _greymore = 32757;
	// Items
	private static final int _letter = 15529;

	public _10288_SecretMission()
	{
		super(PARTY_NONE, ONETIME);

		addStartNpc(_dominic);
		addStartNpc(_aquilani);
		addTalkId(_dominic);
		addTalkId(_greymore);
		addTalkId(_aquilani);
		addFirstTalkId(_aquilani);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		int npcId = npc.getNpcId();
		if(npcId == _dominic)
		{
			if(event.equalsIgnoreCase("31350-05.htm"))
			{
				st.setCond(1);
				st.giveItems(_letter, 1, false, false);
			}
		}
		else if(npcId == _greymore && event.equalsIgnoreCase("32757-03.htm"))
		{
			st.unset("cond");
			st.takeItems(_letter, -1);
			st.giveItems(57, 106583, true, true);
			st.addExpAndSp(417788, 46320);
			st.finishQuest();
		}
		else if(npcId == _aquilani)
		{
			if(event.equalsIgnoreCase("32780-05.htm"))
				st.setCond(2);
			else if(event.equalsIgnoreCase("teleport"))
			{
				if(st.isCompleted())
				{
					st.getPlayer().teleToLocation(118833, -80589, -2688);
					return null;
				}
			}
		}
		return event;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == _dominic)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 82)
					htmltext = "31350-01.htm";
				else
					htmltext = "31350-00.htm";
			}
			else if(cond == 1)
				htmltext = "31350-06.htm";
			else if(cond == 2)
				htmltext = "31350-07.htm";
		}
		else if(npcId == _aquilani)
		{
			if(cond == 1)
				htmltext = "32780-03.htm";
			else if(cond == 2)
				htmltext = "32780-06.htm";
		}
		else if(npcId == _greymore)
		{
			if(cond == 2)
				htmltext = "32757-01.htm";
		}
		return htmltext;
	}

	@Override
	public String onCompleted(NpcInstance npc, QuestState st)
	{
		String htmltext = COMPLETED_DIALOG;
		int npcId = npc.getNpcId();
		if(npcId == _dominic)
					htmltext = "31350-08.htm";
		return htmltext;
	}

	@Override
	public String onFirstTalk(NpcInstance npc, Player player)
	{
		QuestState st = player.getQuestState(this);
		if(st == null)
			st = newQuestState(player);

		if(npc.getNpcId() == _aquilani)
		{
			if(st.isCompleted())
				return "32780-01.htm";
			else
				return "32780-00.htm";
		}
		return null;
	}
}