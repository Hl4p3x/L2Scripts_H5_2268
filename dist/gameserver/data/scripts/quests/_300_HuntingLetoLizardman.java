package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _300_HuntingLetoLizardman extends QuestScript
{
	//NPCs
	private static int RATH = 30126;
	//Items
	private static int BRACELET_OF_LIZARDMAN = 7139;
	private static int ANIMAL_BONE = 1872;
	private static int ANIMAL_SKIN = 1867;
	//Chances
	private static int BRACELET_OF_LIZARDMAN_CHANCE = 70;

	public _300_HuntingLetoLizardman()
	{
		super(PARTY_NONE, REPEATABLE);
		addStartNpc(RATH);
		for(int lizardman_id = 20577; lizardman_id <= 20582; lizardman_id++)
			addKillId(lizardman_id);
		addQuestItem(BRACELET_OF_LIZARDMAN);
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		if(npc.getNpcId() != RATH)
			return htmltext;

		int cond = st.getCond();
		if(cond == 0)
		{
			if(st.getPlayer().getLevel() < 34)
				htmltext = "rarshints_q0300_0103.htm";
			else
				htmltext = "rarshints_q0300_0101.htm";
		}
		else
		{
			if(st.getQuestItemsCount(BRACELET_OF_LIZARDMAN) < 60)
			{
				htmltext = "rarshints_q0300_0106.htm";
				st.setCond(1);
			}
			else
				htmltext = "rarshints_q0300_0105.htm";
		}
		return htmltext;
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("rarshints_q0300_0104.htm"))
		{
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("rarshints_q0300_0201.htm"))
			if(st.getQuestItemsCount(BRACELET_OF_LIZARDMAN) < 60)
			{
				htmltext = "rarshints_q0300_0202.htm";
				st.setCond(1);
			}
			else
			{
				st.takeItems(BRACELET_OF_LIZARDMAN, -1);
				switch(Rnd.get(3))
				{
					case 0:
						st.giveItems(ADENA_ID, 30000, true, true);
						break;
					case 1:
						st.giveItems(ANIMAL_BONE, 50, false, false);
						break;
					case 2:
						st.giveItems(ANIMAL_SKIN, 50, false, false);
						break;
				}
				st.finishQuest();
			}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState qs)
	{
		if(qs.isStarted())
		{
			long _count = qs.getQuestItemsCount(BRACELET_OF_LIZARDMAN);
			if(_count < 60 && Rnd.chance(BRACELET_OF_LIZARDMAN_CHANCE))
			{
				qs.giveItems(BRACELET_OF_LIZARDMAN, 1, true, true);
				if(_count >= 59)
				{
					qs.setCond(2);
				}
				else
					qs.playSound(SOUND_ITEMGET);
			}
		}
		return null;
	}
}