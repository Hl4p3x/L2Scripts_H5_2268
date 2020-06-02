package quests;

import java.util.HashMap;
import java.util.Map;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;


public class _633_InTheForgottenVillage extends QuestScript
{
	// NPC
	private static int MINA = 31388;
	// ITEMS
	private static int RIB_BONE = 7544;
	private static int Z_LIVER = 7545;

	// Mobid : DROP CHANCES
	private static Map<Integer, Double> DAMOBS = new HashMap<Integer, Double>();
	private static Map<Integer, Double> UNDEADS = new HashMap<Integer, Double>();

	public _633_InTheForgottenVillage()
	{
		super(PARTY_ONE, REPEATABLE);

		DAMOBS.put(21557, 32.8); // Bone Snatcher
		DAMOBS.put(21558, 32.8); // Bone Snatcher
		DAMOBS.put(21559, 33.7); // Bone Maker
		DAMOBS.put(21560, 33.7); // Bone Shaper
		DAMOBS.put(21563, 34.2); // Bone Collector
		DAMOBS.put(21564, 34.8); // Skull Collector
		DAMOBS.put(21565, 35.1); // Bone Animator
		DAMOBS.put(21566, 35.9); // Skull Animator
		DAMOBS.put(21567, 35.9); // Bone Slayer
		DAMOBS.put(21572, 36.5); // Bone Sweeper
		DAMOBS.put(21574, 38.3); // Bone Grinder
		DAMOBS.put(21575, 38.3); // Bone Grinder
		DAMOBS.put(21580, 38.5); // Bone Caster
		DAMOBS.put(21581, 39.5); // Bone Puppeteer
		DAMOBS.put(21583, 39.7); // Bone Scavenger
		DAMOBS.put(21584, 40.1); // Bone Scavenger

		UNDEADS.put(21553, 34.7); // Trampled Man
		UNDEADS.put(21554, 34.7); // Trampled Man
		UNDEADS.put(21561, 45.0); // Sacrificed Man
		UNDEADS.put(21578, 50.1); // Behemoth Zombie
		UNDEADS.put(21596, 35.9); // Requiem Lord
		UNDEADS.put(21597, 37.0); // Requiem Behemoth
		UNDEADS.put(21598, 44.1); // Requiem Behemoth
		UNDEADS.put(21599, 39.5); // Requiem Priest
		UNDEADS.put(21600, 40.8); // Requiem Behemoth
		UNDEADS.put(21601, 41.1); // Requiem Behemoth

		addStartNpc(MINA);
		addQuestItem(RIB_BONE);

		for(int i : UNDEADS.keySet())
			addKillId(i);

		for(int i : DAMOBS.keySet())
			addKillId(i);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("quest_accept"))
		{
			st.setCond(1);
			htmltext = "day_mina_q0633_0104.htm";
		}
		if(event.equalsIgnoreCase("633_4"))
		{
			st.takeItems(RIB_BONE, -1);
			htmltext = "day_mina_q0633_0204.htm";
			st.finishQuest();
		}
		else if(event.equalsIgnoreCase("633_1"))
			htmltext = "day_mina_q0633_0201.htm";
		else if(event.equalsIgnoreCase("633_3"))
			if(st.getCond() == 2)
				if(st.getQuestItemsCount(RIB_BONE) >= 200)
				{
					st.takeItems(RIB_BONE, -1);
					st.giveItems(ADENA_ID, 25000, true, true);
					st.addExpAndSp(305235, 0);
					st.setCond(1);
					htmltext = "day_mina_q0633_0202.htm";
				}
				else
					htmltext = "day_mina_q0633_0203.htm";
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == MINA)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 65)
					htmltext = "day_mina_q0633_0101.htm";
				else
					htmltext = "day_mina_q0633_0103.htm";
			}
			else if(cond == 1)
				htmltext = "day_mina_q0633_0106.htm";
			else if(cond == 2)
				htmltext = "day_mina_q0633_0105.htm";
		}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		if(UNDEADS.containsKey(npcId))
			st.rollAndGive(Z_LIVER, 1, UNDEADS.get(npcId));
		else if(DAMOBS.containsKey(npcId))
		{
			long count = st.getQuestItemsCount(RIB_BONE);
			if(count < 200 && Rnd.chance(DAMOBS.get(npcId)))
			{
				st.giveItems(RIB_BONE, 1, true, true);
				if(st.getQuestItemsCount(RIB_BONE) > 199)
					st.setCond(2);
				else
					st.playSound(SOUND_ITEMGET);
			}
		}
		return null;
	}
}