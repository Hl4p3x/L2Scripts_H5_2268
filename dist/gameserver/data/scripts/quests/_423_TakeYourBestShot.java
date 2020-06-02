package quests;

import org.apache.commons.lang3.ArrayUtils;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.utils.Location;

/**
 * @author pchayka
 */

public class _423_TakeYourBestShot extends QuestScript
{
	private static final int Johnny = 32744;
	private static final int Batracos = 32740;
	private static final int TantaGuard = 18862;
	private static final int SeerUgorosPass = 15496;
	private static final int[] TantaClan = {
			22768,
			22769,
			22770,
			22771,
			22772,
			22773,
			22774
	};

	public _423_TakeYourBestShot()
	{
		super(PARTY_ONE, REPEATABLE);
		addStartNpc(Johnny);
		addTalkId(Batracos);
		addKillId(TantaGuard);
		addKillId(TantaClan);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("johnny_q423_04.htm"))
			st.finishQuest();
		else if(event.equalsIgnoreCase("johnny_q423_05.htm"))
		{
			st.setCond(1);
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == Johnny)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 82 && st.getPlayer().isQuestCompleted(249))
					htmltext = "johnny_q423_01.htm";
				else
					htmltext = "johnny_q423_00.htm";
			}
			else if(cond == 1)
				htmltext = "johnny_q423_06.htm";
			else if(cond == 2)
				htmltext = "johnny_q423_07.htm";
		}
		else if(npcId == Batracos)
		{
			if(cond == 1)
				htmltext = "batracos_q423_01.htm";
			else if(cond == 2)
			{
				htmltext = "batracos_q423_02.htm";
				st.giveItems(SeerUgorosPass, 1, false, false);
				st.finishQuest();
			}
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
			if(ArrayUtils.contains(TantaClan, npcId) && Rnd.chance(2))
			{
				Location loc = st.getPlayer().getLoc();
				addSpawn(TantaGuard, loc.x, loc.y, loc.z, 0, 100, 120000);
			}
			else if(npcId == TantaGuard && st.getQuestItemsCount(SeerUgorosPass) < 1)
				st.setCond(2);
		}
		return null;
	}
}