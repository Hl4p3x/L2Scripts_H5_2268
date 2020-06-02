package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _360_PlunderTheirSupplies extends QuestScript
{
	//NPC
	private static final int COLEMAN = 30873;

	//MOBS
	private static final int TAIK_SEEKER = 20666;
	private static final int TAIK_LEADER = 20669;

	//QUEST ITEMS
	private static final int SUPPLY_ITEM = 5872;
	private static final int SUSPICIOUS_DOCUMENT = 5871;
	private static final int RECIPE_OF_SUPPLY = 5870;

	//DROP CHANCES
	private static final int ITEM_DROP_SEEKER = 50;
	private static final int ITEM_DROP_LEADER = 65;
	private static final int DOCUMENT_DROP = 5;

	public _360_PlunderTheirSupplies()
	{
		super(PARTY_NONE, REPEATABLE);
		addStartNpc(COLEMAN);

		addKillId(TAIK_SEEKER);
		addKillId(TAIK_LEADER);

		addQuestItem(SUPPLY_ITEM);
		addQuestItem(SUSPICIOUS_DOCUMENT);
		addQuestItem(RECIPE_OF_SUPPLY);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("guard_coleman_q0360_04.htm"))
		{
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("guard_coleman_q0360_10.htm"))
		{
			st.finishQuest();
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int cond = st.getCond();
		long docs = st.getQuestItemsCount(RECIPE_OF_SUPPLY);
		long supplies = st.getQuestItemsCount(SUPPLY_ITEM);
		if(cond == 0)
		{
			if(st.getPlayer().getLevel() >= 52)
				htmltext = "guard_coleman_q0360_02.htm";
			else
				htmltext = "guard_coleman_q0360_01.htm";
		}
		else if(docs > 0 || supplies > 0)
		{
			long reward = 6000 + supplies * 100 + docs * 6000;
			st.takeItems(SUPPLY_ITEM, -1);
			st.takeItems(RECIPE_OF_SUPPLY, -1);
			st.giveItems(ADENA_ID, reward, true, true);
			htmltext = "guard_coleman_q0360_08.htm";
		}
		else
			htmltext = "guard_coleman_q0360_05.htm";
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		if(npcId == TAIK_SEEKER && Rnd.chance(ITEM_DROP_SEEKER) || npcId == TAIK_LEADER && Rnd.chance(ITEM_DROP_LEADER))
		{
			st.giveItems(SUPPLY_ITEM, 1, true, true);
			st.playSound(SOUND_ITEMGET);
		}
		if(Rnd.chance(DOCUMENT_DROP))
		{
			if(st.getQuestItemsCount(SUSPICIOUS_DOCUMENT) < 4)
				st.giveItems(SUSPICIOUS_DOCUMENT, 1, true, true);
			else
			{
				st.takeItems(SUSPICIOUS_DOCUMENT, -1);
				st.giveItems(RECIPE_OF_SUPPLY, 1, true, true);
			}
			st.playSound(SOUND_ITEMGET);
		}
		return null;
	}
}