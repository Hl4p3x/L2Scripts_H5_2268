package quests;

// version = Unknown

import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

/**
 * Рейты учтены
 */
public class _003_WilltheSealbeBroken extends QuestScript
{
	int StartNpc = 30141;
	int[] Monster = {
			20031,
			20041,
			20046,
			20048,
			20052,
			20057
	};

	int OnyxBeastEye = 1081;
	int TaintStone = 1082;
	int SuccubusBlood = 1083;

	public _003_WilltheSealbeBroken()
	{
		super(PARTY_NONE, ONETIME);
		addStartNpc(StartNpc);

		for(int npcId : Monster)
			addKillId(npcId);

		addQuestItem(new int[]{
				OnyxBeastEye,
				TaintStone,
				SuccubusBlood
		});
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("quest_accept"))
		{
			htmltext = "redry_q0003_03.htm";
			st.setCond(1);
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int cond = st.getCond();
		if(cond == 0)
		{
			if(st.getPlayer().getRace() != Race.DARKELF)
				htmltext = "redry_q0003_00.htm";
			else if(st.getPlayer().getLevel() >= 16)
				htmltext = "redry_q0003_02.htm";
			else
				htmltext = "redry_q0003_01.htm";
		}
		else if(cond > 1)
		{
			if(st.getQuestItemsCount(OnyxBeastEye) > 0 && st.getQuestItemsCount(TaintStone) > 0 && st.getQuestItemsCount(SuccubusBlood) > 0)
			{
				htmltext = "redry_q0003_06.htm";
				st.takeItems(OnyxBeastEye, -1);
				st.takeItems(TaintStone, -1);
				st.takeItems(SuccubusBlood, -1);
				st.giveItems(956, 1, false, false);
				st.finishQuest();
			}
			else
				htmltext = "redry_q0003_04.htm";
		}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(cond == 1)
		{
			if(npcId == Monster[0] && st.getQuestItemsCount(OnyxBeastEye) == 0)
			{
				st.giveItems(OnyxBeastEye, 1, false, false);
				st.playSound(SOUND_ITEMGET);
			}
			else if((npcId == Monster[1] || npcId == Monster[2]) && st.getQuestItemsCount(TaintStone) == 0)
			{
				st.giveItems(TaintStone, 1, false, false);
				st.playSound(SOUND_ITEMGET);
			}
			else if((npcId == Monster[3] || npcId == Monster[4] || npcId == Monster[5]) && st.getQuestItemsCount(SuccubusBlood) == 0)
			{
				st.giveItems(SuccubusBlood, 1, false, false);
				st.playSound(SOUND_ITEMGET);
			}
			if(st.getQuestItemsCount(OnyxBeastEye) > 0 && st.getQuestItemsCount(TaintStone) > 0 && st.getQuestItemsCount(SuccubusBlood) > 0)
			{
				st.setCond(2);
			}
		}
		return null;
	}
}