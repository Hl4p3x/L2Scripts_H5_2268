package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _186_ContractExecution extends QuestScript
{
	private static final int Luka = 31437;
	private static final int Lorain = 30673;
	private static final int Nikola = 30621;

	private static final int Certificate = 10362;
	private static final int MetalReport = 10366;
	private static final int Accessory = 10367;

	private static final int LetoLizardman = 20577;
	private static final int LetoLizardmanArcher = 20578;
	private static final int LetoLizardmanSoldier = 20579;
	private static final int LetoLizardmanWarrior = 20580;
	private static final int LetoLizardmanShaman = 20581;
	private static final int LetoLizardmanOverlord = 20582;
	private static final int TimakOrc = 20583;

	public _186_ContractExecution()
	{
		super(PARTY_NONE, ONETIME);

		addTalkId(Luka, Nikola, Lorain);
		addFirstTalkId(Lorain);
		addKillId(LetoLizardman, LetoLizardmanArcher, LetoLizardmanSoldier, LetoLizardmanWarrior, LetoLizardmanShaman, LetoLizardmanOverlord, TimakOrc);
		addQuestItem(Certificate, MetalReport, Accessory);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("researcher_lorain_q0186_03.htm"))
		{
			st.setCond(1);
			st.takeItems(Certificate, -1);
			st.giveItems(MetalReport, 1, false, false);
		}
		else if(event.equalsIgnoreCase("maestro_nikola_q0186_03.htm"))
		{
			st.setCond(2);
		}
		else if(event.equalsIgnoreCase("blueprint_seller_luka_q0186_06.htm"))
		{
			st.giveItems(ADENA_ID, 105083, true, true);
			st.addExpAndSp(285935, 18711);
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
		if(npcId == Lorain)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() < 41)
					htmltext = "researcher_lorain_q0186_02.htm";
				else
					htmltext = "researcher_lorain_q0186_01.htm";
			}
			else if(cond == 1)
				htmltext = "researcher_lorain_q0186_04.htm";
		}
		else if(npcId == Nikola)
		{
			if(cond == 1)
				htmltext = "maestro_nikola_q0186_01.htm";
			else if(cond == 2)
				htmltext = "maestro_nikola_q0186_04.htm";
		}
		else if(npcId == Luka)
		{
			if(st.getQuestItemsCount(Accessory) <= 0)
				htmltext = "blueprint_seller_luka_q0186_01.htm";
			else
				htmltext = "blueprint_seller_luka_q0186_02.htm";
		}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.getQuestItemsCount(Accessory) <= 0 && st.getCond() == 2 && Rnd.get(5) == 0)
		{
			st.playSound(SOUND_MIDDLE);
			st.giveItems(Accessory, 1, false, false);
		}
		return null;
	}

	@Override
	public String onFirstTalk(NpcInstance npc, Player player)
	{
		if(player.isQuestCompleted(184) && player.getQuestState(this) == null)
			newQuestState(player);
		return "";
	}
}