package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _347_GoGetTheCalculator extends QuestScript
{
	//npc
	public final int BRUNON = 30526;
	public final int SILVERA = 30527;
	public final int SPIRON = 30532;
	public final int BALANKI = 30533;
	//mob
	public final int GEMSTONE_BEAST = 20540;
	//quest items
	public final int GEMSTONE_BEAST_CRYSTAL = 4286;
	public final int CALCULATOR_Q = 4285;
	public final int CALCULATOR = 4393;

	public _347_GoGetTheCalculator()
	{
		super(PARTY_NONE, REPEATABLE);

		addStartNpc(BRUNON);

		addTalkId(SILVERA);
		addTalkId(SPIRON);
		addTalkId(BALANKI);

		addKillId(GEMSTONE_BEAST);

		addQuestItem(GEMSTONE_BEAST_CRYSTAL);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("1"))
		{
			st.setCond(1);
			htmltext = BRUNON + "-02.htm";
		}
		else if(event.equalsIgnoreCase("30533_1"))
			if(st.getQuestItemsCount(ADENA_ID) > 100)
			{
				st.takeItems(ADENA_ID, 100);
				if(st.getCond() == 1)
					st.setCond(2);
				else
					st.setCond(4);
				htmltext = BALANKI + "-02.htm";
			}
			else
				htmltext = BALANKI + "-03.htm";
		else if(event.equalsIgnoreCase("30532_1"))
		{
			htmltext = SPIRON + "-02a.htm";
			if(st.getCond() == 1)
				st.setCond(3);
			else
				st.setCond(4);
		}
		else if(event.equalsIgnoreCase("30532_2"))
			htmltext = SPIRON + "-02b.htm";
		else if(event.equalsIgnoreCase("30532_3"))
			htmltext = SPIRON + "-02c.htm";
		else if(event.equalsIgnoreCase("30526_1"))
		{
			st.giveItems(CALCULATOR, 1, false, false);
			st.takeItems(CALCULATOR_Q, 1);
			st.finishQuest();
			htmltext = BRUNON + "-05.htm";
		}
		else if(event.equalsIgnoreCase("30526_2"))
		{
			st.giveItems(ADENA_ID, 1000, true, true);
			st.takeItems(CALCULATOR_Q, 1);
			st.finishQuest();
			htmltext = BRUNON + "-06.htm";
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		String htmltext = NO_QUEST_DIALOG;
		if(npcId == BRUNON && cond == 0 && st.getPlayer().getLevel() >= 12)
			htmltext = BRUNON + "-01.htm";
		else if(npcId == BRUNON && cond > 0 && st.getQuestItemsCount(CALCULATOR_Q) == 0)
			htmltext = BRUNON + "-03.htm";
		else if(npcId == BRUNON && cond == 6 && st.getQuestItemsCount(CALCULATOR_Q) >= 1)
			htmltext = BRUNON + "-04.htm";
		else if(npcId == BALANKI && (cond == 1 || cond == 3))
			htmltext = BALANKI + "-01.htm";
		else if(npcId == SPIRON && (cond == 1 || cond == 2))
			htmltext = SPIRON + "-01.htm";
		else if(npcId == SILVERA && cond == 4)
		{
			st.setCond(5);
			htmltext = SILVERA + "-01.htm";
		}
		else if(npcId == SILVERA && cond == 5 && st.getQuestItemsCount(GEMSTONE_BEAST_CRYSTAL) < 10)
			htmltext = SILVERA + "-02.htm";
		else if(npcId == SILVERA && cond == 5 && st.getQuestItemsCount(GEMSTONE_BEAST_CRYSTAL) >= 10)
		{
			htmltext = SILVERA + "-03.htm";
			st.takeItems(GEMSTONE_BEAST_CRYSTAL, 10);
			st.giveItems(CALCULATOR_Q, 1, false, false);
			st.playSound(SOUND_ITEMGET);
			st.setCond(6);
		}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		if(npcId == GEMSTONE_BEAST && st.getCond() == 5 && Rnd.chance(50) && st.getQuestItemsCount(GEMSTONE_BEAST_CRYSTAL) < 10)
		{
			st.giveItems(GEMSTONE_BEAST_CRYSTAL, 1, true, true);
			if(st.getQuestItemsCount(GEMSTONE_BEAST_CRYSTAL) >= 10)
				st.playSound(SOUND_MIDDLE);
			else
				st.playSound(SOUND_ITEMGET);
		}
		return null;
	}
}