package quests;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _624_TheFinestIngredientsPart1 extends QuestScript
{
	//NPC
	private static int JEREMY = 31521;

	//MOBS
	private static int HOT_SPRINGS_ATROX = 21321;
	private static int HOT_SPRINGS_NEPENTHES = 21319;
	private static int HOT_SPRINGS_ATROXSPAWN = 21317;
	private static int HOT_SPRINGS_BANDERSNATCHLING = 21314;

	//QUEST ITEMS
	private static int SECRET_SPICE = 7204;
	private static int TRUNK_OF_NEPENTHES = 7202;
	private static int FOOT_OF_BANDERSNATCHLING = 7203;
	private static int CRYOLITE = 7080;
	private static int SAUCE = 7205;

	public _624_TheFinestIngredientsPart1()
	{
		super(PARTY_ONE, REPEATABLE);

		addStartNpc(JEREMY);

		addKillId(HOT_SPRINGS_ATROX);
		addKillId(HOT_SPRINGS_NEPENTHES);
		addKillId(HOT_SPRINGS_ATROXSPAWN);
		addKillId(HOT_SPRINGS_BANDERSNATCHLING);

		addQuestItem(TRUNK_OF_NEPENTHES);
		addQuestItem(FOOT_OF_BANDERSNATCHLING);
		addQuestItem(SECRET_SPICE);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("jeremy_q0624_0104.htm"))
		{
			if(st.getPlayer().getLevel() >= 73)
				st.setCond(1);
			else
				htmltext = "jeremy_q0624_0103.htm";
		}
		else if(event.equalsIgnoreCase("jeremy_q0624_0201.htm"))
			if(st.getQuestItemsCount(TRUNK_OF_NEPENTHES) >= 50 && st.getQuestItemsCount(FOOT_OF_BANDERSNATCHLING) >= 50 && st.getQuestItemsCount(SECRET_SPICE) >= 50)
			{
				st.takeItems(TRUNK_OF_NEPENTHES, -1);
				st.takeItems(FOOT_OF_BANDERSNATCHLING, -1);
				st.takeItems(SECRET_SPICE, -1);
				st.giveItems(SAUCE, 1, false, false);
				st.giveItems(CRYOLITE, 1, false, false);
				htmltext = "jeremy_q0624_0201.htm";
				st.finishQuest();
			}
			else
			{
				htmltext = "jeremy_q0624_0202.htm";
				st.setCond(1);
			}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int cond = st.getCond();
		if(cond == 0)
			htmltext = "jeremy_q0624_0101.htm";
		else if(cond != 3)
			htmltext = "jeremy_q0624_0106.htm";
		else
			htmltext = "jeremy_q0624_0105.htm";
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(!st.isStarted())
			return null;
		int npcId = npc.getNpcId();
		if(st.getCond() == 1)
		{
			if(npcId == HOT_SPRINGS_NEPENTHES && st.getQuestItemsCount(TRUNK_OF_NEPENTHES) < 50)
				st.rollAndGive(TRUNK_OF_NEPENTHES, 1, 1, 50, 100);
			else if(npcId == HOT_SPRINGS_BANDERSNATCHLING && st.getQuestItemsCount(FOOT_OF_BANDERSNATCHLING) < 50)
				st.rollAndGive(FOOT_OF_BANDERSNATCHLING, 1, 1, 50, 100);
			else if((npcId == HOT_SPRINGS_ATROX || npcId == HOT_SPRINGS_ATROXSPAWN) && st.getQuestItemsCount(SECRET_SPICE) < 50)
				st.rollAndGive(SECRET_SPICE, 1, 1, 50, 100);
			onKillCheck(st);
		}
		return null;
	}

	private void onKillCheck(QuestState st)
	{
		if(st.getQuestItemsCount(TRUNK_OF_NEPENTHES) >= 50 && st.getQuestItemsCount(FOOT_OF_BANDERSNATCHLING) >= 50 && st.getQuestItemsCount(SECRET_SPICE) >= 50)
		{
			st.setCond(3);
		}
		else
			st.playSound(SOUND_ITEMGET);
	}
}