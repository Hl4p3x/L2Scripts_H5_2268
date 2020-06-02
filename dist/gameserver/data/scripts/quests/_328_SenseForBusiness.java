package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _328_SenseForBusiness extends QuestScript
{
	//NPC
	private int SARIEN = 30436;
	//items
	private int MONSTER_EYE_CARCASS = 1347;
	private int MONSTER_EYE_LENS = 1366;
	private int BASILISK_GIZZARD = 1348;

	public _328_SenseForBusiness()
	{
		super(PARTY_NONE, REPEATABLE);

		addStartNpc(SARIEN);
		addKillId(20055);
		addKillId(20059);
		addKillId(20067);
		addKillId(20068);
		addKillId(20070);
		addKillId(20072);
		addQuestItem(MONSTER_EYE_CARCASS);
		addQuestItem(MONSTER_EYE_LENS);
		addQuestItem(BASILISK_GIZZARD);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		if(event.equalsIgnoreCase("trader_salient_q0328_03.htm"))
		{
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("trader_salient_q0328_06.htm"))
		{
			st.finishQuest();
		}
		return event;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext;
		if(st.getCond() == 0)
		{
			if(st.getPlayer().getLevel() >= 21)
			{
				htmltext = "trader_salient_q0328_02.htm";
				return htmltext;
			}
			htmltext = "trader_salient_q0328_01.htm";
		}
		else
		{
			long carcass = st.getQuestItemsCount(MONSTER_EYE_CARCASS);
			long lenses = st.getQuestItemsCount(MONSTER_EYE_LENS);
			long gizzard = st.getQuestItemsCount(BASILISK_GIZZARD);
			if(carcass + lenses + gizzard > 0)
			{
				st.giveItems(ADENA_ID, (30 * carcass) + (2000 * lenses) + (75 * gizzard), true, true);
				st.takeItems(MONSTER_EYE_CARCASS, -1);
				st.takeItems(MONSTER_EYE_LENS, -1);
				st.takeItems(BASILISK_GIZZARD, -1);
				htmltext = "trader_salient_q0328_05.htm";
			}
			else
				htmltext = "trader_salient_q0328_04.htm";
		}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int n = Rnd.get(1, 100);
		if(npcId == 20055)
		{
			if(n < 47)
			{
				st.giveItems(MONSTER_EYE_CARCASS, 1, true, true);
				st.playSound(SOUND_ITEMGET);
			}
			else if(n < 49)
			{
				st.giveItems(MONSTER_EYE_LENS, 1, true, true);
				st.playSound(SOUND_ITEMGET);
			}
		}
		else if(npcId == 20059)
		{
			if(n < 51)
			{
				st.giveItems(MONSTER_EYE_CARCASS, 1, true, true);
				st.playSound(SOUND_ITEMGET);
			}
			else if(n < 53)
			{
				st.giveItems(MONSTER_EYE_LENS, 1, true, true);
				st.playSound(SOUND_ITEMGET);
			}
		}
		else if(npcId == 20067)
		{
			if(n < 67)
			{
				st.giveItems(MONSTER_EYE_CARCASS, 1, true, true);
				st.playSound(SOUND_ITEMGET);
			}
			else if(n < 69)
			{
				st.giveItems(MONSTER_EYE_LENS, 1, true, true);
				st.playSound(SOUND_ITEMGET);
			}
		}
		else if(npcId == 20068)
		{
			if(n < 75)
			{
				st.giveItems(MONSTER_EYE_CARCASS, 1, true, true);
				st.playSound(SOUND_ITEMGET);
			}
			else if(n < 77)
			{
				st.giveItems(MONSTER_EYE_LENS, 1, true, true);
				st.playSound(SOUND_ITEMGET);
			}
		}
		else if(npcId == 20070)
		{
			if(n < 50)
			{
				st.giveItems(BASILISK_GIZZARD, 1, true, true);
				st.playSound(SOUND_ITEMGET);
			}
		}
		else if(npcId == 20072)
			if(n < 51)
			{
				st.giveItems(BASILISK_GIZZARD, 1, true, true);
				st.playSound(SOUND_ITEMGET);
			}
		return null;
	}
}