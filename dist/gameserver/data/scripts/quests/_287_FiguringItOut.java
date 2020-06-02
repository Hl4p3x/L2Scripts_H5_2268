package quests;

import org.apache.commons.lang3.ArrayUtils;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

/**
 * @author pchayka
 */

public class _287_FiguringItOut extends QuestScript
{
	private static final int Laki = 32742;
	private static final int[] TantaClan = {
			22768,
			22769,
			22770,
			22771,
			22772,
			22773,
			22774
	};
	private static final int VialofTantaBlood = 15499;

	public _287_FiguringItOut()
	{
		super(PARTY_ONE, REPEATABLE);
		addStartNpc(Laki);
		addKillId(TantaClan);
		addQuestItem(VialofTantaBlood);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("laki_q287_03.htm"))
		{
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("request_spitter"))
		{
			if(st.getQuestItemsCount(VialofTantaBlood) >= 500)
			{
				st.takeItems(VialofTantaBlood, 500);
				switch(Rnd.get(1, 5))
				{
					case 1:
						st.giveItems(10381, 1, false, false);
						break;
					case 2:
						st.giveItems(10405, 1, false, false);
						break;
					case 3:
						st.giveItems(10405, 4, false, false);
						break;
					case 4:
						st.giveItems(10405, 4, false, false);
						break;
					case 5:
						st.giveItems(10405, 6, false, false);
						break;
				}
				htmltext = "laki_q287_07.htm";
			}
			else
				htmltext = "laki_q287_06.htm";
		}
		else if(event.equalsIgnoreCase("request_moirai"))
		{
			if(st.getQuestItemsCount(VialofTantaBlood) >= 100)
			{
				st.takeItems(VialofTantaBlood, 100);
				switch(Rnd.get(1, 16))
				{
					case 1:
						st.giveItems(15776, 1, false, false);
						break;
					case 2:
						st.giveItems(15779, 1, false, false);
						break;
					case 3:
						st.giveItems(15782, 1, false, false);
						break;
					case 4:
						st.giveItems(15785, 1, false, false);
						break;
					case 5:
						st.giveItems(15788, 1, false, false);
						break;
					case 6:
						st.giveItems(15812, 1, false, false);
						break;
					case 7:
						st.giveItems(15813, 1, false, false);
						break;
					case 8:
						st.giveItems(15814, 5, false, false);
						break;
					case 9:
						st.giveItems(15646, 5, false, false);
						break;
					case 10:
						st.giveItems(15649, 5, false, false);
						break;
					case 11:
						st.giveItems(15652, 5, false, false);
						break;
					case 12:
						st.giveItems(15655, 5, false, false);
						break;
					case 13:
						st.giveItems(15658, 5, false, false);
						break;
					case 14:
						st.giveItems(15772, 1, false, false);
						break;
					case 15:
						st.giveItems(15773, 1, false, false);
						break;
					case 16:
						st.giveItems(15771, 1, false, false);
						break;
				}
				htmltext = "laki_q287_07.htm";
			}
			else
				htmltext = "laki_q287_10.htm";
		}
		else if(event.equalsIgnoreCase("continue"))
			htmltext = "laki_q287_08.htm";
		else if(event.equalsIgnoreCase("quit"))
		{
			htmltext = "laki_q287_09.htm";
			st.finishQuest();
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == Laki)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 82 && st.getPlayer().isQuestCompleted(250))
					htmltext = "laki_q287_01.htm";
				else
					htmltext = "laki_q287_00.htm";
			}
			else if(cond == 1 && st.getQuestItemsCount(VialofTantaBlood) < 100)
				htmltext = "laki_q287_04.htm";
			else if(cond == 1 && st.getQuestItemsCount(VialofTantaBlood) >= 100)
				htmltext = "laki_q287_05.htm";
		}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(cond == 1)
		{
			if(ArrayUtils.contains(TantaClan, npcId) && Rnd.chance(60))
				st.giveItems(VialofTantaBlood, 1, true, true);
		}
		return null;
	}
}