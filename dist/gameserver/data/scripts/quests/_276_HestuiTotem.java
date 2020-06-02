package quests;

import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.model.base.ClassLevel;
import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage.ScreenMessageAlign;

public class _276_HestuiTotem extends QuestScript
{
	//NPCs
	private static int Tanapi = 30571;
	//Mobs
	private static int Kasha_Bear = 20479;
	private static int Kasha_Bear_Totem_Spirit = 27044;
	//Items
	private static int Leather_Pants = 29;
	private static int Totem_of_Hestui = 1500;
	//Quest Items
	private static int Kasha_Parasite = 1480;
	private static int Kasha_Crystal = 1481;

	public _276_HestuiTotem()
	{
		super(PARTY_NONE, REPEATABLE);
		addStartNpc(Tanapi);
		addKillId(Kasha_Bear);
		addKillId(Kasha_Bear_Totem_Spirit);
		addQuestItem(Kasha_Parasite);
		addQuestItem(Kasha_Crystal);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		if(event.equalsIgnoreCase("seer_tanapi_q0276_03.htm") && st.getPlayer().getRace() == Race.ORC && st.getPlayer().getLevel() >= 15)
		{
			st.setCond(1);
		}
		return event;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == Tanapi)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getRace() != Race.ORC)
					htmltext = "seer_tanapi_q0276_00.htm";
				else if(st.getPlayer().getLevel() < 15)
					htmltext = "seer_tanapi_q0276_01.htm";
				else
					htmltext = "seer_tanapi_q0276_02.htm";
			}
			else
			{
				if(st.getQuestItemsCount(Kasha_Crystal) > 0)
				{
					htmltext = "seer_tanapi_q0276_05.htm";
					st.takeItems(Kasha_Parasite, -1);
					st.takeItems(Kasha_Crystal, -1);

					st.giveItems(Leather_Pants, 1, false, false);
					st.giveItems(Totem_of_Hestui, 1, false, false);
					if(st.getRateQuestsReward() > 1)
						st.giveItems(57, Math.round(ItemHolder.getInstance().getTemplate(Totem_of_Hestui).getReferencePrice() * (st.getRateQuestsReward() - 1) / 2), true, true);

					if(st.getPlayer().getClassId().isOfLevel(ClassLevel.NONE) && !st.getPlayer().getVarBoolean("p1q4"))
					{
						st.getPlayer().setVar("p1q4", "1", -1);
						st.getPlayer().sendPacket(new ExShowScreenMessage(NpcString.NONE, 5000, ScreenMessageAlign.TOP_CENTER, true, "Now go find the Newbie Guide."));
					}
					st.finishQuest();
				}
				else
					htmltext = "seer_tanapi_q0276_04.htm";
			}
		}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState qs)
	{
		if(qs.isStarted())
		{
			int npcId = npc.getNpcId();
			if(npcId == Kasha_Bear && qs.getQuestItemsCount(Kasha_Crystal) == 0)
			{
				if(qs.getQuestItemsCount(Kasha_Parasite) < 50)
				{
					qs.giveItems(Kasha_Parasite, 1, true, true);
					qs.playSound(SOUND_ITEMGET);
				}
				else
				{
					qs.takeItems(Kasha_Parasite, -1);
					qs.addSpawn(Kasha_Bear_Totem_Spirit);
				}
			}
			else if(npcId == Kasha_Bear_Totem_Spirit && qs.getQuestItemsCount(Kasha_Crystal) == 0)
			{
				qs.giveItems(Kasha_Crystal, 1, false, false);
				qs.playSound(SOUND_MIDDLE);
			}
		}
		return null;
	}
}