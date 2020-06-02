package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _324_SweetestVenom extends QuestScript
{
	//NPCs
	private static int ASTARON = 30351;
	//Mobs
	private static int Prowler = 20034;
	private static int Venomous_Spider = 20038;
	private static int Arachnid_Tracker = 20043;
	//Items
	private static int VENOM_SAC = 1077;
	//Chances
	private static int VENOM_SAC_BASECHANCE = 60;

	public _324_SweetestVenom()
	{
		super(PARTY_NONE, REPEATABLE);
		addStartNpc(ASTARON);
		addKillId(Prowler);
		addKillId(Venomous_Spider);
		addKillId(Arachnid_Tracker);
		addQuestItem(VENOM_SAC);
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		if(npc.getNpcId() != ASTARON)
			return htmltext;

		int cond = st.getCond();
		if(cond == 0)
		{
			if(st.getPlayer().getLevel() >= 18)
				htmltext = "astaron_q0324_03.htm";
			else
				htmltext = "astaron_q0324_02.htm";
		}
		else
		{
			long _count = st.getQuestItemsCount(VENOM_SAC);
			if(_count >= 10)
			{
				htmltext = "astaron_q0324_06.htm";
				st.takeItems(VENOM_SAC, -1);
				st.giveItems(ADENA_ID, 5810, true, true);
				st.finishQuest();
			}
			else
				htmltext = "astaron_q0324_05.htm";
		}
		return htmltext;
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		if(event.equalsIgnoreCase("astaron_q0324_04.htm"))
		{
			st.setCond(1);
		}
		return event;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState qs)
	{
		if(qs.isStarted())
		{
			long _count = qs.getQuestItemsCount(VENOM_SAC);
			int _chance = VENOM_SAC_BASECHANCE + (npc.getNpcId() - Prowler) / 4 * 12;
			if(_count < 10 && Rnd.chance(_chance))
			{
				qs.giveItems(VENOM_SAC, 1, true, true);
				if(qs.getQuestItemsCount(VENOM_SAC) >= 10)
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