package quests;

import l2s.gameserver.model.base.ClassLevel;
import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage.ScreenMessageAlign;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;

public class _104_SpiritOfMirror extends QuestScript
{
	static final int GALLINS_OAK_WAND = 748;
	static final int WAND_SPIRITBOUND1 = 1135;
	static final int WAND_SPIRITBOUND2 = 1136;
	static final int WAND_SPIRITBOUND3 = 1137;
	static final int WAND_OF_ADEPT = 747;

	static final SystemMessagePacket CACHE_SYSMSG_GALLINS_OAK_WAND = SystemMessagePacket.removeItems(GALLINS_OAK_WAND, 1);

	public _104_SpiritOfMirror()
	{
		super(PARTY_ALL, ONETIME);
		addStartNpc(30017);
		addTalkId(30041, 30043, 30045);
		addKillId(27003, 27004, 27005);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		if(event.equalsIgnoreCase("gallin_q0104_03.htm"))
		{
			st.setCond(1);
			st.giveItems(GALLINS_OAK_WAND, 3, false, false);
		}
		return event;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == 30017)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getRace() != Race.HUMAN)
					htmltext = "gallin_q0104_00.htm";
				else if(st.getPlayer().getLevel() >= 10)
					htmltext = "gallin_q0104_02.htm";
				else
					htmltext = "gallin_q0104_06.htm";
			}
			else if(cond == 1 && st.getQuestItemsCount(GALLINS_OAK_WAND) >= 1 && (st.getQuestItemsCount(WAND_SPIRITBOUND1) == 0 || st.getQuestItemsCount(WAND_SPIRITBOUND2) == 0 || st.getQuestItemsCount(WAND_SPIRITBOUND3) == 0))
				htmltext = "gallin_q0104_04.htm";
			else if(cond == 3 && st.getQuestItemsCount(WAND_SPIRITBOUND1) >= 1 && st.getQuestItemsCount(WAND_SPIRITBOUND2) >= 1 && st.getQuestItemsCount(WAND_SPIRITBOUND3) >= 1)
			{
				st.takeAllItems(WAND_SPIRITBOUND1, WAND_SPIRITBOUND2, WAND_SPIRITBOUND3);

				st.giveItems(WAND_OF_ADEPT, 1, false, false);
				st.giveItems(ADENA_ID, 16866, true, true);
				st.addExpAndSp(39750, 3407);

				if(st.getPlayer().getClassId().isOfLevel(ClassLevel.NONE) && !st.getPlayer().getVarBoolean("p1q3"))
				{
					st.getPlayer().setVar("p1q3", "1", -1); // flag for helper
					st.getPlayer().sendPacket(new ExShowScreenMessage(NpcString.NONE, 5000, ScreenMessageAlign.TOP_CENTER, true, "Now go find the Newbie Guide."));
					st.giveItems(1060, 100, false, false); // healing potion
					for(int item = 4412; item <= 4417; item++)
						st.giveItems(item, 10, false, false); // echo cry
					if(st.getPlayer().getClassId().isMage())
					{
						st.playTutorialVoice("tutorial_voice_027");
						st.giveItems(5790, 3000, true, true); // newbie sps
					}
					else
					{
						st.playTutorialVoice("tutorial_voice_026");
						st.giveItems(5789, 6000, true, true); // newbie ss
					}
				}

				htmltext = "gallin_q0104_05.htm";
				st.finishQuest();
			}
		}
		else if((npcId == 30041 || npcId == 30043 || npcId == 30045) && cond == 1)
		{
			if(npcId == 30041 && st.getInt("id1") == 0)
				st.set("id1", "1");
			htmltext = "arnold_q0104_01.htm";
			if(npcId == 30043 && st.getInt("id2") == 0)
				st.set("id2", "1");
			htmltext = "johnson_q0104_01.htm";
			if(npcId == 30045 && st.getInt("id3") == 0)
				st.set("id3", "1");
			htmltext = "ken_q0104_01.htm";
			if(st.getInt("id1") + st.getInt("id2") + st.getInt("id3") == 3)
			{
				st.setCond(2);
				st.unset("id1");
				st.unset("id2");
				st.unset("id3");
			}
		}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int cond = st.getCond();
		int npcId = npc.getNpcId();

		if((cond == 1 || cond == 2) && st.getPlayer().getActiveWeaponInstance() != null && st.getPlayer().getActiveWeaponInstance().getItemId() == GALLINS_OAK_WAND)
		{
			ItemInstance weapon = st.getPlayer().getActiveWeaponInstance();
			if(npcId == 27003 && st.getQuestItemsCount(WAND_SPIRITBOUND1) == 0)
			{
				if(st.getPlayer().getInventory().destroyItem(weapon, 1L))
				{
					st.giveItems(WAND_SPIRITBOUND1, 1, false, false);
					st.getPlayer().sendPacket(CACHE_SYSMSG_GALLINS_OAK_WAND);
					long Collect = st.getQuestItemsCount(WAND_SPIRITBOUND1) + st.getQuestItemsCount(WAND_SPIRITBOUND2) + st.getQuestItemsCount(WAND_SPIRITBOUND3);
					if(Collect == 3)
					{
						st.setCond(3);
					}
					else
						st.playSound(SOUND_ITEMGET);
				}
			}
			else if(npcId == 27004 && st.getQuestItemsCount(WAND_SPIRITBOUND2) == 0)
			{
				if(st.getPlayer().getInventory().destroyItem(weapon, 1L))
				{
					st.giveItems(WAND_SPIRITBOUND2, 1, false, false);
					st.getPlayer().sendPacket(CACHE_SYSMSG_GALLINS_OAK_WAND);
					long Collect = st.getQuestItemsCount(WAND_SPIRITBOUND1) + st.getQuestItemsCount(WAND_SPIRITBOUND2) + st.getQuestItemsCount(WAND_SPIRITBOUND3);
					if(Collect >= 3)
					{
						st.setCond(3);
					}
					else
						st.playSound(SOUND_ITEMGET);
				}
			}
			else if(npcId == 27005 && st.getQuestItemsCount(WAND_SPIRITBOUND3) == 0)
				if(st.getPlayer().getInventory().destroyItem(weapon, 1L))
				{
					st.giveItems(WAND_SPIRITBOUND3, 1, false, false);
					st.getPlayer().sendPacket(CACHE_SYSMSG_GALLINS_OAK_WAND);
					long Collect = st.getQuestItemsCount(WAND_SPIRITBOUND1) + st.getQuestItemsCount(WAND_SPIRITBOUND2) + st.getQuestItemsCount(WAND_SPIRITBOUND3);
					if(Collect >= 3)
					{
						st.setCond(3);
					}
					else
						st.playSound(SOUND_ITEMGET);
				}
		}
		return null;
	}
}