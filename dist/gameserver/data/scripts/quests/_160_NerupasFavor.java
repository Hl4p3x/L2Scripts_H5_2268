package quests;

import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _160_NerupasFavor extends QuestScript
{
	private static int SILVERY_SPIDERSILK = 1026;
	private static int UNOS_RECEIPT = 1027;
	private static int CELS_TICKET = 1028;
	private static int NIGHTSHADE_LEAF = 1029;
	private static int LESSER_HEALING_POTION = 1060;

	private static int NERUPA = 30370;
	private static int UNOREN = 30147;
	private static int CREAMEES = 30149;
	private static int JULIA = 30152;

	/**
	 * Delivery of Goods
	 * Trader Unoren asked Nerupa to collect silvery spidersilks for him.
	 * Norupa doesn't want to enter the village and asks you to deliver the silvery spidersilks to Trader Unoren in the weapons shop and bring back a nightshade leaf.	 *
	 */
	private static int COND1 = 1;

	/**
	 * Nightshade Leaf
	 * Nightshade leaves are very rare. Fortunately, Trader Creamees of the magic shop has obtained a few of them. Go see him with Unoren's receipt.
	 */
	private static int COND2 = 2;

	/**
	 * Go to the Warehouse
	 * Since nightshade leaf is so rare it has been stored in the warehouse. Take Creamees' ticket to Warehouse Keeper Julia.
	 */
	private static int COND3 = 3;

	/**
	 * Goods to be Delivered to Nerupa
	 * You've obtained the nightshade leaf that Creamees stored in the warehouse. Deliver it to Nerupa.
	 */
	private static int COND4 = 4;

	public _160_NerupasFavor()
	{
		super(PARTY_NONE, ONETIME);

		addStartNpc(NERUPA);

		addTalkId(UNOREN, CREAMEES, JULIA);

		addQuestItem(SILVERY_SPIDERSILK, UNOS_RECEIPT, CELS_TICKET, NIGHTSHADE_LEAF);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equals("30370-04.htm"))
		{
			st.setCond(COND1);
			st.giveItems(SILVERY_SPIDERSILK, 1, false, false);
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == NERUPA)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getRace() != Race.ELF)
					htmltext = "30370-00.htm";
				else if(st.getPlayer().getLevel() < 3)
					htmltext = "30370-02.htm";
				else
					htmltext = "30370-03.htm";
			}
			else if(cond == COND1)
				htmltext = "30370-04.htm";
			else if(cond == COND4 && st.getQuestItemsCount(NIGHTSHADE_LEAF) > 0)
			{
				st.takeItems(NIGHTSHADE_LEAF, -1);
				st.giveItems(LESSER_HEALING_POTION, 5, false, false);
				st.addExpAndSp(1000, 0);
				htmltext = "30370-06.htm";
				st.finishQuest();
			}
			else
				htmltext = "30370-05.htm";
		}
		else if(npcId == UNOREN)
		{
			if(cond == COND1)
			{
				st.takeItems(SILVERY_SPIDERSILK, -1);
				st.giveItems(UNOS_RECEIPT, 1, false, false);
				st.setCond(COND2);
				htmltext = "30147-01.htm";
			}
			else if(cond == COND2 || cond == COND3)
				htmltext = "30147-02.htm";
			else if(cond == COND4)
				htmltext = "30147-03.htm";
		}
		else if(npcId == CREAMEES)
		{
			if(cond == COND2)
			{
				st.takeItems(UNOS_RECEIPT, -1);
				st.giveItems(CELS_TICKET, 1, false, false);
				st.setCond(COND3);
				htmltext = "30149-01.htm";
			}
			else if(cond == COND3)
				htmltext = "30149-02.htm";
			else if(cond == COND4)
				htmltext = "30149-03.htm";
		}
		else if(npcId == JULIA)
			if(cond == COND3)
			{
				st.takeItems(CELS_TICKET, -1);
				st.giveItems(NIGHTSHADE_LEAF, 1, false, false);
				htmltext = "30152-01.htm";
				st.setCond(COND4);
			}
			else if(cond == COND4)
				htmltext = "30152-02.htm";
		return htmltext;
	}
}