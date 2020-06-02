package quests;

import l2s.gameserver.model.base.ClassLevel;
import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage.ScreenMessageAlign;

public class _260_HuntTheOrcs extends QuestScript
{

	private static final int ORC_AMULET = 1114;
	private static final int ORC_NECKLACE = 1115;

	public _260_HuntTheOrcs()
	{
		super(PARTY_NONE, REPEATABLE);

		addStartNpc(30221);

		addKillId(20468, 20469, 20470, 20471, 20472, 20473);

		addQuestItem(ORC_AMULET, ORC_NECKLACE);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equals("sentinel_rayjien_q0260_03.htm"))
		{
			st.setCond(1);
		}
		else if(event.equals("sentinel_rayjien_q0260_06.htm"))
		{
			st.finishQuest();
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		String htmltext = NO_QUEST_DIALOG;
		int cond = st.getCond();
		if(npcId == 30221)
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 6 && st.getPlayer().getRace() == Race.ELF)
				{
					htmltext = "sentinel_rayjien_q0260_02.htm";
					return htmltext;
				}
				else if(st.getPlayer().getRace() != Race.ELF)
					htmltext = "sentinel_rayjien_q0260_00.htm";
				else if(st.getPlayer().getLevel() < 6)
					htmltext = "sentinel_rayjien_q0260_01.htm";
				else if(cond == 1 && st.getQuestItemsCount(ORC_AMULET) == 0 && st.getQuestItemsCount(ORC_NECKLACE) == 0)
					htmltext = "sentinel_rayjien_q0260_04.htm";
			}
			else if(cond == 1 && (st.getQuestItemsCount(ORC_AMULET) > 0 || st.getQuestItemsCount(ORC_NECKLACE) > 0))
			{
				htmltext = "sentinel_rayjien_q0260_05.htm";
				int adenaPay = 0;
				if(st.getQuestItemsCount(ORC_AMULET) >= 40)
					adenaPay += st.getQuestItemsCount(ORC_AMULET) * 14;
				else
					adenaPay += st.getQuestItemsCount(ORC_AMULET) * 12;
				if(st.getQuestItemsCount(ORC_NECKLACE) >= 40)
					adenaPay += st.getQuestItemsCount(ORC_NECKLACE) * 40;
				else
					adenaPay += st.getQuestItemsCount(ORC_NECKLACE) * 30;
				st.giveItems(ADENA_ID, adenaPay, true, true);
				st.takeItems(ORC_AMULET, -1);
				st.takeItems(ORC_NECKLACE, -1);

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
			}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		if(st.getCond() > 0)
			if(npcId == 20468 || npcId == 20469 || npcId == 20470)
				st.rollAndGive(ORC_AMULET, 1, 14);
			else if(npcId == 20471 || npcId == 20472 || npcId == 20473)
				st.rollAndGive(ORC_NECKLACE, 1, 14);
		return null;
	}
}