package quests;

// Created by Artful

import l2s.commons.util.Rnd;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _659_IdRatherBeCollectingFairyBreath extends QuestScript
{
	//NPC
	public final int GALATEA = 30634;
	//Mobs
	public final int[] MOBS = {
			20078,
			21026,
			21025,
			21024,
			21023
	};
	//Quest Item
	public final int FAIRY_BREATH = 8286;

	public _659_IdRatherBeCollectingFairyBreath()
	{
		super(PARTY_NONE, REPEATABLE);

		addStartNpc(GALATEA);
		addTalkId(GALATEA);
		addTalkId(GALATEA);
		addTalkId(GALATEA);

		for(int i : MOBS)
			addKillId(i);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("high_summoner_galatea_q0659_0103.htm"))
		{
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("high_summoner_galatea_q0659_0203.htm"))
		{
			long count = st.getQuestItemsCount(FAIRY_BREATH);
			if(count > 0)
			{
				long reward = 0;
				if(count < 10)
					reward = count * 50;
				else
					reward = (count * 50) + 5365;
				st.takeItems(FAIRY_BREATH, -1);
				st.giveItems(ADENA_ID, reward, true, true);
			}
		}
		else if(event.equalsIgnoreCase("high_summoner_galatea_q0659_0204.htm"))
			st.finishQuest();
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		String htmltext = NO_QUEST_DIALOG;
		int cond = st.getCond();
		if(npcId == GALATEA)
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() < 26)
					htmltext = "high_summoner_galatea_q0659_0102.htm";
				else
					htmltext = "high_summoner_galatea_q0659_0101.htm";
			}
			else if(cond == 1)
				if(st.getQuestItemsCount(FAIRY_BREATH) == 0)
					htmltext = "high_summoner_galatea_q0659_0105.htm";
				else
					htmltext = "high_summoner_galatea_q0659_0105.htm";
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(cond == 1)
			for(int i : MOBS)
				if(npcId == i && Rnd.chance(30))
				{
					st.giveItems(FAIRY_BREATH, 1, true, true);
					st.playSound(SOUND_ITEMGET);
				}
		return null;
	}
}