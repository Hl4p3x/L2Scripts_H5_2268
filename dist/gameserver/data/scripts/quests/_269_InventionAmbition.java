package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _269_InventionAmbition extends QuestScript
{
	//NPC
	public final int INVENTOR_MARU = 32486;
	//MOBS
	public final int[] MOBS = {
			21124,
			// Red Eye Barbed Bat
			21125,
			// Northern Trimden
			21126,
			// Kerope Werewolf
			21127,
			// Northern Goblin
			21128,
			// Spine Golem
			21129,
			// Kerope Werewolf Chief
			21130,
			// Northern Goblin Leader
			21131,
			// Enchanted Spine Golem
	};
	//ITEMS
	public final int ENERGY_ORES = 10866;

	public _269_InventionAmbition()
	{
		super(PARTY_NONE, REPEATABLE);
		addStartNpc(INVENTOR_MARU);
		addKillId(MOBS);
		addQuestItem(ENERGY_ORES);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equals("inventor_maru_q0269_04.htm"))
		{
			st.setCond(1);
		}
		else if(event.equals("inventor_maru_q0269_07.htm"))
		{
			st.finishQuest();
		}
		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int cond = st.getCond();
		if(npc.getNpcId() == INVENTOR_MARU)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() < 18)
					htmltext = "inventor_maru_q0269_02.htm";
				else
					htmltext = "inventor_maru_q0269_01.htm";
			}
			else
			{
				long count = st.getQuestItemsCount(ENERGY_ORES);
				if(count > 0)
				{
					st.giveItems(ADENA_ID, count * 50 + 2044 * (count / 20), true, true);
					st.takeItems(ENERGY_ORES, -1);
					htmltext = "inventor_maru_q0269_06.htm";
				}
				else
					htmltext = "inventor_maru_q0269_05.htm";
			}
		}
		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState st)
	{
		if(st.getCond() == 1)
		{
			if(Rnd.chance(60))
			{
				st.giveItems(ENERGY_ORES, 1, true, true);
				st.playSound(SOUND_ITEMGET);
			}
		}
		return null;
	}
}