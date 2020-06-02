package quests;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

/**
 * @author: pchayka
 * @date: 22.06.2010
 */
public class _699_GuardianoftheSkies extends QuestScript
{
	// NPC's
	private static int Lekon = 32557;

	// ITEMS
	private static int VulturesGoldenFeather = 13871;

	// MOB's
	private static int VultureRider1 = 22614;
	private static int VultureRider2 = 22615;
	private static int EliteRider = 25633;
	private static int Valdstone = 25623;

	//Price
	private static int _featherprice = 5000;

	public _699_GuardianoftheSkies()
	{
		super(PARTY_NONE, REPEATABLE);

		addStartNpc(Lekon);
		addTalkId(Lekon);
		addKillId(VultureRider1, VultureRider2, EliteRider, Valdstone);
		addQuestItem(VulturesGoldenFeather);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		int cond = st.getCond();
		String htmltext = event;

		if(event.equals("lekon_q699_2.htm") && cond == 0)
		{
			st.setCond(1);
		}
		else if(event.equals("ex_feathers") && cond == 1)
			if(st.getQuestItemsCount(VulturesGoldenFeather) >= 1)
			{
				st.giveItems(ADENA_ID, st.getQuestItemsCount(VulturesGoldenFeather) * _featherprice, true, true);
				st.takeItems(VulturesGoldenFeather, -1);
				htmltext = "lekon_q699_4.htm";
			}
			else
				htmltext = "lekon_q699_3a.htm";
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int npcId = npc.getNpcId();
		int cond = st.getCond();

		if(npcId == Lekon)
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 75 && st.getPlayer().isQuestCompleted(10273))
					htmltext = "lekon_q699_1.htm";
				else
					htmltext = "lekon_q699_0.htm";
			}
			else if(cond == 1)
				if(st.getQuestItemsCount(VulturesGoldenFeather) >= 1)
					htmltext = "lekon_q699_3.htm";
				else
					htmltext = "lekon_q699_3a.htm";
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(cond == 1)
			if(npcId == VultureRider1 || npcId == VultureRider2 || npcId == EliteRider)
			{
				st.giveItems(VulturesGoldenFeather, 1, true, true);
				st.playSound(SOUND_ITEMGET);
			}
			else if(npcId == Valdstone)
			{
				st.giveItems(VulturesGoldenFeather, 50, true, true);
				st.playSound(SOUND_ITEMGET);
			}
		return null;
	}
}