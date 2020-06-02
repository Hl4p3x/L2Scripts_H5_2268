package quests;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;

public class _306_CrystalOfFireice extends QuestScript
{
	//NPCs
	private static int Katerina = 30004;
	//Mobs
	private static int Salamander = 20109;
	private static int Undine = 20110;
	private static int Salamander_Elder = 20112;
	private static int Undine_Elder = 20113;
	private static int Salamander_Noble = 20114;
	private static int Undine_Noble = 20115;
	//Quest Items
	private static int Flame_Shard = 1020;
	private static int Ice_Shard = 1021;
	//Chances
	private static int Chance = 30;
	private static int Elder_Chance = 40;
	private static int Noble_Chance = 50;

	public _306_CrystalOfFireice()
	{
		super(PARTY_NONE, REPEATABLE);
		addStartNpc(Katerina);
		addKillId(Salamander);
		addKillId(Undine);
		addKillId(Salamander_Elder);
		addKillId(Undine_Elder);
		addKillId(Salamander_Noble);
		addKillId(Undine_Noble);
		addQuestItem(Flame_Shard);
		addQuestItem(Ice_Shard);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		if(event.equalsIgnoreCase("katrine_q0306_04.htm"))
		{
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("katrine_q0306_08.htm"))
		{
			st.finishQuest();
		}
		return event;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		if(npc.getNpcId() != Katerina)
			return htmltext;

		int cond = st.getCond();
		if(cond == 0)
		{
			if(st.getPlayer().getLevel() < 17)
				htmltext = "katrine_q0306_02.htm";
			else
				htmltext = "katrine_q0306_03.htm";
		}
		else
		{
			long Shrads_count = st.getQuestItemsCount(Flame_Shard) + st.getQuestItemsCount(Ice_Shard);
			long Reward = Shrads_count * 40 + (Shrads_count >= 10 ? 5000 : 0);
			if(Reward > 0)
			{
				htmltext = "katrine_q0306_07.htm";
				st.takeItems(Flame_Shard, -1);
				st.takeItems(Ice_Shard, -1);
				st.giveItems(ADENA_ID, Reward, true, true);
			}
			else
				htmltext = "katrine_q0306_05.htm";
		}

		return htmltext;
	}

	@Override
	public String onKill(NpcInstance npc, QuestState qs)
	{
		if(qs.isStarted())
		{
			int npcId = npc.getNpcId();
			if((npcId == Salamander || npcId == Undine) && !Rnd.chance(Chance))
				return null;
			if((npcId == Salamander_Elder || npcId == Undine_Elder) && !Rnd.chance(Elder_Chance))
				return null;
			if((npcId == Salamander_Noble || npcId == Undine_Noble) && !Rnd.chance(Noble_Chance))
				return null;
			qs.giveItems(npcId == Salamander || npcId == Salamander_Elder || npcId == Salamander_Noble ? Flame_Shard : Ice_Shard, 1, true, true);
			qs.playSound(SOUND_ITEMGET);
		}
		return null;
	}
}