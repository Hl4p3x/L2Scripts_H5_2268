package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _286_FabulousFeathers extends QuestScript
{
	//NPCs
	private static int ERINU = 32164;
	//Mobs
	private static int Shady_Muertos_Captain = 22251;
	private static int Shady_Muertos_Warrior = 22253;
	private static int Shady_Muertos_Archer = 22254;
	private static int Shady_Muertos_Commander = 22255;
	private static int Shady_Muertos_Wizard = 22256;
	//Quest Items
	private static int Commanders_Feather = 9746;
	//Chances
	private static int Commanders_Feather_Chance = 66;

	public _286_FabulousFeathers()
	{
		super(PARTY_NONE, REPEATABLE);
		addStartNpc(ERINU);
		addKillId(Shady_Muertos_Captain);
		addKillId(Shady_Muertos_Warrior);
		addKillId(Shady_Muertos_Archer);
		addKillId(Shady_Muertos_Commander);
		addKillId(Shady_Muertos_Wizard);
		addQuestItem(Commanders_Feather);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		if(event.equalsIgnoreCase("trader_erinu_q0286_0103.htm"))
		{
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("trader_erinu_q0286_0201.htm"))
		{
			st.takeItems(Commanders_Feather, -1);
			st.giveItems(ADENA_ID, 4160, true, true);
			st.finishQuest();
		}
		return event;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == ERINU)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 17)
					htmltext = "trader_erinu_q0286_0101.htm";
				else
					htmltext = "trader_erinu_q0286_0102.htm";
			}
			else
				htmltext = st.getQuestItemsCount(Commanders_Feather) >= 80 ? "trader_erinu_q0286_0105.htm" : "trader_erinu_q0286_0106.htm";
		}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState qs)
	{
		if(qs.isStarted())
		{
			long Commanders_Feather_count = qs.getQuestItemsCount(Commanders_Feather);
			if(Commanders_Feather_count < 80 && Rnd.chance(Commanders_Feather_Chance))
			{
				qs.giveItems(Commanders_Feather, 1, true, true);
				if(Commanders_Feather_count >= 79)
					qs.setCond(2);
				else
					qs.playSound(SOUND_ITEMGET);
			}
		}
		return null;
	}
}