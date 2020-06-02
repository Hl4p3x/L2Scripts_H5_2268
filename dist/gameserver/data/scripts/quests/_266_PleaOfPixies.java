package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _266_PleaOfPixies extends QuestScript
{

	private static final int PREDATORS_FANG = 1334;
	private static final int EMERALD = 1337;
	private static final int BLUE_ONYX = 1338;
	private static final int ONYX = 1339;
	private static final int GLASS_SHARD = 1336;
	private static final int REC_LEATHER_BOOT = 2176;
	private static final int REC_SPIRITSHOT = 3032;

	public _266_PleaOfPixies()
	{
		super(PARTY_NONE, REPEATABLE);
		addStartNpc(31852);
		addKillId(new int[]{
				20525,
				20530,
				20534,
				20537
		});
		addQuestItem(PREDATORS_FANG);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("pixy_murika_q0266_03.htm"))
		{
			st.setCond(1);
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		if(st.getCond() == 0)
		{
			if(st.getPlayer().getRace() != Race.ELF)
				htmltext = "pixy_murika_q0266_00.htm";
			else if(st.getPlayer().getLevel() < 3)
				htmltext = "pixy_murika_q0266_01.htm";
			else
				htmltext = "pixy_murika_q0266_02.htm";
		}
		else if(st.getQuestItemsCount(PREDATORS_FANG) < 100)
			htmltext = "pixy_murika_q0266_04.htm";
		else
		{
			st.takeItems(PREDATORS_FANG, -1);
			int n = Rnd.get(100);
			if(n < 2)
			{
				st.giveItems(EMERALD, 1, false, false);
				st.giveItems(REC_SPIRITSHOT, 1, false, false);
				st.playSound(SOUND_JACKPOT);
			}
			else if(n < 20)
			{
				st.giveItems(BLUE_ONYX, 1, false, false);
				st.giveItems(REC_LEATHER_BOOT, 1, false, false);
			}
			else if(n < 45)
				st.giveItems(ONYX, 1, false, false);
			else
				st.giveItems(GLASS_SHARD, 1, false, false);
			htmltext = "pixy_murika_q0266_05.htm";
			st.finishQuest();
		}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.getCond() == 1)
			st.rollAndGive(PREDATORS_FANG, 1, 1, 100, 60 + npc.getLevel() * 5);
		return null;
	}
}