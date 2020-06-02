package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _316_DestroyPlaguebringers extends QuestScript
{
	//NPCs
	private static int Ellenia = 30155;
	//Mobs
	private static int Sukar_Wererat = 20040;
	private static int Sukar_Wererat_Leader = 20047;
	private static int Varool_Foulclaw = 27020;
	//Quest Items
	private static int Wererats_Fang = 1042;
	private static int Varool_Foulclaws_Fang = 1043;
	//Chances
	private static int Wererats_Fang_Chance = 50;
	private static int Varool_Foulclaws_Fang_Chance = 30;

	public _316_DestroyPlaguebringers()
	{
		super(PARTY_NONE, REPEATABLE);
		addStartNpc(Ellenia);
		addKillId(Sukar_Wererat);
		addKillId(Sukar_Wererat_Leader);
		addKillId(Varool_Foulclaw);
		addQuestItem(Wererats_Fang);
		addQuestItem(Varool_Foulclaws_Fang);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		if(event.equalsIgnoreCase("elliasin_q0316_04.htm") && st.getPlayer().getRace() == Race.ELF && st.getPlayer().getLevel() >= 18)
		{
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("elliasin_q0316_08.htm"))
		{
			st.finishQuest();
		}
		return event;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		if(npc.getNpcId() != Ellenia)
			return htmltext;

		int cond = st.getCond();
		if(cond == 0)
		{
			if(st.getPlayer().getRace() != Race.ELF)
				htmltext = "elliasin_q0316_00.htm";
			else if(st.getPlayer().getLevel() < 18)
				htmltext = "elliasin_q0316_02.htm";
			else
				htmltext = "elliasin_q0316_03.htm";
		}
		else
		{
			long Reward = (st.getQuestItemsCount(Wererats_Fang) * 60) + (st.getQuestItemsCount(Varool_Foulclaws_Fang) * 10000L);
			if(Reward > 0)
			{
				htmltext = "elliasin_q0316_07.htm";
				st.takeItems(Wererats_Fang, -1);
				st.takeItems(Varool_Foulclaws_Fang, -1);
				st.giveItems(ADENA_ID, Reward, true, true);
				st.playSound(SOUND_MIDDLE);
			}
			else
				htmltext = "elliasin_q0316_05.htm";
		}

		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState qs)
	{
		if(qs.isStarted())
		{
			if(npc.getNpcId() == Varool_Foulclaw && qs.getQuestItemsCount(Varool_Foulclaws_Fang) == 0 && Rnd.chance(Varool_Foulclaws_Fang_Chance))
			{
				qs.giveItems(Varool_Foulclaws_Fang, 1, true, true);
				qs.playSound(SOUND_ITEMGET);
			}
			else if(Rnd.chance(Wererats_Fang_Chance))
			{
				qs.giveItems(Wererats_Fang, 1, true, true);
				qs.playSound(SOUND_ITEMGET);
			}
		}
		return null;
	}
}