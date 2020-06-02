package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _274_SkirmishWithTheWerewolves extends QuestScript
{

	private static final int MARAKU_WEREWOLF_HEAD = 1477;
	private static final int NECKLACE_OF_VALOR = 1507;
	private static final int NECKLACE_OF_COURAGE = 1506;
	private static final int MARAKU_WOLFMEN_TOTEM = 1501;

	public _274_SkirmishWithTheWerewolves()
	{
		super(PARTY_NONE, REPEATABLE);
		addStartNpc(30569);

		addKillId(20363);
		addKillId(20364);

		addQuestItem(MARAKU_WEREWOLF_HEAD);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equals("prefect_brukurse_q0274_03.htm"))
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
		if(npcId == 30569)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getRace() != Race.ORC)
					htmltext = "prefect_brukurse_q0274_00.htm";
				else if(st.getPlayer().getLevel() < 9)
					htmltext = "prefect_brukurse_q0274_01.htm";
				else if(st.getQuestItemsCount(NECKLACE_OF_VALOR) > 0 || st.getQuestItemsCount(NECKLACE_OF_COURAGE) > 0)
					htmltext = "prefect_brukurse_q0274_02.htm";
				else
					htmltext = "prefect_brukurse_q0274_07.htm";
			}
			else if(cond == 1)
				htmltext = "prefect_brukurse_q0274_04.htm";
			else if(cond == 2)
			{
				if(st.getQuestItemsCount(MARAKU_WEREWOLF_HEAD) < 40)
					htmltext = "prefect_brukurse_q0274_04.htm";
				else
				{
					st.takeItems(MARAKU_WEREWOLF_HEAD, -1);
					st.giveItems(ADENA_ID, 3500, true, true);
					if(st.getQuestItemsCount(MARAKU_WOLFMEN_TOTEM) >= 1)
					{
						st.giveItems(ADENA_ID, st.getQuestItemsCount(MARAKU_WOLFMEN_TOTEM) * 600, true, true);
						st.takeItems(MARAKU_WOLFMEN_TOTEM, -1);
					}
					htmltext = "prefect_brukurse_q0274_05.htm";
					st.finishQuest();
				}
			}
		}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.getCond() == 1 && st.getQuestItemsCount(MARAKU_WEREWOLF_HEAD) < 40)
		{
			if(st.getQuestItemsCount(MARAKU_WEREWOLF_HEAD) < 39)
				st.playSound(SOUND_ITEMGET);
			else
			{
				st.setCond(2);
			}
			st.giveItems(MARAKU_WEREWOLF_HEAD, 1, true, true);
		}
		if(Rnd.chance(5))
			st.giveItems(MARAKU_WOLFMEN_TOTEM, 1, true, true);
		return null;
	}
}