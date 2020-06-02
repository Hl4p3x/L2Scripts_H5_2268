package quests;

import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.utils.Util;

public class _508_TheClansReputation extends QuestScript
{
	// Quest NPC
	private static final int SIR_ERIC_RODEMAI = 30868;

	// Quest Items
	private static final int NUCLEUS_OF_FLAMESTONE_GIANT = 8494; // Nucleus of Flamestone Giant : Nucleus obtained by defeating Flamestone Giant
	private static final int THEMIS_SCALE = 8277; // Themis' Scale : Obtain this scale by defeating Palibati Queen Themis.
	private static final int Hisilromes_Heart = 14883; // Heart obtained after defeating Shilen's Priest Hisilrome.
	private static final int TIPHON_SHARD = 8280; // Tiphon Shard : Debris obtained by defeating Tiphon, Gargoyle Lord.
	private static final int GLAKIS_NECLEUS = 8281; // Glaki's Necleus : Nucleus obtained by defeating Glaki, the last lesser Giant.
	private static final int RAHHAS_FANG = 8282; // Rahha's Fang : Fangs obtained by defeating Rahha.

	// Quest Raid Bosses
	private static final int FLAMESTONE_GIANT = 25524;
	private static final int PALIBATI_QUEEN_THEMIS = 25252;
	private static final int Shilens_Priest_Hisilrome = 25478;
	private static final int GARGOYLE_LORD_TIPHON = 25255;
	private static final int LAST_LESSER_GIANT_GLAKI = 25245;
	private static final int RAHHA = 25051;

	// id:[RaidBossNpcId,questItemId]
	private static final int[][] REWARDS_LIST = {
			{
					0,
					0
			},
			{
					PALIBATI_QUEEN_THEMIS,
					THEMIS_SCALE,
					85
			},
			{
					Shilens_Priest_Hisilrome,
					Hisilromes_Heart,
					65
			},
			{
					GARGOYLE_LORD_TIPHON,
					TIPHON_SHARD,
					50
			},
			{
					LAST_LESSER_GIANT_GLAKI,
					GLAKIS_NECLEUS,
					125
			},
			{
					RAHHA,
					RAHHAS_FANG,
					71
			},
			{
					FLAMESTONE_GIANT,
					NUCLEUS_OF_FLAMESTONE_GIANT,
					80
			}
	};

	private static final int[][] RADAR = {
			{
					0,
					0,
					0
			},
			{
					192346,
					21528,
					-3648
			},
			{
					191979,
					54902,
					-7658
			},
			{
					170038,
					-26236,
					-3824
			},
			{
					171762,
					55028,
					-5992
			},
			{
					117232,
					-9476,
					-3320
			},
			{
					144218,
					-5816,
					-4722
			},
	};

	public _508_TheClansReputation()
	{
		super(PARTY_ALL, REPEATABLE);

		addStartNpc(SIR_ERIC_RODEMAI);

		for(int[] i : REWARDS_LIST)
		{
			if(i[0] > 0)
				addKillId(i[0]);
			if(i[1] > 0)
				addQuestItem(i[1]);
		}
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		int cond = st.getCond();
		String htmltext = event;
		if(event.equalsIgnoreCase("30868-0.htm") && cond == 0)
		{
			st.setCond(1);
		}
		else if(Util.isNumber(event))
		{
			int evt = Integer.parseInt(event);
			st.set("raid", event);
			htmltext = "30868-" + event + ".htm";
			int x = RADAR[evt][0];
			int y = RADAR[evt][1];
			int z = RADAR[evt][2];
			if(x + y + z > 0)
				st.addRadar(x, y, z);
		}
		else if(event.equalsIgnoreCase("30868-7.htm"))
		{
			st.finishQuest();
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		Clan clan = st.getPlayer().getClan();

		if(clan == null)
		{
			st.abortQuest();
			htmltext = "30868-0a.htm";
		}
		else if(clan.getLeader().getPlayer() != st.getPlayer())
		{
			st.abortQuest();
			htmltext = "30868-0a.htm";
		}
		else if(clan.getLevel() < 5)
		{
			st.abortQuest();
			htmltext = "30868-0b.htm";
		}
		else
		{
			int cond = st.getCond();
			int raid = st.getInt("raid");
			if(cond == 0)
				htmltext = "30868-0c.htm";
			else if(cond == 1)
			{
				int item = REWARDS_LIST[raid][1];
				long count = st.getQuestItemsCount(item);
				if(count == 0)
					htmltext = "30868-" + raid + "a.htm";
				else if(count == 1)
				{
					htmltext = "30868-" + raid + "b.htm";
					int increasedPoints = clan.incReputation(REWARDS_LIST[raid][2], true, "_508_TheClansReputation");
					st.getPlayer().sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_SUCCESSFULLY_COMPLETED_A_CLAN_QUEST_S1_POINTS_HAVE_BEEN_ADDED_TO_YOUR_CLAN_REPUTATION_SCORE).addNumber(increasedPoints));
					st.takeItems(item, 1);
				}
			}
		}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		Player clan_leader;
		try
		{
			clan_leader = st.getPlayer().getClan().getLeader().getPlayer();
		}
		catch(Exception E)
		{
			return null;
		}
		if(clan_leader == null)
			return null;
		if(!st.getPlayer().equals(clan_leader) && clan_leader.getDistance(npc) > Config.ALT_PARTY_DISTRIBUTION_RANGE)
			return null;
		QuestState qs = clan_leader.getQuestState(this);
		if(qs == null || !qs.isStarted() || qs.getCond() != 1)
			return null;

		int raid = REWARDS_LIST[st.getInt("raid")][0];
		int item = REWARDS_LIST[st.getInt("raid")][1];
		if(npc.getNpcId() == raid && st.getQuestItemsCount(item) == 0)
		{
			st.giveItems(item, 1, false, false);
			st.playSound(SOUND_MIDDLE);
		}

		return null;
	}
}