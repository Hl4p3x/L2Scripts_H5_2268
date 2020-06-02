package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _159_ProtectHeadsprings extends QuestScript
{
	int PLAGUE_DUST_ID = 1035;
	int HYACINTH_CHARM1_ID = 1071;
	int HYACINTH_CHARM2_ID = 1072;

	public _159_ProtectHeadsprings()
	{
		super(PARTY_NONE, ONETIME);

		addStartNpc(30154);

		addKillId(27017);

		addQuestItem(new int[]{
				PLAGUE_DUST_ID,
				HYACINTH_CHARM1_ID,
				HYACINTH_CHARM2_ID
		});
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equals("1"))
		{
			st.setCond(1);
			if(st.getQuestItemsCount(HYACINTH_CHARM1_ID) == 0)
			{
				st.giveItems(HYACINTH_CHARM1_ID, 1, false, false);
				htmltext = "30154-04.htm";
			}
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int cond = st.getCond();
		if(cond == 0)
		{
			if(st.getPlayer().getRace() != Race.ELF)
				htmltext = "30154-00.htm";
			else if(st.getPlayer().getLevel() >= 12)
				htmltext = "30154-03.htm";
			else
				htmltext = "30154-02.htm";
		}
		else if(cond == 1)
			htmltext = "30154-05.htm";
		else if(cond == 2)
		{
			st.takeItems(PLAGUE_DUST_ID, -1);
			st.takeItems(HYACINTH_CHARM1_ID, -1);
			st.giveItems(HYACINTH_CHARM2_ID, 1, false, false);
			st.setCond(3);
			htmltext = "30154-06.htm";
		}
		else if(cond == 3)
			htmltext = "30154-07.htm";
		else if(cond == 4)
		{
			st.takeItems(PLAGUE_DUST_ID, -1);
			st.takeItems(HYACINTH_CHARM2_ID, -1);
			st.giveItems(ADENA_ID, 18250, true, true);
			htmltext = "30154-08.htm";
			st.finishQuest();
		}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int cond = st.getCond();

		if(cond == 1 && Rnd.chance(60))
		{
			st.giveItems(PLAGUE_DUST_ID, 1, true, true);
			st.setCond(2);
		}
		else if(cond == 3 && Rnd.chance(60))
		{
			if(st.getQuestItemsCount(PLAGUE_DUST_ID) >= 4)
			{
				st.giveItems(PLAGUE_DUST_ID, 1, false, false);
				st.setCond(4);
			}
			else
			{
				st.giveItems(PLAGUE_DUST_ID, 1, true, true);
				st.playSound(SOUND_ITEMGET);
			}
		}	
		return null;
	}
}