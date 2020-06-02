package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _352_HelpRoodRaiseANewPet extends QuestScript
{
	//NPCs
	private static int Rood = 31067;
	//Mobs
	private static int Lienrik = 20786;
	private static int Lienrik_Lad = 20787;
	//Quest Items
	private static int LIENRIK_EGG1 = 5860;
	private static int LIENRIK_EGG2 = 5861;
	//Chances
	private static int LIENRIK_EGG1_Chance = 30;
	private static int LIENRIK_EGG2_Chance = 7;

	public _352_HelpRoodRaiseANewPet()
	{
		super(PARTY_NONE, REPEATABLE);
		addStartNpc(Rood);
		addKillId(Lienrik);
		addKillId(Lienrik_Lad);
		addQuestItem(LIENRIK_EGG1);
		addQuestItem(LIENRIK_EGG2);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		if(event.equalsIgnoreCase("31067-04.htm"))
		{
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("31067-09.htm"))
		{
			st.finishQuest();
		}
		return event;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		if(npc.getNpcId() != Rood)
			return htmltext;

		if(st.isNotAccepted())
		{
			if(st.getPlayer().getLevel() < 39)
				htmltext = "31067-00.htm";
			else
				htmltext = "31067-01.htm";
		}
		else
		{
			long reward = st.getQuestItemsCount(LIENRIK_EGG1) * 209 + st.getQuestItemsCount(LIENRIK_EGG2) * 2050;
			if(reward > 0)
			{
				htmltext = "31067-08.htm";
				st.takeItems(LIENRIK_EGG1, -1);
				st.takeItems(LIENRIK_EGG2, -1);
				st.giveItems(ADENA_ID, reward, true, true);
				st.playSound(SOUND_MIDDLE);
			}
			else
				htmltext = "31067-05.htm";
		}

		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState qs)
	{
		if(!qs.isStarted())
			return null;

		if(Rnd.chance(LIENRIK_EGG1_Chance))
		{
			qs.giveItems(LIENRIK_EGG1, 1, true, true);
			qs.playSound(SOUND_ITEMGET);
		}
		else if(Rnd.chance(LIENRIK_EGG2_Chance))
		{
			qs.giveItems(LIENRIK_EGG2, 1, true, true);
			qs.playSound(SOUND_ITEMGET);
		}
		return null;
	}
}