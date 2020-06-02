package quests;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.ReflectionUtils;

/**
 * @author pchayka
 */

public class _10286_ReunionWithSirra extends QuestScript
{
	private static final int Rafforty = 32020;
	private static final int Jinia = 32760;
	private static final int Jinia2 = 32781;
	private static final int Sirra = 32762;

	public _10286_ReunionWithSirra()
	{
		super(PARTY_NONE, ONETIME);
		addStartNpc(Rafforty);
		addTalkId(Jinia, Jinia2, Sirra);
	}

	@Override
	public String onEvent(String event, QuestState st, NpcInstance npc)
	{
		String htmltext = event;
		if(event.equalsIgnoreCase("rafforty_q10286_02.htm"))
		{
			st.setCond(1);
		}
		else if(event.equalsIgnoreCase("enterinstance"))
		{
			st.setCond(2);
			enterInstance(st.getPlayer(), 141);
			return null;
		}
		else if(event.equalsIgnoreCase("sirraspawn"))
		{
			st.setCond(3);
			NpcInstance sirra = st.getPlayer().getReflection().addSpawnWithoutRespawn(Sirra, new Location(-23848, -8744, -5413, 49152), 0);
			Functions.npcSay(sirra, "Вы с таким энтузиазмом отправились в путь и это все, чего Вы добиись? Хе-хе-хе...");
			return null;
		}
		else if(event.equalsIgnoreCase("sirra_q10286_04.htm"))
		{
			st.giveItems(15470, 5, false, false);
			st.setCond(4);
			npc.deleteMe();
		}
		else if(event.equalsIgnoreCase("leaveinstance"))
		{
			st.setCond(5);
			st.getPlayer().getReflection().collapse();
			return null;
		}

		return htmltext;
	}

	@Override
	public String onTalk(NpcInstance npc, QuestState st)
	{
		String htmltext = NO_QUEST_DIALOG;
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == Rafforty)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 82 && st.getPlayer().isQuestCompleted(10285))
					htmltext = "rafforty_q10286_01.htm";
				else
					htmltext = "rafforty_q10286_00.htm";
			}
			else if(cond == 1 || cond == 2 || cond == 3 || cond == 4)
				htmltext = "rafforty_q10286_03.htm";
		}
		else if(npcId == Jinia)
		{
			if(cond == 2)
				htmltext = "jinia_q10286_01.htm";
			else if(cond == 3)
				htmltext = "jinia_q10286_01a.htm";
			else if(cond == 4)
				htmltext = "jinia_q10286_05.htm";
		}
		else if(npcId == Sirra)
		{
			if(cond == 3)
				htmltext = "sirra_q10286_01.htm";
		}
		else if(npcId == Jinia2)
		{
			if(cond == 5)
				htmltext = "jinia2_q10286_01.htm";
			else if(cond == 6)
				htmltext = "jinia2_q10286_04.htm";
			else if(cond == 7)
			{
				htmltext = "jinia2_q10286_05.htm";
				st.addExpAndSp(2152200, 181070);
				st.finishQuest();
			}
		}
		return htmltext;
	}

	private void enterInstance(Player player, int izId)
	{
		Reflection r = player.getActiveReflection();
		if(r != null)
		{
			if(player.canReenterInstance(izId))
				player.teleToLocation(r.getTeleportLoc(), r);
		}
		else if(player.canEnterInstance(izId))
		{
			ReflectionUtils.enterReflection(player, izId);
		}
	}
}