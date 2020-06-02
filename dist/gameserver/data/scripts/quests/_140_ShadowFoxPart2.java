package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.data.QuestHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.Quest;
import l2s.gameserver.model.quest.QuestState;

public class _140_ShadowFoxPart2 extends QuestScript
{
	// NPCs
	private final static int KLUCK = 30895;
	private final static int XENOVIA = 30912;

	// Items
	private final static int CRYSTAL = 10347;
	private final static int OXYDE = 10348;
	private final static int CRYPT = 10349;

	// Monsters
	private final static int Crokian = 20789;
	private final static int Dailaon = 20790;
	private final static int CrokianWarrior = 20791;
	private final static int Farhite = 20792;

	public _140_ShadowFoxPart2()
	{
		super(PARTY_NONE, ONETIME);

		// Нет стартового NPC, чтобы квест не появлялся в списке раньше времени
		addFirstTalkId(KLUCK);
		addTalkId(KLUCK, XENOVIA);
		addQuestItem(CRYSTAL, OXYDE, CRYPT);
		addKillId(Crokian, Dailaon, CrokianWarrior, Farhite);
	}

	@Override
	public String onFirstTalk(NpcInstance npc, Player player)
	{
		if(player.isQuestCompleted(139) && player.getQuestState(this) == null)
			newQuestState(player);
		return "";
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("30895-02.htm"))
		{
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("30895-05.htm"))
		{
			st.setCond(2);
		}
		else if(event.equalsIgnoreCase("30895-09.htm"))
		{
			st.giveItems(ADENA_ID, 18775, true, true);
			st.addExpAndSp(30000, 2000);
			Quest q = QuestHolder.getInstance().getQuest(141);
			if(q != null)
				q.newQuestState(st.getPlayer());
			st.finishQuest();
		}
		else if(event.equalsIgnoreCase("30912-07.htm"))
		{
			st.setCond(3);
		}
		else if(event.equalsIgnoreCase("30912-09.htm"))
		{
			st.takeItems(CRYSTAL, 5);
			if(Rnd.chance(60))
			{
				st.giveItems(OXYDE, 1, true, true);
				if(st.getQuestItemsCount(OXYDE) >= 3)
				{
					htmltext = "30912-09b.htm";
					st.setCond(4);
					st.takeItems(CRYSTAL, -1);
					st.takeItems(OXYDE, -1);
					st.giveItems(CRYPT, 1, false, false);
				}
			}
			else
				htmltext = "30912-09a.htm";
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		String htmltext = NO_QUEST_DIALOG;
		if(npcId == KLUCK)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 37)
					htmltext = "30895-01.htm";
				else
					htmltext = "30895-00.htm";
			}
			else if(cond == 1)
				htmltext = "30895-02.htm";
			else if(cond == 2 || cond == 3)
				htmltext = "30895-06.htm";
			else if(cond == 4)
				if(st.getInt("talk") == 1)
					htmltext = "30895-08.htm";
				else
				{
					htmltext = "30895-07.htm";
					st.takeItems(CRYPT, -1);
					st.set("talk", "1");
				}
		}
		else if(npcId == XENOVIA)
			if(cond == 2)
				htmltext = "30912-01.htm";
			else if(cond == 3)
				if(st.getQuestItemsCount(CRYSTAL) >= 5)
					htmltext = "30912-08.htm";
				else
					htmltext = "30912-07.htm";
			else if(cond == 4)
				htmltext = "30912-10.htm";
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.getCond() == 3)
			st.rollAndGive(CRYSTAL, 1, 80 * npc.getTemplate().rateHp);
		return null;
	}
}