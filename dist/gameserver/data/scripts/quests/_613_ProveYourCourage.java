package quests;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _613_ProveYourCourage extends QuestScript
{
	private final static int DURAI = 31377;
	private final static int KETRAS_HERO_HEKATON = 25299;

	// Quest items
	private final static int HEAD_OF_HEKATON = 7240;
	private final static int FEATHER_OF_VALOR = 7229;

	// etc
	@SuppressWarnings("unused")
	private final static int MARK_OF_VARKA_ALLIANCE1 = 7221;
	@SuppressWarnings("unused")
	private final static int MARK_OF_VARKA_ALLIANCE2 = 7222;
	private final static int MARK_OF_VARKA_ALLIANCE3 = 7223;
	private final static int MARK_OF_VARKA_ALLIANCE4 = 7224;
	private final static int MARK_OF_VARKA_ALLIANCE5 = 7225;

	public _613_ProveYourCourage()
	{
		super(PARTY_ONE, REPEATABLE);

		addStartNpc(DURAI);
		addKillId(KETRAS_HERO_HEKATON);

		addQuestItem(HEAD_OF_HEKATON);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equals("quest_accept"))
		{
			htmltext = "elder_ashas_barka_durai_q0613_0104.htm";
			st.setCond(1);
		}
		else if(event.equals("613_3"))
			if(st.getQuestItemsCount(HEAD_OF_HEKATON) >= 1)
			{
				htmltext = "elder_ashas_barka_durai_q0613_0201.htm";
				st.takeItems(HEAD_OF_HEKATON, -1);
				st.giveItems(FEATHER_OF_VALOR, 1, false, false);
				st.addExpAndSp(0, 10000);
				st.unset("cond");
				st.finishQuest();
			}
			else
				htmltext = "elder_ashas_barka_durai_q0613_0106.htm";
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int cond = st.getCond();
		if(cond == 0)
		{
			if(st.getPlayer().getLevel() >= 75)
			{
				if(st.getQuestItemsCount(MARK_OF_VARKA_ALLIANCE3) >= 1 || st.getQuestItemsCount(MARK_OF_VARKA_ALLIANCE4) >= 1 || st.getQuestItemsCount(MARK_OF_VARKA_ALLIANCE5) >= 1)
					htmltext = "elder_ashas_barka_durai_q0613_0101.htm";
				else
					htmltext = "elder_ashas_barka_durai_q0613_0102.htm";
			}
			else
				htmltext = "elder_ashas_barka_durai_q0613_0103.htm";
		}
		else if(cond == 1 && st.getQuestItemsCount(HEAD_OF_HEKATON) == 0)
			htmltext = "elder_ashas_barka_durai_q0613_0106.htm";
		else if(cond == 2 && st.getQuestItemsCount(HEAD_OF_HEKATON) >= 1)
			htmltext = "elder_ashas_barka_durai_q0613_0105.htm";
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		if(npcId == KETRAS_HERO_HEKATON && st.getCond() == 1)
		{
			st.giveItems(HEAD_OF_HEKATON, 1, false, false);
			st.setCond(2);
			st.playSound(SOUND_ITEMGET);
		}
		return null;
	}
}