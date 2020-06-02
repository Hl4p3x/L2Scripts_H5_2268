package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.base.ClassLevel;
import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage.ScreenMessageAlign;

public class _293_HiddenVein extends QuestScript
{
	// NPCs
	private static int Filaur = 30535;
	private static int Chichirin = 30539;
	// Mobs
	private static int Utuku_Orc = 20446;
	private static int Utuku_Orc_Archer = 20447;
	private static int Utuku_Orc_Grunt = 20448;
	// Quest Items
	private static int Chrysolite_Ore = 1488;
	private static int Torn_Map_Fragment = 1489;
	private static int Hidden_Ore_Map = 1490;
	// Chances
	private static int Torn_Map_Fragment_Chance = 5;
	private static int Chrysolite_Ore_Chance = 45;

	public _293_HiddenVein()
	{
		super(PARTY_NONE, ONETIME);
		addStartNpc(Filaur);
		addTalkId(Chichirin);
		addKillId(Utuku_Orc);
		addKillId(Utuku_Orc_Archer);
		addKillId(Utuku_Orc_Grunt);
		addQuestItem(Chrysolite_Ore);
		addQuestItem(Torn_Map_Fragment);
		addQuestItem(Hidden_Ore_Map);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		if(event.equalsIgnoreCase("elder_filaur_q0293_03.htm"))
		{
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("elder_filaur_q0293_06.htm"))
		{
			st.finishQuest();
		}
		else if(event.equalsIgnoreCase("chichirin_q0293_03.htm"))
		{
			if(st.getQuestItemsCount(Torn_Map_Fragment) < 4)
				return "chichirin_q0293_02.htm";
			st.takeItems(Torn_Map_Fragment, 4);
			st.giveItems(Hidden_Ore_Map, 1, false, false);
		}

		return event;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == Filaur)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getRace() != Race.DWARF)
					return "elder_filaur_q0293_00.htm";
				if(st.getPlayer().getLevel() < 6)
					return "elder_filaur_q0293_01.htm";
				return "elder_filaur_q0293_02.htm";
			}
			else
			{
				long Chrysolite_Ore_count = st.getQuestItemsCount(Chrysolite_Ore);
				long Hidden_Ore_Map_count = st.getQuestItemsCount(Hidden_Ore_Map);
				long reward = st.getQuestItemsCount(Chrysolite_Ore) * 10 + st.getQuestItemsCount(Hidden_Ore_Map) * 1000L;
				if(reward == 0)
					return "elder_filaur_q0293_04.htm";

				if(Chrysolite_Ore_count > 0)
					st.takeItems(Chrysolite_Ore, -1);
				if(Hidden_Ore_Map_count > 0)
					st.takeItems(Hidden_Ore_Map, -1);
				st.giveItems(ADENA_ID, reward, true, true);

				if(st.getPlayer().getClassId().isOfLevel(ClassLevel.NONE) && !st.getPlayer().getVarBoolean("p1q2"))
				{
					st.getPlayer().setVar("p1q2", "1", -1);
					st.getPlayer().sendPacket(new ExShowScreenMessage(NpcString.NONE, 5000, ScreenMessageAlign.TOP_CENTER, true, "Acquisition of Soulshot for beginners complete.\n                  Go find the Newbie Guide."));
					QuestState qs = st.getPlayer().getQuestState(255);
					if(qs != null && qs.getInt("Ex") != 10)
					{
						st.showQuestionMark(false, 26);
						qs.set("Ex", "10");
						if(st.getPlayer().getClassId().isMage())
						{
							st.playTutorialVoice("tutorial_voice_027");
							st.giveItems(5790, 3000, false, false);
						}
						else
						{
							st.playTutorialVoice("tutorial_voice_026");
							st.giveItems(5789, 6000, false, false);
						}
					}
				}

				return Chrysolite_Ore_count > 0 && Hidden_Ore_Map_count > 0 ? "elder_filaur_q0293_09.htm" : Hidden_Ore_Map_count > 0 ? "elder_filaur_q0293_08.htm" : "elder_filaur_q0293_05.htm";
			}
		}

		if(npcId == Chichirin)
		{
			if(cond > 0)
				return "chichirin_q0293_01.htm";
		}
		return NO_QUEST_DIALOG;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState qs)
	{
		if(qs.isStarted())
		{
			if(Rnd.chance(Torn_Map_Fragment_Chance))
			{
				qs.giveItems(Torn_Map_Fragment, 1, true, true);
				qs.playSound(SOUND_ITEMGET);
			}
			else if(Rnd.chance(Chrysolite_Ore_Chance))
			{
				qs.giveItems(Chrysolite_Ore, 1, true, true);
				qs.playSound(SOUND_ITEMGET);
			}
		}
		return null;
	}
}