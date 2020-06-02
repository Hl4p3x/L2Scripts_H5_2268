package quests;

import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.utils.Location;

/**
 * @author: pchayka
 * @date: 10.06.2010
 */

public class _10272_LightFragment extends QuestScript
{
	// NPC's
	private static int Orbyu = 32560;
	private static int Artius = 32559;
	private static int Lelikia = 32567;
	private static int Ginby = 32566;
	private static int Lekon = 32557;

	//Monsters (every monster in SoD when stage is "Attack")

	// ITEMS
	private static int DestroyedDarknessFragmentPowder = 13853;
	private static int DestroyedLightFragmentPowder = 13854;
	private static int SacredLightFragment = 13855;

	private static final Location LELIKIA_POSITION = new Location(-170936, 247768, 1102);
	private static final Location BASE_POSITION = new Location(-185032, 242824, 1553);

	public _10272_LightFragment()
	{
		super(PARTY_ONE, ONETIME);

		addStartNpc(Orbyu);
		addTalkId(Orbyu);
		addTalkId(Artius);
		addTalkId(Lelikia);
		addTalkId(Ginby);
		addTalkId(Lekon);

		addKillId(22552, 22541, 22550, 22551, 22596, 22544, 22540, 22547, 22542, 22543, 22539, 22546, 22548, 22536, 22538, 22537);

		addQuestItem(DestroyedDarknessFragmentPowder);
		addQuestItem(DestroyedLightFragmentPowder);
		addQuestItem(SacredLightFragment);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		int cond = st.getCond();
		String htmltext = event;

		if(event.equalsIgnoreCase("orbyu_q10272_2.htm") && cond == 0)
		{
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("artius_q10272_2.htm"))
		{
			st.setCond(2);
		}
		else if(event.equalsIgnoreCase("artius_q10272_4.htm"))
		{
			st.setCond(3);
		}
		else if(event.equalsIgnoreCase("tele_to_lelikia"))
		{
			if(st.getQuestItemsCount(ADENA_ID) >= 10000)
			{
				st.takeItems(ADENA_ID, 10000);
				st.getPlayer().teleToLocation(LELIKIA_POSITION);
				return null;
			}
			else
			{
				st.getPlayer().sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
				return null;
			}
		}
		else if(event.equalsIgnoreCase("lelikia_q10272_2.htm"))
		{
			st.setCond(4);
		}
		else if(event.equalsIgnoreCase("tele_to_base"))
		{
			st.getPlayer().teleToLocation(BASE_POSITION);
			return null;
		}
		else if(event.equalsIgnoreCase("artius_q10272_7.htm"))
		{
			st.setCond(5);
		}
		else if(event.equalsIgnoreCase("artius_q10272_9.htm"))
		{
			st.setCond(6);
		}
		else if(event.equalsIgnoreCase("artius_q10272_11.htm"))
		{
			st.setCond(7);
		}
		else if(event.equalsIgnoreCase("lekon_q10272_2.htm"))
		{
			if(st.getQuestItemsCount(DestroyedLightFragmentPowder) >= 100)
			{
				st.takeItems(DestroyedLightFragmentPowder, -1);
				st.giveItems(SacredLightFragment, 1, false, false);
				st.setCond(8);
			}
			else
				htmltext = "lekon_q10272_1a.htm";
		}
		else if(event.equalsIgnoreCase("artius_q10272_12.htm"))
		{
			st.giveItems(ADENA_ID, 556980, true, true);
			st.addExpAndSp(1009016, 91363);
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
		if(npcId == Orbyu)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 75 && st.getPlayer().isQuestCompleted(10271))
					htmltext = "orbyu_q10272_1.htm";
				else
					htmltext = "orbyu_q10272_0.htm";
			}
			else if(cond == 4)
				htmltext = "orbyu_q10271_4.htm";
		}
		else if(npcId == Artius)
		{
			if(cond == 1)
				htmltext = "artius_q10272_1.htm";
			else if(cond == 2)
				htmltext = "artius_q10272_3.htm";
			else if(cond == 4)
				htmltext = "artius_q10272_5.htm";
			else if(cond == 5)
			{
				if(st.getQuestItemsCount(DestroyedDarknessFragmentPowder) >= 100)
					htmltext = "artius_q10272_8.htm";
				else
					htmltext = "artius_q10272_8a.htm";
			}
			else if(cond == 6)
			{
				if(st.getQuestItemsCount(DestroyedLightFragmentPowder) >= 100)
					htmltext = "artius_q10272_10.htm";
				else
					htmltext = "artius_q10272_10a.htm";
			}
			else if(cond == 8)
				htmltext = "artius_q10272_12.htm";

		}
		else if(npcId == Ginby)
		{
			if(cond == 3)
				htmltext = "ginby_q10272_1.htm";
		}
		else if(npcId == Lelikia)
		{
			if(cond == 3)
				htmltext = "lelikia_q10272_1.htm";
		}
		else if(npcId == Lekon)
			if(cond == 7 && st.getQuestItemsCount(DestroyedLightFragmentPowder) >= 100)
				htmltext = "lekon_q10272_1.htm";
			else
				htmltext = "lekon_q10272_1a.htm";
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.getCond() == 5)
			if(st.getQuestItemsCount(DestroyedDarknessFragmentPowder) <= 100)
			{
				st.giveItems(DestroyedDarknessFragmentPowder, 1, true, true);
				st.playSound(SOUND_ITEMGET);
			}
		return null;
	}
}
