package quests;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _607_ProveYourCourage extends QuestScript
{
	private final static int KADUN_ZU_KETRA = 31370;
	private final static int VARKAS_HERO_SHADITH = 25309;

	// Quest items
	private final static int HEAD_OF_SHADITH = 7235;
	private final static int TOTEM_OF_VALOR = 7219;

	// etc
	@SuppressWarnings("unused")
	private final static int MARK_OF_KETRA_ALLIANCE1 = 7211;
	@SuppressWarnings("unused")
	private final static int MARK_OF_KETRA_ALLIANCE2 = 7212;
	private final static int MARK_OF_KETRA_ALLIANCE3 = 7213;
	private final static int MARK_OF_KETRA_ALLIANCE4 = 7214;
	private final static int MARK_OF_KETRA_ALLIANCE5 = 7215;

	public _607_ProveYourCourage()
	{
		super(PARTY_ONE, REPEATABLE);

		addStartNpc(KADUN_ZU_KETRA);
		addKillId(VARKAS_HERO_SHADITH);

		addQuestItem(HEAD_OF_SHADITH);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equals("quest_accept"))
		{
			htmltext = "elder_kadun_zu_ketra_q0607_0104.htm";
			st.setCond(1);
		}
		else if(event.equals("607_3"))
			if(st.getQuestItemsCount(HEAD_OF_SHADITH) >= 1)
			{
				htmltext = "elder_kadun_zu_ketra_q0607_0201.htm";
				st.takeItems(HEAD_OF_SHADITH, -1);
				st.giveItems(TOTEM_OF_VALOR, 1, false, false);
				st.addExpAndSp(0, 10000);
				st.unset("cond");
				st.finishQuest();
			}
			else
				htmltext = "elder_kadun_zu_ketra_q0607_0106.htm";
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
				if(st.getQuestItemsCount(MARK_OF_KETRA_ALLIANCE3) >= 1 || st.getQuestItemsCount(MARK_OF_KETRA_ALLIANCE4) >= 1 || st.getQuestItemsCount(MARK_OF_KETRA_ALLIANCE5) >= 1)
					htmltext = "elder_kadun_zu_ketra_q0607_0101.htm";
				else
					htmltext = "elder_kadun_zu_ketra_q0607_0102.htm";
			}
			else
				htmltext = "elder_kadun_zu_ketra_q0607_0103.htm";
		}
		else if(cond == 1 && st.getQuestItemsCount(HEAD_OF_SHADITH) == 0)
			htmltext = "elder_kadun_zu_ketra_q0607_0106.htm";
		else if(cond == 2 && st.getQuestItemsCount(HEAD_OF_SHADITH) >= 1)
			htmltext = "elder_kadun_zu_ketra_q0607_0105.htm";
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		if(npcId == VARKAS_HERO_SHADITH && st.getCond() == 1)
		{
			st.giveItems(HEAD_OF_SHADITH, 1, false, false);
			st.setCond(2);
			st.playSound(SOUND_ITEMGET);
		}
		return null;
	}
}