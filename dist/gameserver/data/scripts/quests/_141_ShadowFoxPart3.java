package quests;

import l2s.gameserver.data.QuestHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.Quest;
import l2s.gameserver.model.quest.QuestState;

public class _141_ShadowFoxPart3 extends QuestScript
{
	// NPC
	private final static int NATOOLS = 30894;

	// Items
	private final static int REPORT = 10350;

	// Monsters
	private final static int CrokianWarrior = 20791;
	private final static int Farhite = 20792;
	private final static int Alligator = 20135;

	public _141_ShadowFoxPart3()
	{
		super(PARTY_NONE, ONETIME);

		// Нет стартового NPC, чтобы квест не появлялся в списке раньше времени
		addFirstTalkId(NATOOLS);
		addTalkId(NATOOLS);
		addQuestItem(REPORT);
		addKillId(CrokianWarrior, Farhite, Alligator);
	}

	@Override
	public String onFirstTalk(NpcInstance npc, Player player)
	{
		if(player.isQuestCompleted(140) && player.getQuestState(this) == null)
			newQuestState(player);
		return "";
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("30894-02.htm"))
		{
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("30894-04.htm"))
		{
			st.setCond(2);
		}
		else if(event.equalsIgnoreCase("30894-15.htm"))
		{
			st.setCond(4);
			st.unset("talk");
		}
		else if(event.equalsIgnoreCase("30894-18.htm"))
		{
			if(st.getInt("reward") != 1)
			{
				st.playSound(SOUND_FINISH);
				st.giveItems(ADENA_ID, 88888, true, true);
				st.addExpAndSp(278005, 17058);
				st.set("reward", "1");
				htmltext = "select.htm";
			}
			else
				htmltext = "select.htm";
		}
		else if(event.equalsIgnoreCase("dawn"))
		{
			Quest q1 = QuestHolder.getInstance().getQuest(142);
			if(q1 != null)
			{
				st.finishQuest();
				QuestState qs1 = q1.newQuestState(st.getPlayer());
				q1.notifyEvent("start", qs1, npc);
				return null;
			}
		}
		else if(event.equalsIgnoreCase("dusk"))
		{
			Quest q1 = QuestHolder.getInstance().getQuest(143);
			if(q1 != null)
			{
				st.finishQuest();
				QuestState qs1 = q1.newQuestState(st.getPlayer());
				q1.notifyEvent("start", qs1, npc);
				return null;
			}
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int cond = st.getCond();
		String htmltext = NO_QUEST_DIALOG;
		if(cond == 0)
		{
			if(st.getPlayer().getLevel() >= 37)
				htmltext = "30894-01.htm";
			else
				htmltext = "30894-00.htm";
		}
		else if(cond == 1)
			htmltext = "30894-02.htm";
		else if(cond == 2)
			htmltext = "30894-05.htm";
		else if(cond == 3)
		{
			if(st.getInt("talk") == 1)
				htmltext = "30894-07.htm";
			else
			{
				htmltext = "30894-06.htm";
				st.takeItems(REPORT, -1);
				st.set("talk", "1");
			}
		}
		else if(cond == 4)
			htmltext = "30894-16.htm";
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.getCond() == 2 && st.rollAndGive(REPORT, 1, 1, 30, 80 * npc.getTemplate().rateHp))
			st.setCond(3);
		return null;
	}
}